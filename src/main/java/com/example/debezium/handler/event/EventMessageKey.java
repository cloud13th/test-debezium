package com.example.debezium.handler.event;

import com.example.debezium.handler.event.data.Payload;
import com.example.debezium.handler.event.data.Schema;
import lombok.Data;
import org.springframework.data.elasticsearch.core.document.Document;

import static org.springframework.util.StringUtils.hasText;

@Data
public class EventMessageKey {

    /**
     * 数据行的主键：payload#id
     */
    private Schema schema;
    private Payload payload;

    public String getDataKey() {
        var id = this.payload.getId();
        if (!hasText(id)) {
            var before = this.payload.getBefore();
            id = Document.parse(before).getString("id");
            if (!hasText(id)) {
                var after = this.payload.getAfter();
                id = Document.parse(after).getString("id");
            }
        }
        return id;
    }
}
