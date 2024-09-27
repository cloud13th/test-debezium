package com.example.debezium.config;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.connector.postgresql.PostgresConnectorConfig;
import lombok.Data;
import org.apache.kafka.connect.storage.MemoryOffsetBackingStore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "debezium")
public class DebeziumProperties {

    private List<DebeziumConnector> connectors;

    @Data
    public static class DebeziumConnector {
        private final boolean enable = true;
        private String name;
        private String connectorClass;
        private final String offsetStorage = MemoryOffsetBackingStore.class.getName();
        private final String pluginName = PostgresConnectorConfig.LogicalDecoder.PGOUTPUT.getValue();
        private String slotName;
        private final boolean dropSlotOnStop = false;
        private final boolean slotSeekToKnownOffset = false;
        private String publicationName;
        private final String publicationAutoCreateMode = PostgresConnectorConfig.AutoCreateMode.ALL_TABLES.getValue();
        private final String snapshotMode = PostgresConnectorConfig.SnapshotMode.INITIAL.getValue();
        private final String snapshotLockingMode = PostgresConnectorConfig.SnapshotLockingMode.NONE.getValue();
        private List<String> streamParams;
        private String topicPrefix;
        private final boolean readOnlyConnection = true;
        private final String eventProcessingFailureHandlingMode = CommonConnectorConfig.EventProcessingFailureHandlingMode.WARN.getValue();

        private String hostname;
        private String port;
        private String username;
        private String password;
        private String databaseName;
        private List<String> includeSchemas; // databaseName.schemaName
        private List<String> excludeSchemas; // databaseName.schemaName
        private List<String> includeTables; // databaseName.schemaName.tableName
        private List<String> excludeTables; // databaseName.schemaName.tableName
    }
}
