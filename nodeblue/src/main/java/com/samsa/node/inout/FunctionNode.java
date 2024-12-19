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
     * {@code MessageHandler}는 메시지를 처리하기 위한 함수형 인터페이스입니다.
     */
    @FunctionalInterface
    public interface MessageHandler {
        /**
         * 메시지를 처리하는 메서드입니다.
         *
         * @param message 처리할 메시지 객체
         */
        void handle(Message message);
    }

    private final MessageHandler messageHandler;

    /**
     * Jackson 역직렬화를 위한 기본 생성자
     */
    @JsonCreator
    public FunctionNode(MessageHandler messageHandler) {
        super();
        this.messageHandler = messageHandler != null ? messageHandler
                : message -> log.info("기본 메시지 핸들러 : {}", message.getPayload());
    }

    /**
     * 커스텀 메시지 핸들러를 사용하는 생성자. 사용자 정의 코드로 메시지 처리 로직을 구현합니다.
     *
     * @param className 클래스 이름
     * @param userCode 사용자 정의 코드
     */
    @JsonCreator
    public FunctionNode(@JsonProperty("ClassName") String className,
            @JsonProperty("code") String userCode) {
        super();
        this.messageHandler = createMessageHandler(className, userCode);
    }

    /**
     * 문자열로 받은 사용자 정의 코드를 컴파일하여 MessageHandler를 생성합니다.
     *
     * @param className 사용자 정의 클래스 이름
     * @param userCode 사용자 정의 코드
     * @return 동적으로 생성된 MessageHandler
     */
    private MessageHandler createMessageHandler(String className, String userCode) {
        try {
            // 사용자 정의 코드 포함한 Java 클래스 템플릿
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

            // 1. Java 소스 파일 생성
            String sourceFilePath = writeJavaSourceToFile(javaCode, packageName, className);

            // 2. Java 소스 파일 컴파일
            compileJavaFile(sourceFilePath);

            // 3. 동적으로 컴파일된 클래스 로드
            return loadClassAndCreateHandler(packageName, className);

        } catch (Exception e) {
            log.error("사용자 정의 메시지 핸들러 생성 중 오류 발생", e);
            // 오류 발생 시 기본 핸들러 사용
            return message -> log.info("기본 메시지 핸들러 실행: {}", message.getPayload());
        }
    }

    /**
     * 리소스 폴더에서 import.json 파일을 로드하여, 그 내용을 문자열로 반환합니다.
     * 
     * @return import.json 파일에 정의된 import 구문들로 구성된 문자열
     * @throws IOException import.json 파일을 읽는 중 오류가 발생한 경우
     */
    public String loadImportJson() {
        // JSON 파일을 읽기 위한 InputStream 객체를 가져옵니다.
        try (InputStream inputStream =
                FunctionNode.class.getClassLoader().getResourceAsStream("import.json")) {

            // 파일을 찾지 못한 경우
            if (inputStream == null) {
                log.error("import.json 파일을 리소스 폴더에서 찾을 수 없습니다.");
                throw new IOException("import.json 파일을 리소스 폴더에서 찾을 수 없습니다.");
            }

            // Jackson ObjectMapper를 이용해 JSON을 파싱합니다.
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(inputStream);

            // "import" 배열을 가져옵니다.
            JsonNode importArray = rootNode.get("import");
            if (importArray == null) {
                log.error("import.json 파일에 'import' 배열이 존재하지 않습니다.");
                throw new IOException("'import' 배열이 존재하지 않습니다.");
            }

            // 결과를 저장할 StringBuilder 객체
            StringBuilder result = new StringBuilder();

            // 배열 내 각 import 문을 처리하여 문자열로 만듭니다.
            for (JsonNode node : importArray) {
                result.append(node.asText()).append("\n");
            }

            // 처리된 결과 반환
            return result.toString();

        } catch (IOException e) {
            // 예외 발생 시 로깅하고 예외를 다시 던집니다.
            log.error("import.json 파일을 읽는 중 오류가 발생했습니다.", e);
            throw new RuntimeException("import.json 파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }


    /**
     * Java 소스 파일을 생성하고 해당 경로를 반환합니다.
     *
     * @param javaCode 생성할 Java 코드
     * @param packageName 패키지 이름
     * @param className 클래스 이름
     * @return 생성된 Java 소스 파일 경로
     * @throws IOException 파일 쓰기 오류
     */
    private static String writeJavaSourceToFile(String javaCode, String packageName,
            String className) throws IOException {
        String dirPath = packageName.isEmpty() ? ""
                : "nodeblue/src/main/java/" + packageName.replace('.', '/');
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

    /**
     * Java 소스 파일을 컴파일합니다.
     *
     * @param sourceFilePath 소스 파일 경로
     * @throws IOException 컴파일 중 오류 발생
     */
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

    /**
     * 컴파일된 클래스를 로드하고 MessageHandler를 생성합니다.
     *
     * @param packageName 패키지 이름
     * @param className 클래스 이름
     * @return MessageHandler 인스턴스
     * @throws Exception 로딩 또는 메서드 호출 오류
     */
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
                    log.error("동적 로딩된 핸들러 실행 중 오류 발생", e);
                }
            };
        }
    }

    /**
     * 메시지를 수신하고 사용자 정의 핸들러를 실행한 후 다음 노드로 메시지를 전달합니다.
     *
     * @param message 처리할 메시지 객체
     */
    @Override
    protected void onMessage(Message message) {
        if (message == null) {
            log.warn("Null 메시지를 받았습니다");
            return;
        }
        try {
            log.info("메시지 ID: {} - 처리 시작", message.getId());
            messageHandler.handle(message); // 함수형 인터페이스 호출
        } catch (Exception e) {
            log.error("FunctionNode에서 메시지 처리 중 오류 발생. NodeId: {}, MessageId: {}", getId(),
                    message.getId(), e);
        }
        log.info("메시지 ID: {} - 처리 완료", message.getId());
        emit(message); // 원본 또는 수정된 메시지를 다음 노드로 전달
    }
}
