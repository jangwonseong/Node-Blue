package com.samsa;



import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.samsa.core.Message;
import com.samsa.node.inout.ReadFileNode;
import com.samsa.node.inout.WriteFileNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReadWrite {
    public static void main(String[] args) {
        Path filePath = Paths.get("./log/log2.log").toAbsolutePath();

        WriteFileNode writer = new WriteFileNode(filePath, true);
        ReadFileNode reader = new ReadFileNode(filePath, true);

        // 파일에 내용 쓰기
        Message writeMessage = new Message("This is a test message");
        writer.onMessage(writeMessage);

        // 파일 내용 읽기
        Message readMessage = new Message("");
        reader.onMessage(readMessage);

        // 읽은 내용 출력
        Object readContent = readMessage.getPayload();
        if (readContent instanceof List) {
            List<String> lines = (List<String>) readContent;
            log.info("Read file contents:");
            for (String line : lines) {
                log.info(line);
            }
        } else {
            log.info("Read content: {}", readContent);
        }
    }
}