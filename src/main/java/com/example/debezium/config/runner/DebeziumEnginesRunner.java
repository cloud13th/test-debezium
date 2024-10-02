package com.example.debezium.config.runner;

import com.example.debezium.config.DebeziumProperties;
import com.example.debezium.handler.IDebeziumEventHandler;
import io.debezium.config.Configuration;
import io.debezium.embedded.async.ConvertingAsyncEngineBuilderFactory;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.JsonByteArray;
import io.debezium.engine.format.KeyValueHeaderChangeEventFormat;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.debezium.utils.DebeziumUtils.buildConfiguration;

@Slf4j
@Component
@Order(value = 10_0000)
@AllArgsConstructor
public class DebeziumEnginesRunner implements ApplicationRunner {

    private static final List<DebeziumEngine<ChangeEvent<byte[], byte[]>>> DEBEZIUM_ENGINES = Collections.synchronizedList(new ArrayList<>());

    private final List<IDebeziumEventHandler> eventHandlers;
    private final ConfigurableApplicationContext applicationContext;
    private final DebeziumProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        this.executeDebeziumEngines();
    }

    private void executeDebeziumEngines() {
        for (DebeziumProperties.DebeziumConnector config : properties.getConnectors()) {
            log.info("Registering Debezium engine for connector...");
            if (!config.isEnable()) {
                log.debug("Debezium engine disabled: {}", config);
                continue;
            }
            log.debug("Config >>> {}", config);
            DebeziumEngine<ChangeEvent<byte[], byte[]>> engine = this.buildDebeziumEngine(config);
            applicationContext.getBeanFactory().registerSingleton(config.getName(), engine);
            engine.run();
            log.debug("Success >>> {}", engine);
        }
    }

    private DebeziumEngine<ChangeEvent<byte[], byte[]>> buildDebeziumEngine(DebeziumProperties.DebeziumConnector config) {
        Configuration configuration = buildConfiguration(config);
        DebeziumEngine<ChangeEvent<byte[], byte[]>> engine = DebeziumEngine.create(
                        KeyValueHeaderChangeEventFormat.of(JsonByteArray.class, JsonByteArray.class, JsonByteArray.class),
                        ConvertingAsyncEngineBuilderFactory.class.getName())
                .using(configuration.asProperties())
                .using(this.getClass().getClassLoader())
                .notifying(event -> eventHandlers.forEach(eventHandler -> {
                    try {
                        eventHandler.handle(event);
                    } catch (Exception e) {
                        log.error("Handle Event Error", e);
                    }
                }))
                .build();
        DEBEZIUM_ENGINES.add(engine);
        return engine;
    }

    @PreDestroy
    public void destroy() {
        for (DebeziumEngine<ChangeEvent<byte[], byte[]>> engine : DEBEZIUM_ENGINES) {
            if (!ObjectUtils.isEmpty(engine)) {
                try {
                    engine.close();
                } catch (IOException e) {
                    log.debug("Close DebeziumEngines Error", e);
                }
            }
        }
    }
}
