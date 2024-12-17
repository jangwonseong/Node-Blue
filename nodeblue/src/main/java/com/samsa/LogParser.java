package com.samsa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 로그 파일을 파싱하고 분석하는 기능을 제공하는 클래스입니다.
 * 다양한 필터링 옵션과 검색 기능을 지원합니다.
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>로그 레벨별 메시지 분류</li>
 *   <li>시간 범위 기반 필터링</li>
 *   <li>로그 레벨 우선순위 기반 필터링</li>
 *   <li>키워드 기반 검색</li>
 * </ul>
 *
 * @author samsa
 * @version 2.0
 */
@Slf4j
public class LogParser {
    /** 파싱할 로그 파일의 경로 */
    private final String filePath;
    
    /** 로그 레벨의 우선순위를 정의하는 열거형 */
    public enum LogLevel {
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3);

        private final int priority;

        LogLevel(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    /**
     * LogParser 객체를 생성합니다.
     *
     * @param filePath 파싱할 로그 파일의 경로
     * @throws IllegalArgumentException filePath가 null인 경우
     */
    public LogParser(String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("파일 경로는 null일 수 없습니다");
        }
        this.filePath = filePath;
    }

    /**
     * 모든 로그를 레벨별로 분류하여 반환합니다.
     *
     * @return 로그 레벨을 키로 하고 해당 레벨의 로그 메시지 리스트를 값으로 하는 맵
     */
    public Map<String, List<String>> getLogsByLevel() {
        Map<String, List<String>> logsByLevel = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String logLevel = extractLogLevel(line);
                if (logLevel != null) {
                    logsByLevel.putIfAbsent(logLevel, new ArrayList<>());
                    logsByLevel.get(logLevel).add(line);
                }
            }
        } catch (IOException e) {
            log.error("로그 파일 읽기 오류: {}", e.getMessage());
        }
        return logsByLevel;
    }

    /**
     * 특정 로그 레벨의 메시지만 필터링하여 반환합니다.
     *
     * @param targetLevel 필터링할 로그 레벨
     * @return 지정된 레벨의 로그 메시지 리스트
     */
    public List<String> getLogsBySpecificLevel(LogLevel targetLevel) {
        List<String> filteredLogs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String logLevel = extractLogLevel(line);
                if (logLevel != null && LogLevel.valueOf(logLevel) == targetLevel) {
                    filteredLogs.add(line);
                }
            }
        } catch (IOException e) {
            log.error("로그 파일 읽기 오류: {}", e.getMessage());
        }
        return filteredLogs;
    }

    /**
     * 지정된 시간 범위 내의 로그 메시지를 반환합니다.
     *
     * @param start 시작 시간
     * @param end 종료 시간
     * @return 시간 범위 내의 로그 메시지 리스트
     */
    public List<String> getLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        List<String> filteredLogs = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LocalDateTime logTime = extractTimestamp(line, formatter);
                if (logTime != null && !logTime.isBefore(start) && !logTime.isAfter(end)) {
                    filteredLogs.add(line);
                }
            }
        } catch (IOException e) {
            log.error("로그 파일 읽기 오류: {}", e.getMessage());
        }
        return filteredLogs;
    }

    /**
     * 지정된 키워드가 포함된 로그 메시지를 검색합니다.
     *
     * @param keyword 검색할 키워드
     * @return 키워드가 포함된 로그 메시지 리스트
     */
    public List<String> searchLogsByKeyword(String keyword) {
        List<String> matchedLogs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(keyword)) {
                    matchedLogs.add(line);
                }
            }
        } catch (IOException e) {
            log.error("로그 파일 읽기 오류: {}", e.getMessage());
        }
        return matchedLogs;
    }

    private String extractLogLevel(String line) {
        for (LogLevel level : LogLevel.values()) {
            if (line.contains(" " + level.name() + " ")) {
                return level.name();
            }
        }
        return null;
    }

    private LocalDateTime extractTimestamp(String line, DateTimeFormatter formatter) {
        try {
            String timestamp = line.substring(0, 19);
            return LocalDateTime.parse(timestamp, formatter);
        } catch (Exception e) {
            return null;
        }
    }
}
