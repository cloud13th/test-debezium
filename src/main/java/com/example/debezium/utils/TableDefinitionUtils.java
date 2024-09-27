package com.example.debezium.utils;

import com.example.debezium.annotation.TableDefinition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableDefinitionUtils {

    public static <T> String getTableName(Class<T> type) {
        TableDefinition annotation = type.getAnnotation(TableDefinition.class);
        if (ObjectUtils.isEmpty(annotation)) {
            return null;
        }
        return String.join(".", annotation.database(), annotation.schema(), annotation.table());
    }
}
