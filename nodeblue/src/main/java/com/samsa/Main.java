package com.samsa;



import java.nio.file.Path;
import java.nio.file.Paths;

import com.samsa.core.Message;
import com.samsa.node.inout.ReadFileNode;
import com.samsa.node.inout.WriteFileNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {
        Path filePath = Paths.get("./log/log.log").toAbsolutePath();

        WriteFileNode writer = new WriteFileNode(filePath,true);
        ReadFileNode reader = new ReadFileNode(filePath, true);

        Message writeMessage = new Message("This is a test message");
        writer.onMessage(writeMessage);



    }
}
