package com.samsa.node.inout;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WriteFileNode extends InOutNode {
    private final Path filePath;
    private final boolean append;

    /**
     * 파일 쓰기 노드를 생성합니다.
     *
     * @param filePath 작성할 파일 경로
     * @param append   파일 추가 모드 여부
     */
    public WriteFileNode(Path filePath, boolean append) {
        if (filePath == null) {
            log.error("파일 경로가 null입니다");
            throw new IllegalArgumentException("파일 경로는 null일 수 없습니다");
        }
        this.filePath = filePath;
        this.append = append;
    }

    /**
     * ID를 지정하여 파일 쓰기 노드를 생성합니다.
     *
     * @param id       노드 식별자
     * @param filePath 작성할 파일 경로
     * @param append   파일 추가 모드 여부
     */
    public WriteFileNode(UUID id, Path filePath, boolean append) {
        super(id);
        this.filePath = filePath;
        this.append = append;
    }

    @Override
    public void onMessage(Message message) {

        if (message == null) {
            log.error("메시지가 null입니다. NodeId: {}", getId());
            return;
        }
        
        Object payload = message.getPayload();

        if (payload == null) {
            log.error("페이로드가 null입니다. NodeId: {}, MessageId: {}", getId(), message.getId());
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), append))) {
            log.debug("파일 쓰기 시작. NodeId: {}, MessageId: {}, FilePath: {}", 
                getId(), message.getId(), filePath);
            
            writer.write(payload.toString());
            writer.newLine();
            writer.flush();
            
            log.info("파일 쓰기 완료. NodeId: {}, MessageId: {}, FilePath: {}", 
                getId(), message.getId(), filePath);
            
            // 상위 클래스의 onMessage 호출하여 다음 노드로 메시지 전달
            super.onMessage(message);
        } catch (IOException e) {
            log.error("파일 쓰기 실패. NodeId: {}, MessageId: {}, FilePath: {}", 
                getId(), message.getId(), filePath, e);
            throw new RuntimeException("파일 쓰기 중 오류가 발생했습니다", e);
        }
    }
}
