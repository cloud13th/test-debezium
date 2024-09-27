package com.example.debezium.model.postgres;

import com.example.debezium.annotation.TableDefinition;
import com.example.debezium.model.IDataModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.stereotype.Component;

@Data
@Component
@TableDefinition(table = "customer")
@Document(indexName = "customer")
public class Customer implements IDataModel {

    @Id
    private String id;

    private String name;

    private Integer age;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Long createTime;

    private Long version;
}
