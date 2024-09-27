package com.example.debezium.handler.event.enums;

import io.debezium.util.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum OperationType {

    SEARCH("r"),
    INSERT("c"),
    UPDATE("u"),
    DELETE("d");

    private final String type;

    public static OperationType getByType(String type) {
        return Arrays.stream(OperationType.values())
                .filter(ot -> Strings.equalsIgnoreCase(ot.type, type))
                .findFirst()
                .orElse(null);
    }
}
