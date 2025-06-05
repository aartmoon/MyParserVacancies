package com.example.config;

import com.example.service.general.VacancyFetcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class VacancyFetcherConfig {

    @Bean
    public List<VacancyFetcher> vacancyFetchers(
            com.example.service.hh.HhFetcher hhFetcher,
            com.example.service.trudvsem.TrudVsemFetcher trudVsemFetcher) {

        return List.of(hhFetcher, trudVsemFetcher);
    }
}