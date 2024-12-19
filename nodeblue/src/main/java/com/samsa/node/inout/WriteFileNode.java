package com.samsa.node.inout;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samsa.annotation.NodeType;
import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일에 데이터를 쓰는 기능을 제공하는 노드 클래스입니다.
 * <p>
 * 수신한 {@link Message} 객체의 페이로드를 파일에 작성합니다. 파일 추가 모드 여부를 설정할 수 있으며, 파일 쓰기 실패 시
 * {@link RuntimeException}을 발생시킵니다.
 * </p>
 *
 * <p>
 * 이 클래스는 {@link InOutNode}를 상속받아 구현되었습니다.
 * </p>
 *
 * @see InOutNode
 * @see Message
 */
@NodeType("WriteFileNode")
@Slf4j
public class WriteFileNode extends InOutNode {
    private final Path filePath;
    private final boolean append;

    /**
     * 파일 쓰기 노드를 생성합니다.
     *
     * @param filePath 작성할 파일 경로 (null 불가)
     * @param append {@code true}일 경우 기존 파일에 데이터를 추가합니다. {@code false}일 경우 기존 파일을 덮어씁니다.
     * @throws IllegalArgumentException 파일 경로가 {@code null}인 경우 발생합니다.
     */
    public WriteFileNode(Path filePath, boolean append) {
        if (filePath == null) {
            log.error("파일 경로가 null입니다");
            throw new IllegalArgumentException("파일 경로는 null일 수 없습니다");
        }
        this.filePath = filePath;
        this.append = append;
    }

    /**
     * 문자열 파일 경로를 사용하여 파일 쓰기 노드를 생성합니다.
     *
     * @param filePath 작성할 파일 경로 (문자열, null 불가)
     * @param append {@code true}일 경우 기존 파일에 데이터를 추가합니다. {@code false}일 경우 기존 파일을 덮어씁니다.
     * @throws IllegalArgumentException 파일 경로가 {@code null}인 경우 발생합니다.
     */
    @JsonCreator
    public WriteFileNode(@JsonProperty("path") String filePath,
            @JsonProperty("append") boolean append) {
        if (filePath == null) {
            log.error("파일 경로가 null입니다");
            throw new IllegalArgumentException("파일 경로는 null일 수 없습니다");
        }
        this.filePath = Paths.get(filePath);
        this.append = append;
    }

    /**
     * ID를 지정하여 파일 쓰기 노드를 생성합니다.
     *
     * @param id 노드의 고유 식별자
     * @param filePath 작성할 파일 경로 (null 불가)
     * @param append {@code true}일 경우 기존 파일에 데이터를 추가합니다. {@code false}일 경우 기존 파일을 덮어씁니다.
     * @throws IllegalArgumentException 파일 경로가 {@code null}인 경우 발생합니다.
     */
    public WriteFileNode(UUID id, Path filePath, boolean append) {
        super(id);
        if (filePath == null) {
            log.error("파일 경로가 null입니다. NodeId: {}", id);
            throw new IllegalArgumentException("파일 경로는 null일 수 없습니다");
        }
        this.filePath = filePath;
        this.append = append;
    }

    /**
     * ID를 지정하고 문자열 파일 경로를 사용하여 파일 쓰기 노드를 생성합니다.
     *
     * @param id 노드의 고유 식별자
     * @param filePath 작성할 파일 경로 (문자열, null 불가)
     * @param append {@code true}일 경우 기존 파일에 데이터를 추가합니다. {@code false}일 경우 기존 파일을 덮어씁니다.
     * @throws IllegalArgumentException 파일 경로가 {@code null}인 경우 발생합니다.
     */
    public WriteFileNode(UUID id, String filePath, boolean append) {
        super(id);
        if (filePath == null) {
            log.error("파일 경로가 null입니다. NodeId: {}", id);
            throw new IllegalArgumentException("파일 경로는 null일 수 없습니다");
        }
        this.filePath = Paths.get(filePath);
        this.append = append;
    }

    /**
     * 메시지를 수신하고 페이로드를 파일에 작성합니다.
     * <p>
     * 메시지의 페이로드가 {@code null}일 경우 처리를 중단하며 에러 로그를 출력합니다. 페이로드를 문자열 형태로 변환하여 파일에 작성하고, 파일 쓰기 완료 후 상위
     * 클래스의 {@code onMessage()}를 호출하여 메시지를 다음 노드로 전달합니다.
     * </p>
     *
     * @param message 수신된 메시지 객체
     * @throws RuntimeException 파일 쓰기 중 {@link IOException}이 발생할 경우 예외를 던집니다.
     */
    @Override
    public void onMessage(Message message) {

        if (message == null) {
            log.error("메시지가 null입니다. NodeId: {}", getId());
            return;
        }

        Object payload = message.getPayload();

        if (payload == null) {
            log.error("페이로드가 null입니다. NodeId: {}, MessageId: {}", getId(), message.getId());
            return;
        }

        try (BufferedWriter writer =
                new BufferedWriter(new FileWriter(filePath.toFile(), append))) {
            log.debug("파일 쓰기 시작. NodeId: {}, MessageId: {}, FilePath: {}", getId(), message.getId(),
                    filePath);

            writer.write(payload.toString());
            writer.newLine();
            writer.flush();

            log.info("파일 쓰기 완료. NodeId: {}, MessageId: {}, FilePath: {}", getId(), message.getId(),
                    filePath);

            // 상위 클래스의 onMessage 호출하여 다음 노드로 메시지 전달
            super.onMessage(message);
        } catch (IOException e) {
            log.error("파일 쓰기 실패. NodeId: {}, MessageId: {}, FilePath: {}", getId(), message.getId(),
                    filePath, e);
            throw new RuntimeException("파일 쓰기 중 오류가 발생했습니다", e);
        }
    }
}
