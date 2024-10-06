package com.example.debezium.api;

import com.example.debezium.model.postgres.Customer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "search")
@AllArgsConstructor
public class SearchApi {

    private final SearchOperations operations;

    @GetMapping
    public ResponseEntity<List<Customer>> searchByAge(int upper) {
        var query = CriteriaQuery.builder(Criteria
                        .where("age")
                        .lessThan(upper))
                .build();
        var customers = operations.search(query, Customer.class)
                .stream()
                .map(SearchHit::getContent)
                .toList();
        return ResponseEntity.ok(customers);
    }
}
