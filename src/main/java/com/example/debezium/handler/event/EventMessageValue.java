package com.example.debezium.handler.event;

import com.example.debezium.handler.event.data.Payload;
import com.example.debezium.handler.event.data.Schema;
import com.example.debezium.handler.event.data.Source;
import com.example.debezium.handler.event.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventMessageValue {

    private Schema schema;
    private Payload payload;

    public String fullDefinitionTableName() {
        Source source = this.payload.getSource();
        return String.join(".", source.getDb(), source.getSchema(), source.getTable());
    }

    public String getDataValue() {
        return this.payload.getAfter();
    }

    public OperationType getOperation() {
        String operation = this.payload.getOperation();
        return OperationType.getByType(operation);
    }
}
