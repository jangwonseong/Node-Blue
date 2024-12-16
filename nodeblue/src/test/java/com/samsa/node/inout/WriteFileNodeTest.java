package com.samsa.node.inout;

import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.UUID;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class WriteFileNodeTest {
    private WriteFileNode writeNode;
    private Path testFilePath;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        testFilePath = tempDir.resolve("test.txt");
        writeNode = new WriteFileNode(testFilePath, false);
    }

    @Test
    void testConstructorWithNullPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            new WriteFileNode(null, false);
        });
    }

    @Test
    void testConstructorWithCustomId() {
        UUID customId = UUID.randomUUID();
        WriteFileNode node = new WriteFileNode(customId, testFilePath, false);
        assertEquals(customId, node.getId());
    }

    @Test
    void testWriteMessageToFile() throws IOException {
        String testContent = "테스트 내용";
        Message message = new Message(testContent);
        
        writeNode.onMessage(message);
        
        String fileContent = Files.readString(testFilePath);
        assertTrue(fileContent.contains(testContent));
    }

    @Test
    void testAppendMode() throws IOException {
        WriteFileNode appendNode = new WriteFileNode(testFilePath, true);
        
        Message firstMessage = new Message("첫번째 줄");
        Message secondMessage = new Message("두번째 줄");
        
        appendNode.onMessage(firstMessage);
        appendNode.onMessage(secondMessage);
        
        String fileContent = Files.readString(testFilePath);
        assertTrue(fileContent.contains("첫번째 줄"));
        assertTrue(fileContent.contains("두번째 줄"));
    }

    @Test
    void testNullMessage() {
        assertDoesNotThrow(() -> writeNode.onMessage(null));
    }

    @Test
    void testNullPayload() {
        Message message = new Message("test");
        message.setPayload(null);
        assertDoesNotThrow(() -> writeNode.onMessage(message));
    }

    @Test
    void testInvalidFilePath() {
        WriteFileNode invalidNode = new WriteFileNode(Path.of("/invalid/path/test.txt"), false);
        Message message = new Message("test");
        
        assertThrows(RuntimeException.class, () -> 
            invalidNode.onMessage(message));
    }

    @Test
    void testObjectToStringConversion() throws IOException {
        Object testObject = new TestObject("test");
        Message message = new Message(testObject);
        
        writeNode.onMessage(message);
        
        String fileContent = Files.readString(testFilePath);
        assertTrue(fileContent.contains(testObject.toString()));
    }

    private static class TestObject {
        private final String value;
        
        TestObject(String value) {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return "TestObject{value='" + value + "'}";
        }
    }
}
