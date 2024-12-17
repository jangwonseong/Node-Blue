// JsonParserNode 테스트 코드
package com.samsa.node.inout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonParserNodeTest {

    private JsonParserNode jsonParserNode;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jsonParserNode = new JsonParserNode(UUID.randomUUID(), TestPayload.class);
    }

    @Test
    void testOnMessageWithJsonString() throws JsonProcessingException {
        // Given
        TestPayload payload = new TestPayload("TestValue");
        String jsonString = MAPPER.writeValueAsString(payload);
        Message inputMessage = new Message(jsonString);

        // When
        jsonParserNode.onMessage(inputMessage);

        // Then
        Message emittedMessage = verifyEmission();
        assertNotNull(emittedMessage);
        assertTrue(emittedMessage.getPayload() instanceof TestPayload);
        assertEquals("TestValue", ((TestPayload) emittedMessage.getPayload()).value);
    }

    @Test
    void testOnMessageWithObject() throws JsonProcessingException {
        // Given
        TestPayload payload = new TestPayload("TestValue");
        Message inputMessage = new Message(payload);

        // When
        jsonParserNode.onMessage(inputMessage);

        // Then
        Message emittedMessage = verifyEmission();
        assertNotNull(emittedMessage);
        assertTrue(emittedMessage.getPayload() instanceof String);
        TestPayload result =
                MAPPER.readValue((String) emittedMessage.getPayload(), TestPayload.class);
        assertEquals("TestValue", result.value);
    }

    @Test
    void testOnMessageWithInvalidJson() {
        // Given
        String invalidJson = "{invalidJson}";
        Message inputMessage = new Message(invalidJson);

        // When & Then
        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> jsonParserNode.onMessage(inputMessage));
        assertTrue(exception.getCause() instanceof JsonProcessingException);
    }

    @Test
    void testOnMessageWithNullMessage() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> jsonParserNode.onMessage(null));
    }

    private Message verifyEmission() {
        return assertDoesNotThrow(() -> {
            // emit 메서드 호출 여부와 생성된 메시지 반환 검증
            // 실제 구현에서는 Node의 emit 동작을 목킹하여 확인 필요
            return null;
        });
    }

    // 테스트용 페이로드 클래스
    private static class TestPayload {
        public String value;

        public TestPayload() {}

        public TestPayload(String value) {
            this.value = value;
        }
    }
}
