package com.example.opensearchdemo.study;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/example")
public class ExampleController {

    private final ExampleService exampleService;

    @Autowired
    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @PostMapping
    public ResponseEntity<ExampleEntity> create(@RequestBody ExampleEntity entity) throws Exception {
        ExampleEntity savedEntity = exampleService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntity);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExampleEntity> getById(@PathVariable String id) throws Exception {
        ExampleEntity entity = exampleService.findById(id);
        return entity != null ? ResponseEntity.ok(entity) : ResponseEntity.notFound().build();
    }

    @GetMapping("/company/{company}")
    public ResponseEntity<List<ExampleEntity>> getByCompany(@PathVariable String company) throws Exception {
        List<ExampleEntity> entities = exampleService.findByCompany(company);
        return entities.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(entities);
    }


    @GetMapping("/company/{company}/view")
    public String viewCompanyList(@PathVariable String company, Model model) throws Exception {
        List<ExampleEntity> entities = exampleService.findByCompany(company);
        model.addAttribute("companyName", company);
        model.addAttribute("entities", entities);
        return "companyList";
    }
}