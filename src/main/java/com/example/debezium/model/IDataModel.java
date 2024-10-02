package com.example.debezium.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import static org.springframework.util.StringUtils.hasText;

public interface IDataModel {

    @SneakyThrows
    default <T extends IDataModel> T convert(ObjectMapper mapper, String json, Class<T> type) throws JsonProcessingException {
        if (hasText(json)) {
            return mapper.readerFor(type).readValue(json);
        } else {
            return type.getConstructor().newInstance();
        }
    }

    String getId();

    void setId(String id);
}
