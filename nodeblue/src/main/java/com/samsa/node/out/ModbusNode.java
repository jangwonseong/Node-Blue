package com.samsa.node.out;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samsa.annotation.NodeType;
import com.samsa.core.Message;
import com.samsa.core.node.OutNode;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * ModbusNode 클래스는 Modbus 장치로부터 데이터를 읽어와 메시지를 생성하는 노드입니다.
 */
@NodeType("ModbusNode")
@Slf4j
public class ModbusNode extends OutNode {

    private static final boolean DEFAULT_KEEPALIVE = false;

    private final String host;
    private final int port;
    private final boolean keepAlive;
    private final int slaveId;
    private final int startOffset;
    private final int offsetInterval;
    private final int maxOffset;
    private final int numOfRegisters;

    private int currentOffset;

    /**
     * ModbusNode 생성자입니다.
     *
     * @param host Modbus 장치의 호스트 주소
     * @param port Modbus 장치의 포트 번호
     * @param slaveId Modbus 슬레이브 ID
     * @param startOffset 읽기 시작할 레지스터 오프셋
     * @param numOfRegisters 읽을 레지스터 수
     */
    @JsonCreator
    public ModbusNode(@JsonProperty("host") String host, @JsonProperty("port") int port,
            @JsonProperty("slaveId") int slaveId, @JsonProperty("startOffset") int startOffset,
            @JsonProperty("offsetInterval") int offsetInterval,
            @JsonProperty("maxOffset") int maxOffset,
            @JsonProperty("numOfRegisters") int numOfRegisters) {
        this(host, port, slaveId, startOffset, offsetInterval, maxOffset, numOfRegisters,
                DEFAULT_KEEPALIVE);
    }

    /**
     * ModbusNode 생성자입니다.
     *
     * @param host Modbus 장치의 호스트 주소
     * @param port Modbus 장치의 포트 번호
     * @param slaveId Modbus 슬레이브 ID
     * @param startOffset 읽기 시작할 레지스터 오프셋
     * @param offsetInterval 시작 오프셋부터 마지막 오프셋까지 간격
     * @param maxOffset 마지막 레지스터 오프셋
     * @param numOfRegisters 읽을 레지스터 수
     * @param keepAlive Modbus 연결의 keep-alive 설정
     */
    public ModbusNode(String host, int port, int slaveId, int startOffset, int offsetInterval,
            int maxOffset, int numOfRegisters, boolean keepAlive) {
        this.host = host;
        this.port = port;
        this.keepAlive = keepAlive;
        this.slaveId = slaveId;
        this.startOffset = startOffset;
        this.offsetInterval = offsetInterval;
        this.maxOffset = maxOffset;
        this.numOfRegisters = numOfRegisters;
        this.currentOffset = startOffset;
    }

    /**
     * Modbus 장치에서 데이터를 읽어 메시지를 생성합니다.
     *
     * @return 생성된 메시지, 오류 발생 시 null 반환
     */
    @Override
    protected Message createMessage() {
        // Modbus 연결 매개변수 설정
        IpParameters params = new IpParameters();
        params.setHost(host);
        params.setPort(port);

        // Modbus 마스터 생성
        ModbusMaster master = new ModbusFactory().createTcpMaster(params, keepAlive);
        try {
            master.init();
            log.info("Modbus 마스터가 성공적으로 초기화되었습니다: {}:{}, keepAlive={}", host, port, keepAlive);
        } catch (ModbusInitException e) {
            log.error("Modbus 마스터 초기화 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }

        // 데이터 읽기 및 메시지 생성
        try {
            ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(slaveId,
                    currentOffset * offsetInterval, numOfRegisters);
            ReadHoldingRegistersResponse response =
                    (ReadHoldingRegistersResponse) master.send(request);

            if (response == null || response.isException()) {
                log.error("Modbus 응답에 오류가 있습니다. 예외 코드: {}",
                        response != null ? response.getExceptionCode() : "null");
                return null;
            }

            log.info("Modbus에서 {}개의 레지스터를 성공적으로 읽어왔습니다.", numOfRegisters);

            // 응답 데이터를 Message로 변환하여 반환
            Map<String, Object> payload = new HashMap<>();
            payload.put("offset", currentOffset);
            payload.put("data", response.getShortData());
            return new Message(payload);
        } catch (ModbusTransportException e) {
            log.error("Modbus 전송 오류 발생: {}", e.getMessage(), e);
            return null;
        } finally {
            updateOffset();
            cleanUp(master);
        }
    }

    /**
     * 오프셋을 업데이트합니다.
     */
    private void updateOffset() {
        log.info("currentOffset : {}, offsetInterval : {}, maxOffset : {}", currentOffset,
                offsetInterval, maxOffset);
        if (currentOffset * offsetInterval >= maxOffset) {
            currentOffset = startOffset; // 오프셋 리셋
        } else {
            currentOffset++; // 오프셋 증가
        }
    }

    /**
     * Modbus 마스터 객체를 종료합니다.
     *
     * @param master ModbusMaster 객체
     */
    private void cleanUp(ModbusMaster master) {
        try {
            master.destroy();
            log.info("Modbus 마스터가 종료되었습니다.");
        } catch (Exception e) {
            log.error("Modbus 마스터 종료 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
