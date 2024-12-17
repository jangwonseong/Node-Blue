// DebugNodeTest.java
package com.samsa.node.in;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.samsa.core.Flow;
import com.samsa.core.Message;
import com.samsa.core.Pipe;
import com.samsa.node.in.DebugNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DebugFlowTest {
    public static void main(String[] args) {
        // Flow 생성
        Flow flow = new Flow();

        // 디버그 노드들 생성 (다른 로그 레벨로 설정)
        DebugNode debugNodeInfo = createDebugNode("INFO");
        DebugNode debugNodeWarn = createDebugNode("WARN");
        DebugNode debugNodeError = createDebugNode("ERROR");

        // 파이프 생성 및 연결
        Pipe pipe1 = new Pipe(10);
        Pipe pipe2 = new Pipe(10);
        Pipe pipe3 = new Pipe(10);

        // 각 디버그 노드에 파이프 연결
        debugNodeInfo.getPort().addPipe(pipe1);
        debugNodeWarn.getPort().addPipe(pipe2);
        debugNodeError.getPort().addPipe(pipe3);

        // Flow에 노드들 추가
        flow.addNode(debugNodeInfo);
        flow.addNode(debugNodeWarn);
        flow.addNode(debugNodeError);

        // Flow 실행
        Thread flowThread = new Thread(flow);
        flowThread.start();

        // 테스트 메시지 전송
        sendTestMessages(pipe1, pipe2, pipe3);
    }

    private static DebugNode createDebugNode(String logLevel) {
        DebugNode node = new DebugNode(UUID.randomUUID());
        node.setLogLevel(logLevel);
        node.setIncludeMetadata(true);
        return node;
    }

    private static void sendTestMessages(Pipe... pipes) {
        int messageCount = 0;
        try {
            while (messageCount < 5) { // 5개의 테스트 메시지 전송
                messageCount++;

                // 테스트 메시지 생성
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("timestamp", System.currentTimeMillis());
                metadata.put("messageNumber", messageCount);

                String payload = String.format("Test message #%d", messageCount);
                Message message = new Message(payload, metadata);

                // 모든 파이프에 메시지 전송
                for (Pipe pipe : pipes) {
                    pipe.offer(message);
                    log.debug("Sent message to pipe: {}", message);
                }

                Thread.sleep(2000); // 2초 간격으로 메시지 전송
            }

            // 충분한 처리 시간을 준 후 종료
            Thread.sleep(5000);
            System.exit(0);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}