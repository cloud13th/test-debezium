package com.example.debezium.handler.impl;

import com.example.debezium.handler.IDebeziumEventHandler;
import com.example.debezium.model.EventMessageKey;
import com.example.debezium.model.EventMessageValue;
import com.example.debezium.model.SourceOffset;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.embedded.EmbeddedEngineChangeEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultEventHandler implements IDebeziumEventHandler {

    private static final AtomicInteger INDEX = new AtomicInteger(0);

    private final ObjectMapper objectMapper;

    @Override
    public void handle(EmbeddedEngineChangeEvent<String, String, String> event) {
        log.info("{} >>> {}", INDEX.incrementAndGet(), event);
        // key
        EventMessageKey key = objectMapper.convertValue(event.key(), EventMessageKey.class);
        // value
        objectMapper.convertValue(event.value(), EventMessageValue.class);

        // source offset

        Map<String, ?> sourceOffset = event.sourceRecord().sourceOffset();
        SourceOffset offset = objectMapper.convertValue(sourceOffset, SourceOffset.class);
    }
}
