package com.example.debezium;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@AllArgsConstructor
@SpringBootApplication
public class DebeziumSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(DebeziumSyncApplication.class, args);
    }
}
