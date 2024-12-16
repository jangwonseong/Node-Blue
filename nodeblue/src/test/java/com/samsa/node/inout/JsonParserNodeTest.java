package com.samsa.node.inout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.samsa.core.InPort;
import com.samsa.core.Message;
import com.samsa.core.OutPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonParserNodeTest {

    private JsonParserNode jsonParserNode;
    private InPort inPort;
    private OutPort outPort;
    private final UUID nodeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        inPort = mock(InPort.class);
        outPort = mock(OutPort.class);
        jsonParserNode = new JsonParserNode(nodeId, inPort, outPort, TestData.class);
    }

    @Test
    void testJsonStringToObject() throws JsonProcessingException {
        String jsonString = "{\"name\":\"test\",\"value\":123}";
        Message inputMessage = new Message(jsonString);

        when(inPort.consume()).thenReturn(inputMessage);

        jsonParserNode.onMessage(inputMessage);

        verify(outPort, times(1)).propagate(argThat(message -> {
            TestData data = (TestData) message.getPayload();
            return "test".equals(data.getName()) && data.getValue() == 123;
        }));
    }

    @Test
    void testObjectToJsonString() throws JsonProcessingException {
        TestData data = new TestData("test", 123);
        Message inputMessage = new Message(data);

        when(inPort.consume()).thenReturn(inputMessage);

        jsonParserNode.onMessage(inputMessage);

        verify(outPort, times(1)).propagate(argThat(message -> {
            String payload = (String) message.getPayload();
            return payload.contains("\"name\":\"test\"") && payload.contains("\"value\":123");
        }));
    }

    @Test
    void testInvalidJsonThrowsException() {
        String invalidJson = "{invalid}";
        Message inputMessage = new Message(invalidJson);

        Exception exception =
                assertThrows(RuntimeException.class, () -> jsonParserNode.onMessage(inputMessage));
        assertTrue(exception.getMessage().contains("JSON 처리 중 오류 발생"));
    }

    // Helper class for testing
    static class TestData {
        private String name;
        private int value;

        public TestData() {}

        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }
}
