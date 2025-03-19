package com.foodcity.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class FoodCityApplication {
    public static void main(String[] args) {
        SpringApplication.run(FoodCityApplication.class, args);
    }
}