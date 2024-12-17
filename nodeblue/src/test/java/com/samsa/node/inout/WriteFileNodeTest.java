package com.samsa.node.inout;

import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.UUID;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link WriteFileNode} 클래스의 동작을 검증하는 테스트 클래스입니다.
 * <p>
 * 파일에 메시지를 쓰는 기능, 추가 모드, 예외 상황 및 다양한 입력 조건을 테스트합니다.
 * </p>
 *
 * @see WriteFileNode
 * @see Message
 */
class WriteFileNodeTest {
    private WriteFileNode writeNode;
    private Path testFilePath;

    /**
     * JUnit 5의 {@code @TempDir}을 사용해 테스트용 임시 디렉터리를 생성합니다.
     * 테스트 파일은 이 디렉터리 안에 생성됩니다.
     */
    @TempDir
    Path tempDir;

    /**
     * 각 테스트 실행 전에 {@link WriteFileNode} 인스턴스를 초기화합니다.
     * 쓰기 모드는 덮어쓰기(append=false)로 설정됩니다.
     */
    @BeforeEach
    void setUp() {
        testFilePath = tempDir.resolve("test.txt");
        writeNode = new WriteFileNode(testFilePath, false);
    }

    /**
     * {@code null} 파일 경로로 생성자를 호출했을 때 {@link IllegalArgumentException}이 발생하는지 검증합니다.
     */
    @Test
    void testConstructorWithNullPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            new WriteFileNode(null, false);
        });
    }

    /**
     * 사용자 정의 ID를 포함한 생성자 호출 시 ID가 올바르게 설정되는지 검증합니다.
     */
    @Test
    void testConstructorWithCustomId() {
        UUID customId = UUID.randomUUID();
        WriteFileNode node = new WriteFileNode(customId, testFilePath, false);
        assertEquals(customId, node.getId());
    }

    /**
     * 메시지 페이로드를 파일에 쓰는 동작을 검증합니다.
     *
     * @throws IOException 파일 읽기 오류 발생 시
     */
    @Test
    void testWriteMessageToFile() throws IOException {
        String testContent = "테스트 내용";
        Message message = new Message(testContent);

        writeNode.onMessage(message);

        String fileContent = Files.readString(testFilePath);
        assertTrue(fileContent.contains(testContent));
    }

    /**
     * 추가 모드(append=true)에서 여러 메시지를 파일에 순차적으로 쓰는 동작을 검증합니다.
     *
     * @throws IOException 파일 읽기 오류 발생 시
     */
    @Test
    void testAppendMode() throws IOException {
        WriteFileNode appendNode = new WriteFileNode(testFilePath, true);

        Message firstMessage = new Message("첫번째 줄");
        Message secondMessage = new Message("두번째 줄");

        appendNode.onMessage(firstMessage);
        appendNode.onMessage(secondMessage);

        String fileContent = Files.readString(testFilePath);
        assertTrue(fileContent.contains("첫번째 줄"));
        assertTrue(fileContent.contains("두번째 줄"));
    }

    /**
     * {@code null} 메시지가 입력되었을 때 예외가 발생하지 않는지 검증합니다.
     */
    @Test
    void testNullMessage() {
        assertDoesNotThrow(() -> writeNode.onMessage(null));
    }

    /**
     * 메시지 페이로드가 {@code null}인 경우에도 예외가 발생하지 않는지 확인합니다.
     */
    @Test
    void testNullPayload() {
        Message message = new Message("test");
        message.setPayload(null);
        assertDoesNotThrow(() -> writeNode.onMessage(message));
    }

    /**
     * 존재하지 않는 파일 경로에 쓰기를 시도할 때 {@link RuntimeException}이 발생하는지 검증합니다.
     */
    @Test
    void testInvalidFilePath() {
        WriteFileNode invalidNode = new WriteFileNode(Path.of("/invalid/path/test.txt"), false);
        Message message = new Message("test");

        assertThrows(RuntimeException.class, () ->
            invalidNode.onMessage(message));
    }

    /**
     * 메시지 페이로드로 객체를 제공했을 때 객체의 {@code toString()} 결과가 파일에 쓰이는지 확인합니다.
     *
     * @throws IOException 파일 읽기 오류 발생 시
     */
    @Test
    void testObjectToStringConversion() throws IOException {
        Object testObject = new TestObject("test");
        Message message = new Message(testObject);

        writeNode.onMessage(message);

        String fileContent = Files.readString(testFilePath);
        assertTrue(fileContent.contains(testObject.toString()));
    }

    /**
     * 객체의 {@code toString()}을 재정의한 테스트용 클래스입니다.
     */
    private static class TestObject {
        private final String value;

        /**
         * 테스트 객체의 생성자입니다.
         *
         * @param value 객체의 문자열 값
         */
        TestObject(String value) {
            this.value = value;
        }

        /**
         * 객체를 문자열로 변환합니다.
         *
         * @return 객체의 문자열 표현
         */
        @Override
        public String toString() {
            return "TestObject{value='" + value + "'}";
        }
    }
}
