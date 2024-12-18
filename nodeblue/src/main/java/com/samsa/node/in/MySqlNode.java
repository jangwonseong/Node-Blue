package com.samsa.node.in;

import java.sql.*;
import java.util.Map;
import com.samsa.core.Message;
import com.samsa.core.node.InNode;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * {@code MySqlNode} 클래스는 MySQL 데이터베이스와 상호작용하여 메시지를 처리하는 노드입니다.
 */
@Slf4j
public class MySqlNode extends InNode {

    private static HikariDataSource dataSource;
    private final String query;

    /**
     * 새로운 {@code MySqlNode} 인스턴스를 생성합니다.
     *
     * @param driver JDBC 드라이버 클래스 이름
     * @param url 데이터베이스 URL
     * @param userId 데이터베이스 사용자 ID
     * @param userPw 데이터베이스 사용자 비밀번호
     * @param query 실행할 SQL 쿼리
     */
    public MySqlNode(String driver, String url, String userId, String userPw, String query) {
        super();
        this.query = query;

        // HikariCP 설정
        if (dataSource == null) {
            synchronized (MySqlNode.class) {
                if (dataSource == null) {
                    HikariConfig config = new HikariConfig();
                    config.setDriverClassName(driver);
                    config.setJdbcUrl(url);
                    config.setUsername(userId);
                    config.setPassword(userPw);

                    // 최적화된 풀 설정
                    config.setMaximumPoolSize(10);
                    config.setMinimumIdle(2);
                    config.setIdleTimeout(30000);
                    config.setConnectionTimeout(30000);
                    config.setMaxLifetime(1800000);

                    dataSource = new HikariDataSource(config);
                    log.info("HikariCP DataSource가 초기화되었습니다.");
                }
            }
        }
    }

    /**
     * 수신된 메시지를 처리합니다.
     *
     * @param message 처리할 수신 메시지
     */
    @Override
    protected void onMessage(Message message) {
        if (message.getPayload() instanceof Map) {
            try (Connection connection = dataSource.getConnection()) {
                log.info("풀에서 데이터베이스 연결을 성공적으로 가져왔습니다.");

                if (query.trim().toLowerCase().startsWith("insert")) {
                    doInsert((Map<String, Object>) message.getPayload(), connection);
                } else {
                    log.warn("지원되지 않는 쿼리 유형입니다. 현재는 INSERT 쿼리만 지원됩니다.");
                }
            } catch (SQLException e) {
                log.error("데이터베이스 작업 중 오류 발생: {}", e.getMessage(), e);
            }
        } else {
            log.warn("Payload가 Map 타입이 아닙니다.");
        }
    }

    /**
     * 동적 INSERT 쿼리를 실행합니다.
     *
     * @param columnMap 삽입 작업을 위한 컬럼-값 쌍을 포함한 맵
     * @param connection 사용할 데이터베이스 연결
     * @throws SQLException SQL 실행 중 오류가 발생한 경우
     */
    private void doInsert(Map<String, Object> columnMap, Connection connection)
            throws SQLException {
        // 동적 SQL 쿼리 생성
        StringBuilder sql = new StringBuilder(query);
        StringBuilder placeholders = new StringBuilder();
        int index = 1;

        for (String column : columnMap.keySet()) {
            sql.append(column);
            placeholders.append("?");

            if (index < columnMap.size()) {
                sql.append(", ");
                placeholders.append(", ");
            }
            index++;
        }

        sql.append(") VALUES (").append(placeholders).append(")");

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
            // PreparedStatement에 값을 바인딩
            index = 1;
            for (Object value : columnMap.values()) {
                if (value instanceof String) {
                    preparedStatement.setString(index, (String) value);
                } else if (value instanceof Double) {
                    preparedStatement.setDouble(index, (Double) value);
                } else if (value instanceof Integer) {
                    preparedStatement.setInt(index, (Integer) value);
                } else {
                    preparedStatement.setObject(index, value);
                }
                index++;
            }

            // INSERT 쿼리 실행
            int rowsAffected = preparedStatement.executeUpdate();
            log.info("INSERT 쿼리가 성공적으로 실행되었습니다. 영향을 받은 행 수: {}", rowsAffected);
        } catch (SQLException e) {
            log.error("INSERT 쿼리 실행 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 애플리케이션 종료 시 데이터베이스 연결 풀을 종료합니다.
     */
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            log.info("HikariCP DataSource가 종료되었습니다.");
        }
    }
}
