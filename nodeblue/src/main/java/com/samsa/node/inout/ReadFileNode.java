package com.samsa.node.inout;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReadFileNode extends InOutNode {
    private final Path filePath;
    private final boolean readAllLines;

    public ReadFileNode(Path filePath, boolean readAllLines) {
        if (filePath == null) {
            log.error("파일 경로가 null입니다");
            throw new IllegalArgumentException("파일 경로는 null일 수 없습니다");
        }
        this.filePath = filePath;
        this.readAllLines = readAllLines;
    }

    public ReadFileNode(UUID id, Path filePath, boolean readAllLines) {
        super(id);
        if (filePath == null) {
            log.error("파일 경로가 null입니다. NodeId: {}", id);
            throw new IllegalArgumentException("파일 경로는 null일 수 없습니다");
        }
        this.filePath = filePath;
        this.readAllLines = readAllLines;
    }

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            log.error("메시지가 null입니다. NodeId: {}", getId());
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            if (readAllLines) {
                List<String> lines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                if (lines.isEmpty()) {
                    log.warn("파일이 비어있습니다. NodeId: {}, FilePath: {}", getId(), filePath);
                    return;
                }
                message.setPayload(lines);
            } else {
                String line = reader.readLine();
                if (line != null) {
                    message.setPayload(line);
                } else {
                    log.warn("파일이 비어있습니다. NodeId: {}, FilePath: {}", getId(), filePath);
                    return;
                }
            }
            
            log.debug("파일 읽기 완료. NodeId: {}, MessageId: {}, FilePath: {}", 
                getId(), message.getId(), filePath);
            
            super.onMessage(message);
            
        } catch (IOException e) {
            log.error("파일 읽기 실패. NodeId: {}, MessageId: {}, FilePath: {}", 
                getId(), message.getId(), filePath, e);
            throw new RuntimeException("파일 읽기 중 오류가 발생했습니다", e);
        }
    }
}
