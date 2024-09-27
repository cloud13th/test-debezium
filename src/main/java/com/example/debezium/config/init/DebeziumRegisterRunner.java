package com.example.debezium.config.init;

import com.example.debezium.config.DebeziumProperties;
import com.example.debezium.handler.IDebeziumEventHandler;
import io.debezium.config.Configuration;
import io.debezium.embedded.async.ConvertingAsyncEngineBuilderFactory;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import io.debezium.engine.format.KeyValueHeaderChangeEventFormat;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.storage.MemoryOffsetBackingStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.debezium.connector.postgresql.PostgresConnectorConfig.*;
import static io.debezium.embedded.EmbeddedEngineConfig.*;
import static io.debezium.relational.RelationalDatabaseConnectorConfig.DATABASE_EXCLUDE_LIST;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Component
@Order(value = 10_0000)
@AllArgsConstructor
public class DebeziumRegisterRunner implements ApplicationRunner {

    private static final List<DebeziumEngine<ChangeEvent<String, String>>> DEBEZIUM_ENGINES = Collections.synchronizedList(new ArrayList<>());
    private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private final List<IDebeziumEventHandler> eventHandlers;
    private final ConfigurableApplicationContext applicationContext;
    private final DebeziumProperties properties;

    private static Configuration buildConfiguration(DebeziumProperties.DebeziumConnector config) {
        String engineName = getEngineName(config);
        Configuration.Builder builder = Configuration.create()
                .with(ENGINE_NAME, engineName)
                .with(CONNECTOR_CLASS, config.getConnectorClass())
                .with(OFFSET_STORAGE, MemoryOffsetBackingStore.class.getName())
                // config for connector
                .with(PLUGIN_NAME, config.getPluginName())
                .with(SLOT_NAME, hasText(config.getSlotName()) ? config.getSlotName() : engineName)
                .with(DROP_SLOT_ON_STOP, config.getDropSlotOnStop())
                .with(SLOT_SEEK_TO_KNOWN_OFFSET, config.getSlotSeekToKnownOffset())
                .with(PUBLICATION_NAME, config.getPublicationName())
                .with(PUBLICATION_AUTOCREATE_MODE, config.getPublicationAutoCreateMode())
                // config for snapshot
                .with(SNAPSHOT_MODE, config.getSnapshotMode())
                .with(SNAPSHOT_LOCKING_MODE, config.getSnapshotLockingMode())
                // config for database
                .with(READ_ONLY_CONNECTION, config.getReadOnlyConnection())
                .with(HOSTNAME, config.getHostname())
                .with(PORT, config.getPort())
                .with(USER, config.getUsername())
                .with(PASSWORD, config.getPassword())
                // config for downstream
                .with(TOPIC_PREFIX, config.getTopicPrefix())
                .with(EVENT_PROCESSING_FAILURE_HANDLING_MODE, config.getEventProcessingFailureHandlingMode());

        List<String> streamParams = config.getStreamParams();
        if (!ObjectUtils.isEmpty(streamParams)) {
            builder.with(STREAM_PARAMS, String.join(";", streamParams));
        }

        if (hasText(config.getDatabaseName())) {
            builder.with(DATABASE_NAME, config.getDatabaseName());
        }

        List<String> includeDatabases = config.getIncludeDatabases();
        List<String> excludeDatabases = config.getExcludeDatabases();
        if (!ObjectUtils.isEmpty(includeDatabases)) {
            builder.with(DATABASE_INCLUDE_LIST, String.join(",", includeDatabases));
        } else {
            if (!ObjectUtils.isEmpty(excludeDatabases)) {
                builder.with(DATABASE_EXCLUDE_LIST, String.join(",", excludeDatabases));
            }
        }

        List<String> includeSchemas = config.getIncludeSchemas();
        List<String> excludeSchemas = config.getExcludeSchemas();
        if (!ObjectUtils.isEmpty(includeSchemas)) {
            builder.with(SCHEMA_INCLUDE_LIST, String.join(",", includeSchemas));
        } else {
            if (!ObjectUtils.isEmpty(excludeSchemas)) {
                builder.with(SCHEMA_EXCLUDE_LIST, String.join(",", excludeSchemas));
            }
        }

        List<String> includeTables = config.getIncludeTables();
        List<String> excludeTables = config.getExcludeTables();
        if (!ObjectUtils.isEmpty(includeTables)) {
            builder.with(TABLE_INCLUDE_LIST, String.join(",", includeTables));
        } else {
            if (!ObjectUtils.isEmpty(excludeTables)) {
                builder.with(TABLE_EXCLUDE_LIST, String.join(",", excludeTables));
            }
        }

        return builder.build();
    }

    private static String getEngineName(DebeziumProperties.DebeziumConnector config) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return hasText(config.getName()) ? config.getName() : uuid;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (DebeziumProperties.DebeziumConnector config : properties.getConnectors()) {
            log.info("Registering Debezium engine for connector...");
            if (!config.isEnable()) {
                log.debug("Debezium engine disabled: {}", config);
                continue;
            }
            log.debug("Config >>> {}", config);
            DebeziumEngine<ChangeEvent<String, String>> engine = this.buildDebeziumEngine(config);
            applicationContext.getBeanFactory().registerSingleton(config.getName(), engine);
            engine.run();
            log.debug("Success >>> {}", engine);
        }
    }

    private DebeziumEngine<ChangeEvent<String, String>> buildDebeziumEngine(DebeziumProperties.DebeziumConnector config) {
        Configuration configuration = buildConfiguration(config);
        DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine.create(
                        KeyValueHeaderChangeEventFormat.of(Json.class, Json.class, Json.class),
                        ConvertingAsyncEngineBuilderFactory.class.getName())
                .using(configuration.asProperties())
                .using(this.getClass().getClassLoader())
                .using(CLOCK)
                .notifying(event -> eventHandlers.forEach(eventHandler -> eventHandler.handleEvent(event)))
                .build();
        DEBEZIUM_ENGINES.add(engine);
        return engine;
    }

    @PreDestroy
    public void destroy() {
        for (DebeziumEngine<ChangeEvent<String, String>> engine : DEBEZIUM_ENGINES) {
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
