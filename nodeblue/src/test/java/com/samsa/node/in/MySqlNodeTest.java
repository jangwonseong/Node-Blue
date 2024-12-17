package com.samsa.node.in;

import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Mockito.*;

class MySqlNodeTest {

    private MySqlNode mySqlNode;
    private Connection mockConnection;
    private Statement mockStatement;

    @BeforeEach
    void setUp() throws SQLException {
        // MySQL 드라이버 로드
        DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());

        // Mockito 객체 준비
        mockConnection = mock(Connection.class);
        mockStatement = mock(Statement.class);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        mySqlNode = new MySqlNode("com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/testdb",
                "user", "password",
                "INSERT INTO test_table (id, message) VALUES (1, 'Test Message')");
    }

    @Test
    void testOnMessage() throws SQLException {
        // DriverManager를 직접 사용하여 Connection을 Mock
        Mockito.mockStatic(DriverManager.class);
        when(DriverManager.getConnection(anyString(), anyString(), anyString()))
                .thenReturn(mockConnection);

        // 메시지 처리 테스트
        Message message = new Message("Test Message");
        mySqlNode.onMessage(message);

        // SQL 실행 검증
        verify(mockStatement, times(1))
                .executeUpdate("INSERT INTO test_table (id, message) VALUES (1, 'Test Message')");
    }

    @Test
    void testConnectionFailure() {
        // 잘못된 드라이버와 URL을 가진 MySqlNode 생성
        mySqlNode = new MySqlNode("invalid.driver", "jdbc:invalid", "user", "password", "query");

        // 메시지 처리 중 예외 발생 테스트
        Message message = new Message("Test Message");
        mySqlNode.onMessage(message);

        // 로그 확인 또는 예외 검증 (이 부분은 구현에 따라 변경)
    }
}
