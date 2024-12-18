package com.samsa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LogParser 클래스는 로그 파일을 파싱하여 로그 레벨별로 메시지를 분류하는 기능을 제공합니다.
 * 지원하는 로그 레벨은 DEBUG, INFO, WARN, ERROR입니다.
 * 
 * 이 클래스는 로그 파일을 읽어서 각 로그 레벨별로 메시지를 분류하고,
 * 분류된 결과를 맵 형태로 제공합니다.
 *
 * @author samsa
 * @version 1.0
 */
public class LogParser {

    /** 파싱할 로그 파일의 경로 */
    private String filePath;

    /**
     * LogParser 객체를 생성합니다.
     *
     * @param filePath 파싱할 로그 파일의 경로
     */
    public LogParser(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 로그 파일을 파싱하여 로그 레벨별로 메시지를 분류합니다.
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
            System.err.println("Error reading the log file : " + e.getMessage());
        }
        return logsByLevel;
    }

    /**
     * 주어진 로그 라인에서 로그 레벨을 추출합니다.
     *
     * @param line 파싱할 로그 라인
     * @return 로그 레벨 문자열, 로그 레벨이 없는 경우 null
     */
    private String extractLogLevel(String line) {
        String[] logLevels = { "DEBUG", "INFO", "WARN", "ERROR" };
        for (String level : logLevels) {
            if (line.contains(" " + level + " ")) {
                return level;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String logFilePath = "./log/log.log";
        LogParser parser = new LogParser(logFilePath);

        Map<String, List<String>> logsByLevel = parser.getLogsByLevel();

        for (Map.Entry<String, List<String>> entry : logsByLevel.entrySet()) {
            System.out.println("Log Level: " + entry.getKey());
            for (String log : entry.getValue()) {
                System.out.println("  - " + log);
            }
        }
    }
}
