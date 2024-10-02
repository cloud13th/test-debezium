package com.example.debezium.config.offset;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.runtime.WorkerConfig;
import org.apache.kafka.connect.storage.MemoryOffsetBackingStore;
import org.apache.kafka.connect.util.Callback;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

@Slf4j
public class RedisOffsetBackingStore extends MemoryOffsetBackingStore {

    public RedisOffsetBackingStore() {
        log.info("RedisOffsetBackingStore initialized...");
    }

    @Override
    public Set<Map<String, Object>> connectorPartitions(String connectorName) {
        return Set.of();
    }

    @Override
    public void configure(WorkerConfig config) {
        super.configure(config);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public Future<Map<ByteBuffer, ByteBuffer>> get(Collection<ByteBuffer> keys) {
        return super.get(keys);
    }

    @Override
    public Future<Void> set(Map<ByteBuffer, ByteBuffer> values, Callback<Void> callback) {
        return super.set(values, callback);
    }

    @Override
    protected void save() {
        super.save();
    }
}
