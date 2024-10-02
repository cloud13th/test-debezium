package com.example.debezium.handler.impl;

import com.example.debezium.handler.IDebeziumEventHandler;
import com.example.debezium.handler.event.EventMessageKey;
import com.example.debezium.handler.event.EventMessageValue;
import com.example.debezium.handler.event.enums.OperationType;
import com.example.debezium.service.IElasticsearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.engine.ChangeEvent;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultEventHandler implements IDebeziumEventHandler {

    private static final AtomicInteger INDEX = new AtomicInteger(0);

    private final ObjectMapper mapper;
    private final IElasticsearchService service;

    @SneakyThrows
    @Override
    public void handle(ChangeEvent<byte[], byte[]> event) {
        log.debug("Event Count = {} >>> {}", INDEX.incrementAndGet(), event.toString());

        byte[] keyed = event.key();
        if (ObjectUtils.isEmpty(keyed)) {
            return;
        }
        byte[] valued = event.value();
        if (ObjectUtils.isEmpty(valued)) {
            return;
        }

        EventMessageKey key;
        EventMessageValue value;

        try {
            String content = new String(keyed, StandardCharsets.UTF_8);
            log.trace("Key Content: {}", content);
            key = mapper.readValue(content, EventMessageKey.class);
        } catch (Exception e) {
            log.error("Error reading key: {}", keyed, e);
            return;
        }
        String id = key.getDataKey();

        try {
            String content = new String(valued, StandardCharsets.UTF_8);
            log.trace("Value Content: {}", content);
            value = mapper.readValue(content, EventMessageValue.class);
        } catch (Exception e) {
            log.error("Error reading value: {}", valued, e);
            return;
        }

        OperationType operation = value.getOperation();
        String table = value.fullDefinitionTableName();
        String data = value.getDataValue();

        try {
            service.process(operation, table, id, data);
        } catch (Exception e) {
            log.error("Error processing data: {} {} {} {}", operation, table, id, data, e);
        }
    }
}
