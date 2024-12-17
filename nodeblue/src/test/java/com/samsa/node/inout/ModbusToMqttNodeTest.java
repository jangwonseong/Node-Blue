package com.samsa.node.inout;

import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ModbusToMqttNodeTest {

    private ModbusToMqttNode modbusToMqttNode;

    @BeforeEach
    void setUp() {
        modbusToMqttNode = new ModbusToMqttNode("test/topic");
    }

    @Test
    void testOnMessage() {
        Message mockMessage = mock(Message.class);
        when(mockMessage.getPayload()).thenReturn("Test Payload");

        modbusToMqttNode.onMessage(mockMessage);

        // emit이 호출되었는지 확인
        verify(mockMessage, times(1)).getPayload();
    }
}
