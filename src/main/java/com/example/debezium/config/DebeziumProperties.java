package com.example.debezium.config;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.connector.postgresql.PostgresConnector;
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
        private String name;
        private String slotName;
        private String publicationName;
        private String topicPrefix;
        private List<String> streamParams;
        private Boolean readOnlyConnection = true;
        private Boolean enable = true;
        private String connectorClass = PostgresConnector.class.getName();
        private String offsetStorage = MemoryOffsetBackingStore.class.getName();
        private String pluginName = PostgresConnectorConfig.LogicalDecoder.PGOUTPUT.getValue();
        private Boolean dropSlotOnStop = false;
        private Boolean slotSeekToKnownOffset = false;
        private String publicationAutoCreateMode = PostgresConnectorConfig.AutoCreateMode.ALL_TABLES.getValue();
        private String snapshotMode = PostgresConnectorConfig.SnapshotMode.INITIAL.getValue();
        private String snapshotLockingMode = PostgresConnectorConfig.SnapshotLockingMode.SHARED.getValue();
        private String eventProcessingFailureHandlingMode = CommonConnectorConfig.EventProcessingFailureHandlingMode.WARN.getValue();
        private DebeziumDatabaseConnector database = new DebeziumDatabaseConnector();
    }

    @Data
    public static class DebeziumDatabaseConnector {
        private String hostname = "localhost";
        private String port = "5432";
        private String name = "postgres";
        private String user;
        private String password;
        private List<String> includeSchemas; // databaseName.schemaName
        private List<String> excludeSchemas; // databaseName.schemaName
        private List<String> includeTables; // databaseName.schemaName.tableName
        private List<String> excludeTables; // databaseName.schemaName.tableName
    }
}
