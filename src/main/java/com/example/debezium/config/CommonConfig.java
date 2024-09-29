package com.example.debezium.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
@AllArgsConstructor
public class CommonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 忽略未识别的字段
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 序列化时忽略null值
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 蛇型命名和驼峰命名互相转换
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        // 注册Java时间模块
        mapper.registerModule(new JavaTimeModule());
        // 将所有时间都转为时间戳
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public MessageChannel debeziumChannel() {
        return new DirectChannel();
    }
}
