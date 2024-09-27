package com.example.debezium.handler.event;

import com.example.debezium.handler.event.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SourceOffset {

    @JsonProperty(value = "lsn")
    private long lsn;

    @JsonProperty(value = "lsn_proc")
    private long lsnProc;

    @JsonProperty(value = "lsn_commit")
    private long lsnCommit;

    @JsonProperty(value = "txId")
    private long txId;

    @JsonProperty(value = "ts_usec")
    private long tsUsec;

    @JsonProperty(value = "messageType")
    private MessageType messageType;
}
