package com.samsa.inout;

import java.util.HashMap;
import java.util.Map;

import com.samsa.core.Flow;
import com.samsa.core.Message;
import com.samsa.core.Pipe;
import com.samsa.node.in.DebugNode;
import com.samsa.node.inout.ChangeNode;

public class ChangeNodeTest {
    public static void main(String[] args) {
        // Flow 생성
        Flow flow = new Flow();

        // ChangeNode 생성 및 설정
        ChangeNode changeNode = new ChangeNode();
        changeNode.setMetadataKey("status");
        changeNode.setMetadataValue("processed");

        // 디버그 노드 생성 (결과 확인용)
        DebugNode debugNode = new DebugNode();
        debugNode.setIncludeMetadata(true);
        debugNode.setLogLevel("INFO");

        // 파이프로 노드 연결
        Pipe pipe = new Pipe(10);
        changeNode.getOutPort().addPipe(pipe);
        debugNode.getPort().addPipe(pipe);

        // Flow에 노드 추가
        flow.addNode(changeNode);
        flow.addNode(debugNode);

        // Flow 실행
        Thread flowThread = new Thread(flow);
        flowThread.start();

        // 테스트 메시지 전송
        try {
            // 입력용 파이프 생성
            Pipe inputPipe = new Pipe(10);
            changeNode.getInPort().addPipe(inputPipe);

            // 테스트 케이스 1: 기존 메타데이터가 없는 경우
            Map<String, Object> metadata1 = new HashMap<>();
            Message message1 = new Message("Test message 1", metadata1);
            inputPipe.offer(message1);

            Thread.sleep(2000);

            // 테스트 케이스 2: 기존 메타데이터가 있는 경우
            Map<String, Object> metadata2 = new HashMap<>();
            metadata2.put("status", "new");
            Message message2 = new Message("Test message 2", metadata2);
            inputPipe.offer(message2);

            Thread.sleep(2000);

            // 테스트 케이스 3: 다른 메타데이터와 함께 있는 경우
            Map<String, Object> metadata3 = new HashMap<>();
            metadata3.put("status", "pending");
            metadata3.put("priority", "high");
            Message message3 = new Message("Test message 3", metadata3);
            inputPipe.offer(message3);

            // 충분한 실행 시간 대기
            Thread.sleep(5000);
            System.exit(0);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}