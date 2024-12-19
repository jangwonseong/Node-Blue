package com.samsa;

import com.samsa.core.Flow;
import com.samsa.core.FlowPool;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;

/**
 * 이 클래스는 JSON 구성 파일에서 흐름을 로드하고 실행하는 예시입니다. 흐름은 nodeblue/src/main/resources/example_json 폴더의 파일에서 상대
 * 경로를 이용하여 로드됩니다.
 * 
 * 프로그램은 FlowPool을 생성하고, 여러 흐름을 추가한 후 이를 실행합니다. JSON 파일은 프로젝트의 nodeblue/src/main/resources 폴더에 위치한다고
 * 가정합니다.
 */
@Slf4j
public class FlowTest {

    /**
     * main 메서드는 JSON 파일에서 여러 흐름을 로드하고 FlowPool을 사용하여 실행합니다. JSON 파일의 경로는
     * nodeblue/src/main/resources/example_json 폴더 내 상대 경로로 제공됩니다.
     * 
     * @param args 명령줄 인수(이 경우 사용되지 않음)
     */
    public static void main(String[] args) {
        try {
            // 새로운 FlowPool을 생성하여 흐름들을 관리합니다.
            FlowPool flowPool = new FlowPool();

            // nodeblue/src/main/resources/example_json 폴더에서 상대 경로로 JSON 파일을 로드
            String flow1Path = Paths.get("nodeblue", "src", "main", "resources", "example_json",
                    "ModbusToMqttFlow.json").toString();
            // String flow2Path =
            // Paths.get("nodeblue", "src", "main", "resources", "example_json", "flow2.json")
            // .toString();

            // Flow 객체 생성
            Flow flow1 = FlowLoader.loadFlowFromJson(flow1Path);
            // Flow flow2 = FlowLoader.loadFlowFromJson(flow2Path);

            // 흐름을 FlowPool에 추가합니다.
            flowPool.addFlow(flow1);
            // flowPool.addFlow(flow2);

            // 흐름을 실행합니다.
            flowPool.run();

            // 실행 완료 메시지를 로그에 기록합니다.
            log.info("Flow execution completed!");
        } catch (Exception e) {
            // 오류가 발생하면 스택 트레이스를 출력합니다.
            e.printStackTrace();
        }
    }
}
