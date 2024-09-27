package com.example.debezium.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonToStringDeserializer extends JsonDeserializer<String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        Object tree = OBJECT_MAPPER.readTree(parser);
        return OBJECT_MAPPER.writeValueAsString(tree);
    }
}
