package com.samsa.node.inout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;
import com.samsa.annotation.NodeType;

import lombok.extern.slf4j.Slf4j;

/**
 * {@code FunctionNode} 클래스는 사용자 정의 로직을 처리할 수 있도록 함수형 인터페이스 기반 메시지 핸들러를 제공하는 노드입니다.
 */
@NodeType("FunctionNode")
@Slf4j
public class FunctionNode extends InOutNode {

    /**
     * 메시지를 처리하는 함수형 인터페이스입니다.
     */
    @FunctionalInterface
    public interface MessageHandler {
        void handle(Message message);
    }

    private final MessageHandler messageHandler;

    /**
     * {@code FunctionNode} 생성자.
     * 
     * @param className 사용자 정의 클래스 이름
     * @param userCode 사용자 정의 코드
     */
    @JsonCreator
    public FunctionNode(@JsonProperty("ClassName") String className,
            @JsonProperty("code") String userCode) {
        super();
        this.messageHandler = createMessageHandler(className, userCode);
    }

    /**
     * 사용자 정의 메시지 핸들러를 생성합니다.
     * 
     * @param className 클래스 이름
     * @param userCode 사용자 코드
     * @return 생성된 {@code MessageHandler}
     */
    private MessageHandler createMessageHandler(String className, String userCode) {
        String sourceFilePath = null;
        try {
            String importCode = loadImportJson();
            String packageName = "com.samsa";
            String javaCode = String.format("""
                    package %s;

                    %s

                    @Slf4j
                    public class %s {
                        public void handle(Message message) {
                            %s
                        }
                    }
                    """, packageName, importCode, className, userCode);

            sourceFilePath = writeJavaSourceToFile(javaCode, packageName, className);
            compileJavaFile(sourceFilePath);
            return loadClassAndCreateHandler(packageName, className);

        } catch (Exception e) {
            log.error("사용자 정의 메시지 핸들러 생성 중 오류가 발생했습니다.", e);
            return message -> log.info("기본 메시지 핸들러 실행: {}", message.getPayload());
        } finally {
            if (sourceFilePath != null) {
                // deleteGeneratedFiles(sourceFilePath, className);
            }
        }
    }

    /**
     * import.json 파일에서 임포트 코드를 로드합니다.
     * 
     * @return 임포트 코드 문자열
     */
    public String loadImportJson() {
        try (InputStream inputStream =
                FunctionNode.class.getClassLoader().getResourceAsStream("import.json")) {

            if (inputStream == null) {
                log.error("import.json 파일을 리소스 폴더에서 찾을 수 없습니다.");
                throw new IOException("import.json 파일을 리소스 폴더에서 찾을 수 없습니다.");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(inputStream);
            JsonNode importArray = rootNode.get("import");
            if (importArray == null) {
                log.error("import.json 파일에 'import' 배열이 존재하지 않습니다.");
                throw new IOException("'import' 배열이 존재하지 않습니다.");
            }

            StringBuilder result = new StringBuilder();
            for (JsonNode node : importArray) {
                result.append(node.asText()).append("\n");
            }
            return result.toString();

        } catch (IOException e) {
            log.error("import.json 파일을 읽는 중 오류가 발생했습니다.", e);
            throw new RuntimeException("import.json 파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    private static String writeJavaSourceToFile(String javaCode, String packageName,
            String className) throws IOException {
        String dirPath =
                packageName.isEmpty() ? "" : "src/main/java/" + packageName.replace('.', '/');
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("디렉토리 생성 실패: " + dirPath);
        }

        String sourceFilePath = (dirPath.isEmpty() ? "" : dirPath + "/") + className + ".java";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sourceFilePath))) {
            writer.write(javaCode);
        }
        return sourceFilePath;
    }

    private static void compileJavaFile(String sourceFilePath) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("JavaCompiler를 사용할 수 없습니다.");
        }

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager
                .getJavaFileObjectsFromStrings(Collections.singletonList(sourceFilePath));
        boolean success =
                compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
        fileManager.close();

        if (!success) {
            throw new RuntimeException("컴파일 실패");
        }
    }

    private static MessageHandler loadClassAndCreateHandler(String packageName, String className)
            throws Exception {
        File currentDir = new File(".");
        URL[] urls = {currentDir.toURI().toURL()};
        try (URLClassLoader classLoader = new URLClassLoader(urls)) {
            String qualifiedClassName =
                    packageName.isEmpty() ? className : packageName + "." + className;
            Class<?> clazz = classLoader.loadClass(qualifiedClassName);

            Object instance = clazz.getDeclaredConstructor().newInstance();
            Method handleMethod = clazz.getMethod("handle", Message.class);

            return message -> {
                try {
                    handleMethod.invoke(instance, message);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("동적 로딩된 핸들러 실행 중 오류가 발생했습니다.", e);
                }
            };
        }
    }

    private static void deleteGeneratedFiles(String sourceFilePath, String className) {
        try {
            File sourceFile = new File(sourceFilePath);
            if (sourceFile.exists() && !sourceFile.delete()) {
                log.warn("소스 파일 삭제 실패: {}", sourceFilePath);
            }

            File classFile = new File(sourceFile.getParent(), className + ".class");
            if (classFile.exists() && !classFile.delete()) {
                log.warn("클래스 파일 삭제 실패: {}", classFile.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 메시지를 처리하는 메서드입니다.
     * 
     * @param message 처리할 메시지
     */
    @Override
    protected void onMessage(Message message) {
        if (message == null) {
            log.warn("Null 메시지를 받았습니다.");
            return;
        }
        try {
            log.info("메시지 ID: {} - 처리 시작", message.getId());
            messageHandler.handle(message);
        } catch (Exception e) {
            log.error("FunctionNode에서 메시지 처리 중 오류가 발생했습니다. NodeId: {}, MessageId: {}", getId(),
                    message.getId(), e);
        }
        log.info("메시지 ID: {} - 처리 완료", message.getId());
        emit(message);
    }
}
