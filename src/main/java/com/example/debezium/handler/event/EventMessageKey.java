package com.example.debezium.handler.event;

import com.example.debezium.handler.event.data.Payload;
import com.example.debezium.handler.event.data.Schema;
import lombok.Data;

@Data
public class EventMessageKey {

    /**
     * 数据行的主键：payload#id
     */
    private Schema schema;
    private Payload payload;

    public String getDataKey() {
        return this.payload.getId();
    }
}
