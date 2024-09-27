package com.example.debezium.handler.event.data;

import lombok.Data;

import java.util.List;

@Data
public class Field {

    private String type;
    private Boolean optional;
    private String name;
    private String field;
    private Integer version;
    private List<Field> fields;
}
