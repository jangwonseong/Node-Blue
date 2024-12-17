package com.samsa.node.out;

import com.samsa.core.Message;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ModbusRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModbusNodeTest {

    private ModbusNode modbusNode;
    private ModbusMaster mockMaster;
    private ModbusFactory mockFactory;

    @BeforeEach
    void setUp() {
        mockFactory = mock(ModbusFactory.class);
        mockMaster = mock(ModbusMaster.class);
        when(mockFactory.createTcpMaster(any(), anyBoolean())).thenReturn(mockMaster);

        modbusNode = new ModbusNode("localhost", 502, 1, 0, 2);
    }

    @Test
    void testCreateMessageSuccess() throws ModbusInitException, ModbusTransportException {
        ReadHoldingRegistersResponse mockResponse = mock(ReadHoldingRegistersResponse.class);
        when(mockResponse.isException()).thenReturn(false);
        when(mockResponse.getShortData()).thenReturn(new short[] {1, 2});
        when(mockMaster.send((ModbusRequest)any())).thenReturn(mockResponse);

        Message message = modbusNode.createMessage();

        assertNotNull(message);
        assertArrayEquals(new short[] {1, 2}, (short[]) message.getPayload());
    }

    @Test
    void testCreateMessageException() throws ModbusInitException, ModbusTransportException {
        when(mockMaster.send((ModbusRequest)any())).thenThrow(new ModbusTransportException("Test Exception"));

        Message message = modbusNode.createMessage();

        assertNull(message);
    } 
}
