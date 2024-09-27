package com.example.debezium.config.init;

import com.example.debezium.model.IDataModel;
import com.example.debezium.utils.TableDefinitionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Order(value = 1_0000)
@AllArgsConstructor
public class DataModelRegisterRunner implements ApplicationRunner {

    public static final Map<String, IDataModel> DATA_MODEL_MAP = new ConcurrentHashMap<>();

    private final ApplicationContext context;

    @Override
    public void run(ApplicationArguments args) {
        Map<String, IDataModel> beans = context.getBeansOfType(IDataModel.class);
        for (Map.Entry<String, IDataModel> entry : beans.entrySet()) {
            String table = TableDefinitionUtils.getTableName(entry.getValue().getClass());
            DATA_MODEL_MAP.put(table, entry.getValue());
        }
        log.trace("DataModel Map >>> {}", DATA_MODEL_MAP);
    }
}
