package com.samsa.node.inout;

import java.util.UUID;
import com.samsa.core.InOutNode;
import com.samsa.core.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonParserNode extends InOutNode {

    public JsonParserNode(UUID id) {
        super(id);
    }

    @Override
    public void onMessage(Message message) {
        if(message.getPayload() instanceof )
    }
}
