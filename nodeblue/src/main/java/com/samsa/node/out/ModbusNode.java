package com.samsa.node.out;

import java.util.HashMap;
import java.util.Map;

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
@Slf4j
public class ModbusNode extends OutNode {

    /**
     * 기본 keep-alive 설정 값입니다.
     */
    private static final boolean DEFAULT_KEEPALIVE = false;

    /**
     * Modbus 장치의 호스트 주소입니다.
     */
    private final String host;

    /**
     * Modbus 장치의 포트 번호입니다.
     */
    private final int port;

    /**
     * Modbus 연결의 keep-alive 설정입니다.
     */
    private final boolean keepAlive;

    /**
     * Modbus 슬레이브 ID입니다.
     */
    private final int slaveId;

    /**
     * Modbus 레지스터 시작 오프셋입니다.
     */
    private final int startOffset;

    /**
     * 읽을 Modbus 레지스터 수입니다.
     */
    private final int numOfRegisters;

    /**
     * ModbusNode 생성자입니다.
     *
     * @param host           Modbus 장치의 호스트 주소
     * @param port           Modbus 장치의 포트 번호
     * @param slaveId        Modbus 슬레이브 ID
     * @param startOffset    읽기 시작할 레지스터 오프셋
     * @param numOfRegisters 읽을 레지스터 수
     */
    public ModbusNode(String host, int port, int slaveId, int startOffset, int numOfRegisters) {
        this(host, port, slaveId, startOffset, numOfRegisters, DEFAULT_KEEPALIVE);
    }

    /**
     * ModbusNode 생성자입니다.
     *
     * @param host           Modbus 장치의 호스트 주소
     * @param port           Modbus 장치의 포트 번호
     * @param slaveId        Modbus 슬레이브 ID
     * @param startOffset    읽기 시작할 레지스터 오프셋
     * @param numOfRegisters 읽을 레지스터 수
     * @param keepAlive      Modbus 연결의 keep-alive 설정
     */
    public ModbusNode(String host, int port, int slaveId, int startOffset, int numOfRegisters,
            boolean keepAlive) {
        this.host = host;
        this.port = port;
        this.keepAlive = keepAlive;
        this.slaveId = slaveId;
        this.startOffset = startOffset;
        this.numOfRegisters = numOfRegisters;
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
            log.error("Modbus 마스터 초기화 중 에러 발생: {}", e.getMessage(), e);
            return null;
        }

        try {
            // Modbus 요청 생성
            ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(slaveId, startOffset, numOfRegisters);

            // Modbus 응답 처리
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(request);


            if (response == null || response.isException()) {
                log.error("Modbus 응답에 오류가 있습니다. 예외 코드: {}", response.getExceptionCode());
                return null;
            }

            log.info("Modbus에서 {}개의 레지스터를 성공적으로 읽어왔습니다.", numOfRegisters);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("slaveId", slaveId);
            
            // short[]로 직접 전달
            return new Message(response.getShortData(), metadata);
        } catch (ModbusTransportException e) {
            log.error("Modbus 전송 오류 발생: {}", e.getMessage(), e);
            return null;
        } finally {
            master.destroy();
            log.info("Modbus 마스터가 종료되었습니다.");
        }
    }
}
