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
@TableDefinition(database = "debezium", table = "post")
@Document(indexName = "post")
public class Post implements IDataModel {

    @Id
    private String id;

    private String title;

    private String author;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Long postTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Long createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Long updateTime;

    private Boolean deleted;

    private Long version;
}
