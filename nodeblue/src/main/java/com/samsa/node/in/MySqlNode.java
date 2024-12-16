package com.samsa.node.in;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import com.samsa.core.Message;
import com.samsa.core.node.InNode;
import lombok.extern.slf4j.Slf4j;

/**
 * MySqlNode 클래스는 MySQL 데이터베이스와 상호작용하여 메시지를 처리하는 노드입니다.
 */
@Slf4j
public class MySqlNode extends InNode {

    /**
     * JDBC 드라이버 클래스 이름입니다.
     */
    private final String driver;

    /**
     * 데이터베이스 URL입니다.
     */
    private final String url;

    /**
     * 데이터베이스 사용자 ID입니다.
     */
    private final String userId;

    /**
     * 데이터베이스 사용자 비밀번호입니다.
     */
    private final String userPw;

    /**
     * 실행할 SQL 쿼리입니다.
     */
    private final String query;

    /**
     * 데이터베이스 연결 객체입니다.
     */
    private Connection connection = null;

    /**
     * MySqlNode 생성자입니다.
     *
     * @param driver JDBC 드라이버 클래스 이름
     * @param url 데이터베이스 URL
     * @param userId 데이터베이스 사용자 ID
     * @param userPw 데이터베이스 사용자 비밀번호
     * @param query 실행할 SQL 쿼리
     */
    public MySqlNode(String driver, String url, String userId, String userPw, String query) {
        super();
        this.driver = driver;
        this.url = url;
        this.userId = userId;
        this.userPw = userPw;
        this.query = query;
    }

    /**
     * 메시지를 처리하여 MySQL 데이터베이스에 저장합니다.
     *
     * @param message 처리할 메시지
     */
    @Override
    protected void onMessage(Message message) {
        try {
            // JDBC 드라이버 로드
            Class.forName(driver);
            log.info("JDBC 드라이버가 로드되었습니다: {}", driver);

            // 데이터베이스 연결 생성
            connection = DriverManager.getConnection(url, userId, userPw);
            log.info("데이터베이스에 성공적으로 연결되었습니다: {}", url);

            // SQL 쿼리 실행
            if (query.trim().toLowerCase().startsWith("insert")) {
                doInsert();
            } else {
                log.warn("지원되지 않는 쿼리입니다. 현재는 INSERT 쿼리만 지원됩니다.");
            }
        } catch (ClassNotFoundException e) {
            log.error("JDBC 드라이버를 찾을 수 없습니다: {}", e.getMessage(), e);
        } catch (SQLException e) {
            log.error("데이터베이스 작업 중 오류가 발생했습니다: {}", e.getMessage(), e);
        } finally {
            // 연결 종료
            closeConnection();
        }
    }

    /**
     * INSERT 쿼리를 실행합니다.
     *
     * @throws SQLException SQL 실행 중 오류가 발생한 경우
     */
    private void doInsert() throws SQLException {
        if (Objects.isNull(connection)) {
            log.error("데이터베이스 연결이 초기화되지 않았습니다. 연결을 확인하세요.");
            throw new SQLException("데이터베이스 연결이 null입니다.");
        }

        try (Statement st = connection.createStatement()) {
            st.executeUpdate(query);
            log.info("INSERT 쿼리가 성공적으로 실행되었습니다: {}", query);
        }
    }

    /**
     * 데이터베이스 연결을 종료합니다.
     */
    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                log.info("데이터베이스 연결이 종료되었습니다.");
            } catch (SQLException e) {
                log.error("데이터베이스 연결 종료 중 오류가 발생했습니다: {}", e.getMessage(), e);
            }
        }
    }
}
