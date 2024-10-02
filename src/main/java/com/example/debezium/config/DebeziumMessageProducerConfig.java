package com.example.debezium.config;

import io.debezium.embedded.async.ConvertingAsyncEngineBuilderFactory;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.KeyValueHeaderChangeEventFormat;
import io.debezium.engine.format.Protobuf;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.debezium.inbound.DebeziumMessageProducer;
import org.springframework.messaging.MessageChannel;

import java.util.List;

import static com.example.debezium.utils.DebeziumUtils.buildConfiguration;

@Slf4j
//@org.springframework.context.annotation.Configuration
@AllArgsConstructor
public class DebeziumMessageProducerConfig {

    private final ConfigurableApplicationContext applicationContext;
    private final DebeziumProperties properties;

    @Bean
    MessageChannel debeziumInputChannel() {
        return new DirectChannel();
    }

    MessageProducer messageProducer() {
        for (DebeziumProperties.DebeziumConnector config : properties.getConnectors()) {
            log.info("Registering Debezium producer for connector...");
            if (!config.isEnable()) {
                log.debug("Debezium engine disabled: {}", config);
                continue;
            }
            log.debug("Config >>> {}", config);
            var producer = this.debeziumMessageProducer(null, null);
            applicationContext.getBeanFactory().registerSingleton(config.getName(), producer);
            log.debug("Success >>> {}", producer);
        }
        return null;
    }

    private DebeziumMessageProducer debeziumMessageProducer(DebeziumEngine.Builder<ChangeEvent<byte[], byte[]>> debeziumEngineBuilder,
                                                            MessageChannel debeziumChannel) {
        DebeziumMessageProducer producer = new DebeziumMessageProducer(debeziumEngineBuilder);
        producer.setEnableBatch(false);
        producer.setEnableEmptyPayload(false);
        producer.setOutputChannel(debeziumChannel);
        return producer;
    }

    private static DebeziumEngine.Builder<ChangeEvent<byte[], byte[]>> getBuilder(DebeziumProperties.DebeziumConnector config) {
        var configuration = buildConfiguration(config);
        var eventFormat = KeyValueHeaderChangeEventFormat.of(Protobuf.class, Protobuf.class, Protobuf.class);
        return DebeziumEngine.create(eventFormat, ConvertingAsyncEngineBuilderFactory.class.getName())
                .using(configuration.asProperties());
    }

    @ServiceActivator(inputChannel = "debeziumInputChannel")
    public void handler(List<ChangeEvent<Object, Object>> payload) {
        log.trace("payload >>> {}", payload);
    }
}
