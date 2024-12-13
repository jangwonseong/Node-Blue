package com.samsa;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogParser {

    private String filePath;

    public LogParser(String filePath) {
        this.filePath = filePath;
    }

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

    private String extractLogLevel(String line) {
        String[] logLevels = {"DEBUG", "INFO", "WARN", "ERROR"};
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
