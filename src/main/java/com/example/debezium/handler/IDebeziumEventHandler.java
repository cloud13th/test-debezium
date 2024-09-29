package com.example.debezium.handler;

import io.debezium.engine.ChangeEvent;

public interface IDebeziumEventHandler {

    void handle(ChangeEvent<byte[], byte[]> event);
}
