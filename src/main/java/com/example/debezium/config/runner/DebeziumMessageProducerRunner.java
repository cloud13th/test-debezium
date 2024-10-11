//package com.example.debezium.config.runner;
//
//import com.example.debezium.config.DebeziumProperties;
//import com.example.debezium.handler.IDebeziumEventHandler;
//import io.debezium.embedded.async.ConvertingAsyncEngineBuilderFactory;
//import io.debezium.engine.ChangeEvent;
//import io.debezium.engine.DebeziumEngine;
//import io.debezium.engine.format.KeyValueHeaderChangeEventFormat;
//import io.debezium.engine.format.Protobuf;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.core.task.SimpleAsyncTaskExecutor;
//import org.springframework.integration.annotation.ServiceActivator;
//import org.springframework.integration.channel.ExecutorChannel;
//import org.springframework.integration.debezium.inbound.DebeziumMessageProducer;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.util.ObjectUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//import static com.example.debezium.utils.DebeziumUtils.buildConfiguration;
//
//@Slf4j
//@AllArgsConstructor
//@Order
//@Configuration
//public class DebeziumMessageProducerRunner {
//
//    private static final int THREADS = Runtime.getRuntime().availableProcessors() * 2;
//    private static final List<DebeziumMessageProducer> MESSAGE_PRODUCERS = new ArrayList<>();
//
//    private final List<IDebeziumEventHandler> eventHandlers;
//    private final DebeziumProperties properties;
//
//    @Bean
//    Executor executor() {
//        return Executors.newFixedThreadPool(THREADS);
//    }
//
//    @Bean(name = "inputChannel")
//    MessageChannel channel(Executor executor) {
//        return new ExecutorChannel(executor);
//    }
//
//    @Bean
//    List<DebeziumMessageProducer> messageProducers(@Qualifier("inputChannel") MessageChannel channel) {
//        for (DebeziumProperties.DebeziumConnector config : properties.getConnectors()) {
//            log.info("Registering debezium-producer for connector...");
//            if (!config.isEnable()) {
//                log.debug("Debezium engine disabled: {}", config);
//                continue;
//            }
//            log.debug("Debezium engine enabled: {}", config);
//            var producer = messageProducer(builder(config), channel);
//            boolean running = producer.isRunning();
//            log.trace("Debezium-producer is running: {}", running);
//            if (!running) {
//                producer.afterPropertiesSet();
//                producer.start();
//            }
//            log.debug("Successfully registered debezium-producer for connector: {}", config.getName());
//            MESSAGE_PRODUCERS.add(producer);
//        }
//        return MESSAGE_PRODUCERS;
//    }
//
//    @ServiceActivator(inputChannel = "inputChannel")
//    public void handler(List<ChangeEvent<byte[], byte[]>> events) {
//        log.trace("Events received: {}", events.size());
//        if (ObjectUtils.isEmpty(eventHandlers)) {
//            log.warn("No event handlers registered");
//            return;
//        }
//        for (ChangeEvent<byte[], byte[]> event : events) {
//            for (IDebeziumEventHandler handler : eventHandlers) {
//                try {
//                    handler.handle(event);
//                } catch (Exception e) {
//                    log.error("Handle Event Error", e);
//                }
//            }
//        }
//    }
//
//    private static DebeziumMessageProducer messageProducer(DebeziumEngine.Builder<ChangeEvent<byte[], byte[]>> builder,
//                                                           MessageChannel channel) {
//        var producer = new DebeziumMessageProducer(builder);
//        producer.setOutputChannel(channel);
//        producer.setTaskExecutor(new SimpleAsyncTaskExecutor());
//        return producer;
//    }
//
//    private static DebeziumEngine.Builder<ChangeEvent<byte[], byte[]>> builder(DebeziumProperties.DebeziumConnector config) {
//        var configuration = buildConfiguration(config);
//        var eventFormat = KeyValueHeaderChangeEventFormat.of(Protobuf.class, Protobuf.class, Protobuf.class);
//        return DebeziumEngine.create(eventFormat, ConvertingAsyncEngineBuilderFactory.class.getName())
//                .using(configuration.asProperties());
//    }
//}