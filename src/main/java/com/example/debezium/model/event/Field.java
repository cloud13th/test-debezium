package com.example.debezium.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Field {

    private String type;

    private boolean optional;

    @JsonProperty(value = "field")
    private String fieldName;

    private String name;

    private Integer version;

    @JsonProperty(value = "default")
    private Integer defaultValue;
}
