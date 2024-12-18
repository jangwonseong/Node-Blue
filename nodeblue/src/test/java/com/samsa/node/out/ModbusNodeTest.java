package com.samsa.node.out;

import com.samsa.core.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class ModbusNodeTest {
    private ModbusNode modbusNode;
    private static final String TEST_HOST = "192.168.70.203";
    private static final int TEST_PORT = 502;

    @BeforeEach
    void setUp() {
        modbusNode = new ModbusNode(TEST_HOST, TEST_PORT, 1, 0, 2);
    }

    @Test
    void testConstructorWithDefaultKeepAlive() {
        ModbusNode node = new ModbusNode(TEST_HOST, TEST_PORT, 1, 0, 2);
        assertNotNull(node);
    }

    @Test
    void testConstructorWithCustomKeepAlive() {
        ModbusNode node = new ModbusNode(TEST_HOST, TEST_PORT, 1, 0, 2, true);
        assertNotNull(node);
    }

    @Test
    void testInvalidHostConnection() {
        ModbusNode invalidNode = new ModbusNode("invalid-host", TEST_PORT, 1, 0, 2);
        Message message = invalidNode.createMessage();
        assertNull(message);
    }

    @Test
    void testInvalidPortConnection() {
        ModbusNode invalidNode = new ModbusNode(TEST_HOST, 0, 1, 0, 2);
        Message message = invalidNode.createMessage();
        assertNull(message);
    }

    @Test
    void testInvalidSlaveId() {
        ModbusNode invalidNode = new ModbusNode(TEST_HOST, TEST_PORT, -1, 0, 2);
        Message message = invalidNode.createMessage();
        assertNull(message);
    }

    @Test
    void testValidConnection() {
        Message message = modbusNode.createMessage();
        if (message != null) {
            assertNotNull(message.getPayload());
            assertTrue(message.getPayload() instanceof Short);
        }
    }

    @Test
    void testReadMultipleRegisters() {
        ModbusNode multiNode = new ModbusNode(TEST_HOST, TEST_PORT, 1, 0, 5);
        Message message = multiNode.createMessage();
        if (message != null) {
            assertNotNull(message.getPayload());
            assertTrue(message.getPayload() instanceof Short);
        }
    }
}
