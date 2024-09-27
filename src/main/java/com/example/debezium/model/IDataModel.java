package com.example.debezium.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface IDataModel {

    default <T extends IDataModel> T convert(ObjectMapper mapper, String json, Class<T> type) throws JsonProcessingException {
        return mapper.readValue(json, type);
    }

    String getId();

    void setId(String id);
}
