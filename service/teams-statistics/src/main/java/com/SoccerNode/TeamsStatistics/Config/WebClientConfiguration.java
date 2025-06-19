package com.SoccerNode.TeamsStatistics.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${api.key}")
    private String apiKey;

    @Bean
    public WebClient footballWebClient(@Value("${api.base-url}") String baseUrl,
                                       @Value("${api.key}") String apiKey) {

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-rapidapi-host", "v3.football.api-sports.io")
                .defaultHeader("x-rapidapi-key", apiKey)
                .exchangeStrategies(strategies)
                .build();
    }

}

