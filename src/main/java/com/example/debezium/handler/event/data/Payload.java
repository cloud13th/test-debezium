package com.example.debezium.handler.event.data;

import com.example.debezium.config.JsonToStringDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class Payload {

    private String id;

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    private String before;
    @JsonDeserialize(using = JsonToStringDeserializer.class)
    private String after;

    private Source source;

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    private String transaction;

    /**
     * 操作类型
     * <pre>
     *     r-read
     *     c-create
     *     u-update
     *     d-delete
     *     t-truncates
     * </pre>
     */
    @JsonProperty(value = "op")
    private String operation;

    // 事务耗时
    @JsonProperty(value = "ts_ms")
    private Long millisecond; // 毫秒
    @JsonProperty(value = "ts_us")
    private Long microseconds; // 微秒
    @JsonProperty(value = "ts_ns")
    private Long nanosecond; // 纳秒
}
