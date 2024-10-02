package com.example.debezium.utils;

import com.example.debezium.config.DebeziumProperties;
import io.debezium.config.Configuration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.UUID;

import static io.debezium.config.CommonConnectorConfig.EVENT_PROCESSING_FAILURE_HANDLING_MODE;
import static io.debezium.config.CommonConnectorConfig.TOPIC_PREFIX;
import static io.debezium.connector.postgresql.PostgresConnectorConfig.*;
import static io.debezium.embedded.EmbeddedEngineConfig.*;
import static org.springframework.util.StringUtils.hasText;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DebeziumUtils {

    public static Configuration buildConfiguration(DebeziumProperties.DebeziumConnector config) {
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
}
