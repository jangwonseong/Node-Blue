// package com.samsa.node.in;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.UUID;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import org.slf4j.LoggerFactory;

// import com.samsa.core.Message;
// import com.samsa.core.Node;

// import ch.qos.logback.classic.Level;
// import ch.qos.logback.classic.Logger;
// import ch.qos.logback.classic.spi.ILoggingEvent;
// import ch.qos.logback.core.read.ListAppender;

// @ExtendWith(MockitoExtension.class)
// class DebugNodeTest {

//     private DebugNode debugNode;
//     private ListAppender<ILoggingEvent> listAppender;
//     private Logger debugNodeLogger;

//     @BeforeEach
//     void setUp() {
//         debugNode = new DebugNode();

//         // Logback 로거 설정
//         debugNodeLogger = (Logger) LoggerFactory.getLogger(DebugNode.class);
//         listAppender = new ListAppender<>();
//         listAppender.start();
//         debugNodeLogger.addAppender(listAppender);
//     }

//     @Nested
//     @DisplayName("Constructor Tests")
//     class ConstructorTests {

//         @Test
//         @DisplayName("기본 생성자는 랜덤 UUID를 생성해야 함")
//         void defaultConstructorShouldCreateRandomUUID() {
//             DebugNode node1 = new DebugNode();
//             DebugNode node2 = new DebugNode();

//             assertNotNull(node1.getId());
//             assertNotNull(node2.getId());
//             assertNotEquals(node1.getId(), node2.getId());
//         }
//     }

//     @Nested
//     @DisplayName("Message Handling Tests")
//     class MessageHandlingTests {

//         @Test
//         @DisplayName("정상적인 메시지 처리 시 로그가 기록되어야 함")
//         void shouldLogValidMessage() {
//             String payload = "Test payload";
//             Message message = new Message(payload);

//             debugNode.onMessage(message);

//             assertTrue(listAppender.list.stream()
//                     .anyMatch(event -> event.getFormattedMessage().contains(payload)));
//         }

//         @Test
//         @DisplayName("null 메시지 처리 시 경고 로그가 기록되어야 함")
//         void shouldLogWarningForNullMessage() {
//             debugNode.onMessage(null);

//             assertTrue(listAppender.list.stream()
//                     .anyMatch(event -> event.getLevel() == Level.WARN &&
//                             event.getFormattedMessage().contains("Received null message")));
//         }

//         @Test
//         @DisplayName("null 페이로드 메시지 처리 시 경고 로그가 기록되어야 함")
//         void shouldLogWarningForNullPayload() {
//             Message message = mock(Message.class);
//             when(message.getPayload()).thenReturn(null);

//             debugNode.onMessage(message);

//             assertTrue(listAppender.list.stream()
//                     .anyMatch(event -> event.getLevel() == Level.WARN &&
//                             event.getFormattedMessage().contains("null payload")));
//         }

//         @Test
//         @DisplayName("메타데이터 포함 설정 시 메타데이터가 로그에 포함되어야 함")
//         void shouldIncludeMetadataWhenEnabled() {
//             Map<String, Object> metadata = new HashMap<>();
//             metadata.put("key", "value");
//             Message message = new Message("test", metadata);

//             debugNode.setIncludeMetadata(true);
//             debugNode.onMessage(message);

//             assertTrue(listAppender.list.stream()
//                     .anyMatch(event -> event.getFormattedMessage().contains("Metadata") &&
//                             event.getFormattedMessage().contains("key") &&
//                             event.getFormattedMessage().contains("value")));
//         }
//     }

//     @Nested
//     @DisplayName("Log Level Tests")
//     class LogLevelTests {

//         @Test
//         @DisplayName("유효한 로그 레벨 설정이 동작해야 함")
//         void shouldAcceptValidLogLevels() {
//             assertDoesNotThrow(() -> {
//                 debugNode.setLogLevel("DEBUG");
//                 debugNode.setLogLevel("INFO");
//                 debugNode.setLogLevel("WARN");
//                 debugNode.setLogLevel("ERROR");
//             });
//         }

//         @Test
//         @DisplayName("잘못된 로그 레벨 설정 시 예외가 발생해야 함")
//         void shouldRejectInvalidLogLevels() {
//             assertAll(
//                     () -> assertThrows(IllegalArgumentException.class, () -> debugNode.setLogLevel(null)),
//                     () -> assertThrows(IllegalArgumentException.class, () -> debugNode.setLogLevel("")),
//                     () -> assertThrows(IllegalArgumentException.class, () -> debugNode.setLogLevel("INVALID")));
//         }

//         @Test
//         @DisplayName("설정된 로그 레벨에 따라 올바른 로깅이 수행되어야 함")
//         void shouldLogAtConfiguredLevel() {
//             Message message = new Message("test");

//             debugNode.setLogLevel("DEBUG");
//             debugNode.onMessage(message);
//             assertTrue(listAppender.list.stream()
//                     .anyMatch(event -> event.getLevel() == Level.DEBUG));

//             listAppender.list.clear();
//             debugNode.setLogLevel("INFO");
//             debugNode.onMessage(message);
//             assertTrue(listAppender.list.stream()
//                     .anyMatch(event -> event.getLevel() == Level.INFO));
//         }
//     }

//     @Nested
//     @DisplayName("Error Handling Tests")
//     class ErrorHandlingTests {

//         @Test
//         @DisplayName("에러 처리 시 상태가 ERROR로 변경되어야 함")
//         void shouldChangeStatusToErrorOnHandleError() {
//             Exception error = new RuntimeException("Test error");

//             debugNode.handleError(error);

//             assertEquals(Node.NodeStatus.ERROR, debugNode.getStatus());
//         }

//         @Test
//         @DisplayName("null 에러 처리 시 예외가 발생해야 함")
//         void shouldThrowExceptionOnNullError() {
//             assertThrows(IllegalArgumentException.class, () -> debugNode.handleError(null));
//         }

//         @Test
//         @DisplayName("메시지 처리 중 예외 발생 시 적절히 처리되어야 함")
//         void shouldHandleExceptionDuringMessageProcessing() {
//             Message message = mock(Message.class);
//             when(message.getPayload()).thenThrow(new RuntimeException("Test error"));

//             assertThrows(IllegalStateException.class, () -> debugNode.onMessage(message));
//             assertEquals(Node.NodeStatus.ERROR, debugNode.getStatus());
//         }
//     }

//     @Test
//     @DisplayName("로그 메시지가 올바른 형식을 가져야 함")
//     void logMessageShouldHaveCorrectFormat() {
//         String payload = "Test payload";
//         Message message = new Message(payload);

//         debugNode.onMessage(message);

//         ILoggingEvent lastEvent = listAppender.list.get(listAppender.list.size() - 1);
//         String logMessage = lastEvent.getFormattedMessage();

//         assertTrue(logMessage.contains("Node["));
//         assertTrue(logMessage.contains(debugNode.getId().toString()));
//         assertTrue(logMessage.contains(payload));
//     }
// }