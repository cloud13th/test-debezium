package com.example.debezium.handler;

import io.debezium.embedded.EmbeddedEngineChangeEvent;
import io.debezium.engine.ChangeEvent;

public interface IDebeziumEventHandler {

    void handle(EmbeddedEngineChangeEvent<String, String, String> event);

    default void handleEvent(ChangeEvent<String, String> changeEvent) {
        @SuppressWarnings("unchecked")
        EmbeddedEngineChangeEvent<String, String, String> event = (EmbeddedEngineChangeEvent<String, String, String>) changeEvent;
        this.handle(event);
    }
}
