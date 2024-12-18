package com.samsa.node.inout;

import com.samsa.ModbusToMqttFlow;
import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ModbusToMqttNodeTest {

    private ModbusToMqttNode modbusToMqttNode;

    @BeforeEach
    void setUp() {
        ModbusToMqttFlow modbusToMqttFlow = new ModbusToMqttFlow();
    }

    @Test
    void testOnMessage() {
        
    }
}
