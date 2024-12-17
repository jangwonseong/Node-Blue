package com.samsa.node.in;

import java.util.HashMap;
import java.util.Map;

import com.samsa.core.Flow;
import com.samsa.core.Message;
import com.samsa.core.Pipe;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InfluxNodeTest {
    // InfluxDB 연결 정보
    private static final String INFLUX_URL = "http://192.168.71.221:8086";
    private static final String INFLUX_TOKEN = "7BuWmxpqHzyY7dN6jZiysF8SvdXEdnccAq9Uq23Mr7uL9NGc2tkZKrIkiTmaD4QtXY7NxaJSG8hbqs_kvu1kZQ==";
    private static final String INFLUX_ORG = "seong";
    private static final String INFLUX_BUCKET = "nhnacademy";

    public static void main(String[] args) {
        try {
            // InfluxNode 설정
            InfluxNode influxNode = new InfluxNode(
                    INFLUX_URL,
                    INFLUX_TOKEN,
                    INFLUX_ORG,
                    INFLUX_BUCKET);

            // measurement, tag, field 설정
            influxNode.setMeasurement("power");
            influxNode.addTagKey("location"); // 메타데이터의 location을 태그로 사용
            influxNode.addTagKey("deviceId"); // 메타데이터의 deviceId를 태그로 사용
            influxNode.addFieldKey("voltage"); // 페이로드의 voltage를 필드로 사용
            influxNode.addFieldKey("current"); // 페이로드의 current를 필드로 사용
            influxNode.addFieldKey("power"); // 페이로드의 power를 필드로 사용

            // Flow에 노드 추가
            Flow flow = new Flow();
            flow.addNode(influxNode);

            // 입력 파이프 설정
            Pipe inputPipe = new Pipe(10);
            influxNode.getPort().addPipe(inputPipe);

            // Flow 실행
            Thread flowThread = new Thread(flow);
            flowThread.start();

            // 테스트 데이터 전송
            for (int i = 0; i < 100; i++) {
                // 페이로드 데이터 생성
                Map<String, Object> payload = new HashMap<>();
                payload.put("voltage", 220.0 + i);
                payload.put("current", 10.0 + i);
                payload.put("power", (220.0 + i) * (10.0 + i));

                // 메타데이터 생성
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("location", "Room-" + (i + 1));
                metadata.put("deviceId", "Device-" + (i + 1));

                // 메시지 생성 및 전송
                Message message = new Message(payload, metadata);
                inputPipe.offer(message);

                log.info("Sent message {}: {}", i + 1, message);

                // 메시지 간 간격
                Thread.sleep(2000);
            }

            // 모든 데이터가 처리될 때까지 대기
            Thread.sleep(5000);

            // 종료
            influxNode.close();
            System.exit(0);

        } catch (Exception e) {
            log.error("Error in test: ", e);
        }
    }
}