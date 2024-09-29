package com.example.debezium.config.init;

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
import org.springframework.messaging.MessageChannel;
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
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Component
@Order(value = 10_0000)
@AllArgsConstructor
public class DebeziumRegisterRunner implements ApplicationRunner {

    private static final List<DebeziumEngine<ChangeEvent<byte[], byte[]>>> DEBEZIUM_ENGINES = Collections.synchronizedList(new ArrayList<>());

    private final List<IDebeziumEventHandler> eventHandlers;
    private final ConfigurableApplicationContext applicationContext;
    private final DebeziumProperties properties;
    private final MessageChannel debeziumChannel;

    private static Configuration buildConfiguration(DebeziumProperties.DebeziumConnector config) {
        String engineName = getEngineName(config.getName());
        Configuration.Builder builder = Configuration.empty().edit()
                .with(ENGINE_NAME, engineName)
                .with(CONNECTOR_CLASS, config.getConnectorClass())
                .with(OFFSET_STORAGE, config.getOffsetStorage())
                .with(READ_ONLY_CONNECTION, config.getReadOnlyConnection())
                .with(PLUGIN_NAME, config.getPluginName())
                .with(SLOT_NAME, hasText(config.getSlotName()) ? config.getSlotName() : engineName)
                .with(DROP_SLOT_ON_STOP, config.getDropSlotOnStop())
                .with(SLOT_SEEK_TO_KNOWN_OFFSET, config.getSlotSeekToKnownOffset())
                .with(PUBLICATION_NAME, config.getPublicationName() + "_publication")
                .with(PUBLICATION_AUTOCREATE_MODE, config.getPublicationAutoCreateMode())
                .with(SNAPSHOT_MODE, config.getSnapshotMode())
                .with(SNAPSHOT_LOCKING_MODE, config.getSnapshotLockingMode())
                // config for downstream
                .with(TOPIC_PREFIX, config.getTopicPrefix())
                .with(EVENT_PROCESSING_FAILURE_HANDLING_MODE, config.getEventProcessingFailureHandlingMode());

        List<String> params = config.getStreamParams();
        if (!ObjectUtils.isEmpty(params)) {
            builder.with(STREAM_PARAMS, String.join(";", params));
        }

        DebeziumProperties.DebeziumDatabaseConnector database = config.getDatabase();
        if (!ObjectUtils.isEmpty(database)) {
            // config for database
            builder.with(HOSTNAME, database.getHostname())
                    .with(PORT, database.getPort())
                    .with(USER, database.getUser())
                    .with(PASSWORD, database.getPassword())
                    .with(DATABASE_NAME, database.getName());
            // config for schemas
            List<String> includeSchemas = database.getIncludeSchemas();
            List<String> excludeSchemas = database.getExcludeSchemas();
            if (!ObjectUtils.isEmpty(includeSchemas)) {
                builder.with(SCHEMA_INCLUDE_LIST, String.join(",", includeSchemas));
            } else {
                if (!ObjectUtils.isEmpty(excludeSchemas)) {
                    builder.with(SCHEMA_EXCLUDE_LIST, String.join(",", excludeSchemas));
                }
            }
            // config for tables
            List<String> includeTables = database.getIncludeTables();
            List<String> excludeTables = database.getExcludeTables();
            if (!ObjectUtils.isEmpty(includeTables)) {
                builder.with(TABLE_INCLUDE_LIST, String.join(",", includeTables));
            } else {
                if (!ObjectUtils.isEmpty(excludeTables)) {
                    builder.with(TABLE_EXCLUDE_LIST, String.join(",", excludeTables));
                }
            }
        }

        return builder.build();
    }

    private static String getEngineName(String name) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return hasText(name) ? name : uuid;
    }

    @Override
    public void run(ApplicationArguments args) {
        this.executeDebeziumEngines();
    }

    private void executeDebeziumEngines() {
        for (DebeziumProperties.DebeziumConnector config : properties.getConnectors()) {
            log.info("Registering Debezium engine for connector...");
            if (!config.getEnable()) {
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

//    private DebeziumEngine<ChangeEvent<String, String>> buildDebeziumEngines(DebeziumProperties.DebeziumConnector config) {
//        Configuration configuration = buildConfiguration(config);
//        DebeziumEngine.Builder<ChangeEvent<byte[], byte[]>> builder = DebeziumEngine.create(
//                        KeyValueHeaderChangeEventFormat.of(Protobuf.class, Protobuf.class, Protobuf.class),
//                        ConvertingAsyncEngineBuilderFactory.class.getName())
//                .using(configuration.asProperties())
//                .using(CLOCK);
//        DebeziumMessageProducer producer = new DebeziumMessageProducer(builder);
//        producer.setApplicationContext(applicationContext);
//        producer.setOutputChannel(debeziumChannel);
//        return producer;
//    }

    private DebeziumEngine<ChangeEvent<byte[], byte[]>> buildDebeziumEngine(DebeziumProperties.DebeziumConnector config) {
        Configuration configuration = buildConfiguration(config);
        DebeziumEngine<ChangeEvent<byte[], byte[]>> engine = DebeziumEngine.create(
                        KeyValueHeaderChangeEventFormat.of(JsonByteArray.class, JsonByteArray.class, JsonByteArray.class),
                        ConvertingAsyncEngineBuilderFactory.class.getName())
                .using(configuration.asProperties())
                .using(this.getClass().getClassLoader())
                .notifying(event -> eventHandlers.forEach(eventHandler -> eventHandler.handle(event)))
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
