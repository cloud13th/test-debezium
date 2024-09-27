package com.example.debezium.service;

import com.example.debezium.handler.event.enums.OperationType;

public interface IElasticsearchService {

    void process(OperationType operation, String table, String id, String data);
}
