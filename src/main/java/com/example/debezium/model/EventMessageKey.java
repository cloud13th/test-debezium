package com.example.debezium.model;

import com.example.debezium.model.event.Field;
import lombok.Data;

import java.util.List;

@Data
public class EventMessageKey {

    private Schema schema;
    private Payload payload;

    @Data
    public static class Schema {
        private String type;
        private List<Field> fields;
        private boolean optional;
        private String name;
    }

    @Data
    public static class Payload {
        private String id;
    }
}
