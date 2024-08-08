package com.example.opensearchdemo.study;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "example")
@Getter
@Setter
public class ExampleEntity {
    @Id
    private String id;

    private String name;

    private String company;

}