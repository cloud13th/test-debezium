package com.example.debezium.utils;

import com.example.debezium.config.DebeziumProperties;
import io.debezium.config.Configuration;
import io.debezium.storage.redis.offset.RedisOffsetBackingStore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.UUID;

import static io.debezium.config.CommonConnectorConfig.EVENT_PROCESSING_FAILURE_HANDLING_MODE;
import static io.debezium.config.CommonConnectorConfig.TOPIC_PREFIX;
import static io.debezium.connector.postgresql.PostgresConnectorConfig.*;
import static io.debezium.embedded.EmbeddedEngineConfig.*;
import static io.debezium.embedded.async.AsyncEngineConfig.AVAILABLE_CORES;
import static io.debezium.embedded.async.AsyncEngineConfig.RECORD_PROCESSING_THREADS;
import static org.springframework.util.StringUtils.hasText;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DebeziumUtils {

    private static final String DEBEZIUM_PREFIX = "debezium";

    public static Configuration buildConfiguration(DebeziumProperties.DebeziumConnector config) {
        Configuration.Builder builder = Configuration.empty().edit()
                .withDefault(ENGINE_NAME, getEngineName(config.getName()))
                .withDefault(CONNECTOR_CLASS, config.getConnectorClass())
                .withDefault(READ_ONLY_CONNECTION, config.getReadOnlyConnection())
                .withDefault(PLUGIN_NAME, config.getPluginName())
                .withDefault(SLOT_NAME, getSlotName(config))
                .withDefault(DROP_SLOT_ON_STOP, config.getDropSlotOnStop())
                .withDefault(SLOT_SEEK_TO_KNOWN_OFFSET, config.getSlotSeekToKnownOffset())
                .withDefault(PUBLICATION_NAME, getPublicationName(config))
                .withDefault(PUBLICATION_AUTOCREATE_MODE, config.getPublicationAutoCreateMode())
                .withDefault(SNAPSHOT_MODE, config.getSnapshotMode())
                .withDefault(SNAPSHOT_LOCKING_MODE, config.getSnapshotLockingMode())
                // config for downstream
                .withDefault(TOPIC_PREFIX, getTopicPrefix(config))
                .withDefault(EVENT_PROCESSING_FAILURE_HANDLING_MODE, config.getEventProcessingFailureHandlingMode());

        List<String> params = config.getStreamParams();
        if (!ObjectUtils.isEmpty(params)) {
            builder.withDefault(STREAM_PARAMS, String.join(";", params));
        }

        buildDatabaseConfig(config, builder);
        buildOffsetBackingStore(config, builder);

        return builder.build();
    }

    private static void buildDatabaseConfig(DebeziumProperties.DebeziumConnector config, Configuration.Builder builder) {
        DebeziumProperties.DebeziumDatabaseConnector database = config.getDatabase();
        if (!ObjectUtils.isEmpty(database)) {
            // config for database
            builder.withDefault(HOSTNAME, database.getHostname())
                    .withDefault(PORT, database.getPort())
                    .withDefault(USER, database.getUser())
                    .withDefault(PASSWORD, database.getPassword())
                    .withDefault(DATABASE_NAME, database.getName())
                    .withDefault(RECORD_PROCESSING_THREADS, AVAILABLE_CORES * 2);
            // config for schemas
            List<String> includeSchemas = database.getIncludeSchemas();
            List<String> excludeSchemas = database.getExcludeSchemas();
            if (!ObjectUtils.isEmpty(includeSchemas)) {
                builder.withDefault(SCHEMA_INCLUDE_LIST, String.join(",", includeSchemas));
            } else {
                if (!ObjectUtils.isEmpty(excludeSchemas)) {
                    builder.withDefault(SCHEMA_EXCLUDE_LIST, String.join(",", excludeSchemas));
                }
            }
            // config for tables
            List<String> includeTables = database.getIncludeTables();
            List<String> excludeTables = database.getExcludeTables();
            if (!ObjectUtils.isEmpty(includeTables)) {
                builder.withDefault(TABLE_INCLUDE_LIST, String.join(",", includeTables));
            } else {
                if (!ObjectUtils.isEmpty(excludeTables)) {
                    builder.withDefault(TABLE_EXCLUDE_LIST, String.join(",", excludeTables));
                }
            }
        }
    }

    private static void buildOffsetBackingStore(DebeziumProperties.DebeziumConnector config, Configuration.Builder builder) {
        var offset = config.getOffset();
        var prefix = "debezium.source.offset.storage.redis.";
        builder.withDefault(OFFSET_STORAGE, RedisOffsetBackingStore.class.getName())
                .withDefault("debezium.sink.type", "redis")
                .withDefault(prefix + "address", offset.getAddress())
                .withDefault(prefix + "password", offset.getPassword())
                .withDefault(prefix + "database", offset.getDatabase())
                .withDefault(prefix + "ssl.enabled", offset.getSsl())
                .withDefault(prefix + "key", String.join(":", "metadata", offset.getKey(), "offsets"));
        String username = offset.getUsername();
        if (hasText(username)) {
            builder.withDefault(prefix + "user", username);
        }
    }

    private static String getEngineName(String name) {
        return String.join("_", DEBEZIUM_PREFIX, "engine", name);
    }

    private static String getSlotName(DebeziumProperties.DebeziumConnector config) {
        String name = config.getName();
        String slotName = config.getSlotName();
        if (hasText(slotName)) {
            name = slotName;
        }
        return String.join("_", DEBEZIUM_PREFIX, "slot", name);
    }

    private static String getPublicationName(DebeziumProperties.DebeziumConnector config) {
        String name = config.getName();
        String publicationName = config.getPublicationName();
        if (hasText(publicationName)) {
            name = publicationName;
        }
        return String.join("_", DEBEZIUM_PREFIX, "publication", name);
    }

    private static String getTopicPrefix(DebeziumProperties.DebeziumConnector config) {
        String prefix = config.getTopicPrefix();
        if (!hasText(prefix)) {
            prefix = uuid();
        }
        return prefix;
    }

    private static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
