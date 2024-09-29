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
        log.info("{} >>> {}", INDEX.incrementAndGet(), event);

        EventMessageKey key = mapper.readValue(event.key(), EventMessageKey.class);
        String id = key.getDataKey();

        EventMessageValue value = mapper.readValue(event.value(), EventMessageValue.class);
        OperationType operation = value.getOperation();
        String table = value.fullDefinitionTableName();
        String data = value.getDataValue();

        service.process(operation, table, id, data);
    }
}
