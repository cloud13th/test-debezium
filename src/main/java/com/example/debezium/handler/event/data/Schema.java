package com.example.debezium.handler.event.data;

import lombok.Data;

import java.util.List;

@Data
public class Schema {

    private String type;
    private List<Field> fields;
    private Boolean optional;
    private String name;
    private Integer version;
}
