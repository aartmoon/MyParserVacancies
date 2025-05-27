package com.example.config;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class HhSearchProperties {

    private final String searchField = "name";
    private final int minSalary = 0;
    private final String currency = "RUR";
    private final boolean onlyWithSalary = false;
    private final int perPage = 100;
}