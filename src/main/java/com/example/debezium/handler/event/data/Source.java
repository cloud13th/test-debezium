package com.example.debezium.handler.event.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Source {

    private String version;
    private String connector;
    private String name;

    private String db;
    private String schema;
    private String table;

    private String snapshot;
    private String sequence;

    private Long lsn;

    private Integer txId;
    // 事务耗时
    @JsonProperty(value = "ts_ms")
    private Long millisecond; // 毫秒
    @JsonProperty(value = "ts_us")
    private Long microseconds; // 微秒
    @JsonProperty(value = "ts_ns")
    private Long nanosecond; // 纳秒
}
