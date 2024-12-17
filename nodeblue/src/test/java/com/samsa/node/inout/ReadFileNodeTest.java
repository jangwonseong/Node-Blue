package com.samsa.node.inout;

import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ReadFileNode} 클래스의 동작을 검증하기 위한 테스트 클래스입니다.
 * <p>
 * 파일 경로를 통해 데이터를 읽어 메시지 페이로드를 업데이트하는 동작, 
 * 예외 상황 및 다양한 생성자 사용을 테스트합니다.
 * </p>
 *
 * @see ReadFileNode
 * @see Message
 */
class ReadFileNodeTest {
    private ReadFileNode readNode;
    private static final Path LOG_FILE_PATH = Path.of("log/log.log");

    /**
     * 각 테스트 실행 전에 {@link ReadFileNode} 인스턴스를 초기화합니다.
     * 기본적으로 모든 라인을 읽는 설정으로 초기화됩니다.
     */
    @BeforeEach
    void setUp() {
        readNode = new ReadFileNode(LOG_FILE_PATH, true);
    }

    /**
     * {@code null} 파일 경로로 생성자를 호출했을 때 {@link IllegalArgumentException}이 발생하는지 검증합니다.
     */
    @Test
    void testConstructorWithNullPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ReadFileNode(null, true);
        });
    }

    /**
     * ID를 포함한 생성자에 {@code null} 파일 경로를 제공했을 때 {@link IllegalArgumentException}이 발생하는지 확인합니다.
     */
    @Test
    void testConstructorWithIdAndNullPath() {
        UUID id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> {
            new ReadFileNode(id, null, true);
        });
    }

    /**
     * 파일의 모든 라인을 읽어 {@link List} 형태의 페이로드로 메시지가 업데이트되는지 검증합니다.
     */
    @Test
    void testReadAllLines() {
        Message message = new Message("");
        readNode.onMessage(message);
        
        Object payload = message.getPayload();
        assertTrue(payload instanceof List);
        @SuppressWarnings("unchecked")
        List<String> lines = (List<String>) payload;
        assertFalse(lines.isEmpty());
    }

    /**
     * 단일 라인 읽기 모드에서 파일의 첫 번째 라인을 페이로드로 읽는지 확인합니다.
     */
    @Test
    void testReadSingleLine() {
        ReadFileNode singleLineNode = new ReadFileNode(LOG_FILE_PATH, false);
        Message message = new Message("");
        singleLineNode.onMessage(message);
        
        Object payload = message.getPayload();
        assertTrue(payload instanceof String);
        assertFalse(((String) payload).isEmpty());
    }

    /**
     * {@code null} 메시지가 제공되었을 때 예외가 발생하지 않는지 검증합니다.
     */
    @Test
    void testNullMessage() {
        assertDoesNotThrow(() -> readNode.onMessage(null));
    }

    /**
     * 사용자 정의 ID를 사용한 생성자에서 ID가 올바르게 설정되는지 검증합니다.
     */
    @Test
    void testCustomIdConstructor() {
        UUID customId = UUID.randomUUID();
        ReadFileNode customNode = new ReadFileNode(customId, LOG_FILE_PATH, true);
        assertEquals(customId, customNode.getId());
    }

    /**
     * 존재하지 않는 파일 경로를 지정했을 때 {@link RuntimeException}이 발생하는지 확인합니다.
     */
    @Test
    void testInvalidFilePath() {
        ReadFileNode invalidNode = new ReadFileNode(Path.of("invalid/path.txt"), true);
        Message message = new Message("");
        
        assertThrows(RuntimeException.class, () -> 
            invalidNode.onMessage(message));
    }
}
