package com.example.debezium.config;

import lombok.Data;
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
        private boolean enable = true;
        private String slotName;
        private String connectorClass;
        private String offsetStorage;
        // PostgresConnectorConfig.LogicalDecoder
        private String pluginName;
        private Boolean dropSlotOnStop;
        private Boolean slotSeekToKnownOffset;
        private String publicationName;
        // PostgresConnectorConfig.AutoCreateMode
        private String publicationAutoCreateMode;
        private List<String> streamParams;
        // PostgresConnectorConfig.SnapshotMode
        private String snapshotMode;
        // PostgresConnectorConfig.SnapshotLockingMode
        private String snapshotLockingMode;
        private String topicPrefix;
        private Boolean readOnlyConnection;
        // CommonConnectorConfig.EventProcessingFailureHandlingMode
        private String eventProcessingFailureHandlingMode;
        private String hostname;
        private String port;
        private String username;
        private String password;
        private String databaseName;
        private List<String> includeDatabases; // databaseName
        private List<String> excludeDatabases; // databaseName
        private List<String> includeSchemas; // databaseName.schemaName
        private List<String> excludeSchemas; // databaseName.schemaName
        private List<String> includeTables; // databaseName.schemaName.tableName
        private List<String> excludeTables; // databaseName.schemaName.tableName
    }
}
