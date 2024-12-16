package com.samsa.node.inout;

import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReadFileNodeTest {
    private ReadFileNode readNode;
    private static final Path LOG_FILE_PATH = Path.of("log/log.log");

    @BeforeEach
    void setUp() {
        readNode = new ReadFileNode(LOG_FILE_PATH, true);
    }

    @Test
    void testConstructorWithNullPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ReadFileNode(null, true);
        });
    }

    @Test
    void testConstructorWithIdAndNullPath() {
        UUID id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> {
            new ReadFileNode(id, null, true);
        });
    }

    @Test
    void testReadAllLines() {
        Message message = new Message("");
        readNode.onMessage(message);
        
        Object payload = message.getPayload();
        assertTrue(payload instanceof List);
        List<String> lines = (List<String>) payload;
        assertFalse(lines.isEmpty());
    }

    @Test
    void testReadSingleLine() {
        ReadFileNode singleLineNode = new ReadFileNode(LOG_FILE_PATH, false);
        Message message = new Message("");
        singleLineNode.onMessage(message);
        
        Object payload = message.getPayload();
        assertTrue(payload instanceof String);
        assertFalse(((String) payload).isEmpty());
    }

    @Test
    void testNullMessage() {
        assertDoesNotThrow(() -> readNode.onMessage(null));
    }

    @Test
    void testCustomIdConstructor() {
        UUID customId = UUID.randomUUID();
        ReadFileNode customNode = new ReadFileNode(customId, LOG_FILE_PATH, true);
        assertEquals(customId, customNode.getId());
    }

    @Test
    void testInvalidFilePath() {
        ReadFileNode invalidNode = new ReadFileNode(Path.of("invalid/path.txt"), true);
        Message message = new Message("");
        
        assertThrows(RuntimeException.class, () -> 
            invalidNode.onMessage(message));
    }
}
