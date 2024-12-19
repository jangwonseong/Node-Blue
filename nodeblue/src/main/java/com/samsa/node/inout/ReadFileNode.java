package com.samsa.node.inout;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samsa.annotation.NodeType;
import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일을 읽어 메시지의 페이로드에 저장하는 노드 클래스입니다.
 * <p>
 * 지정된 파일 경로에서 데이터를 읽고 {@link Message} 객체의 페이로드에 저장합니다. 전체 라인을 읽거나 첫 번째 라인만 읽는 옵션을 제공하며, 파일이 비어있을 경우
 * 경고 로그를 출력하고 처리를 중단합니다.
 * </p>
 * 
 * <p>
 * 이 클래스는 {@link InOutNode}를 상속받아 구현되었습니다.
 * </p>
 * 
 * @see InOutNode
 * @see Message
 */
@NodeType("ReadFileNode")
@Slf4j
public class ReadFileNode extends InOutNode {
    private final Path filePath;
    private final boolean readAllLines;

    /**
     * 파일 읽기 노드의 생성자입니다.
     * 
     * @param filePath 읽을 파일의 경로 (null 불가)
     * @param readAllLines true일 경우 파일의 모든 라인을 읽어 페이로드에 저장합니다. false일 경우 첫 번째 라인만 읽습니다.
     * @throws IllegalArgumentException 파일 경로가 null일 경우 발생합니다.
     */
    public ReadFileNode(Path filePath, boolean readAllLines) {
        if (filePath == null) {
            log.error("파일 경로가 null입니다");
            throw new IllegalArgumentException("파일 경로는 null일 수 없습니다");
        }
        this.filePath = filePath;
        this.readAllLines = readAllLines;
    }

    /**
     * 파일 읽기 노드의 생성자입니다.
     * 
     * @param filePath 읽을 파일의 경로 문자열 (null 불가)
     * @param readAllLines true일 경우 파일의 모든 라인을 읽어 페이로드에 저장합니다. false일 경우 첫 번째 라인만 읽습니다.
     * @throws IllegalArgumentException 파일 경로가 null일 경우 발생합니다.
     */
    @JsonCreator
    public ReadFileNode(@JsonProperty("path") String filePath,
            @JsonProperty("readAllLines") boolean readAllLines) {
        if (filePath == null) {
            log.error("파일 경로가 null입니다");
            throw new IllegalArgumentException("파일 경로는 null일 수 없습니다");
        }
        this.filePath = Path.of(filePath);
        this.readAllLines = readAllLines;
    }

    /**
     * 파일 읽기 노드의 생성자입니다.
     * 
     * @param id 노드의 고유 ID
     * @param filePath 읽을 파일의 경로 (null 불가)
     * @param readAllLines true일 경우 파일의 모든 라인을 읽어 페이로드에 저장합니다. false일 경우 첫 번째 라인만 읽습니다.
     * @throws IllegalArgumentException 파일 경로가 null일 경우 발생합니다.
     */
    public ReadFileNode(UUID id, Path filePath, boolean readAllLines) {
        super(id);
        if (filePath == null) {
            log.error("파일 경로가 null입니다. NodeId: {}", id);
            throw new IllegalArgumentException("파일 경로는 null일 수 없습니다");
        }
        this.filePath = filePath;
        this.readAllLines = readAllLines;
    }

    /**
     * 파일 읽기 노드의 생성자입니다.
     * 
     * @param id 노드의 고유 ID
     * @param filePath 읽을 파일의 경로 문자열 (null 불가)
     * @param readAllLines true일 경우 파일의 모든 라인을 읽어 페이로드에 저장합니다. false일 경우 첫 번째 라인만 읽습니다.
     * @throws IllegalArgumentException 파일 경로가 null일 경우 발생합니다.
     */
    public ReadFileNode(UUID id, String filePath, boolean readAllLines) {
        super(id);
        if (filePath == null) {
            log.error("파일 경로가 null입니다. NodeId: {}", id);
            throw new IllegalArgumentException("파일 경로는 null일 수 없습니다");
        }
        this.filePath = Path.of(filePath);
        this.readAllLines = readAllLines;
    }

    /**
     * 메시지를 수신하고 파일의 내용을 읽어 메시지 페이로드에 설정합니다.
     * <p>
     * {@code readAllLines}가 true인 경우 파일의 모든 라인을 읽어 {@link List} 형태로 페이로드에 저장합니다. false인 경우 첫 번째 라인만
     * 읽습니다. 파일이 비어있을 경우 경고 로그를 출력하고 처리를 중단합니다.
     * </p>
     * 
     * @param message 수신된 메시지 객체
     * @throws RuntimeException 파일 읽기 중 {@link IOException}이 발생할 경우 예외가 던져집니다.
     */
    @Override
    public void onMessage(Message message) {
        if (message == null) {
            message = new Message("");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            if (readAllLines) {
                List<String> lines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                if (lines.isEmpty()) {
                    log.warn("파일이 비어있습니다. NodeId: {}, FilePath: {}", getId(), filePath);
                    return;
                }
                message.setPayload(lines);
            } else {
                String line = reader.readLine();
                if (line != null) {
                    message.setPayload(line);
                } else {
                    log.warn("파일이 비어있습니다. NodeId: {}, FilePath: {}", getId(), filePath);
                    return;
                }
            }

            log.debug("파일 읽기 완료. NodeId: {}, MessageId: {}, FilePath: {}", getId(), message.getId(),
                    filePath);

            super.onMessage(message);

        } catch (IOException e) {
            log.error("파일 읽기 실패. NodeId: {}, MessageId: {}, FilePath: {}", getId(), message.getId(),
                    filePath, e);
            throw new RuntimeException("파일 읽기 중 오류가 발생했습니다", e);
        }
    }
}
