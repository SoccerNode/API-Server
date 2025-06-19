package com.SoccerNode.FixturesStatistics;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FixturesStatisticsApplication {

    public static void main(String[] args) {
        // Run Spring Application with .ini Initializer
        new SpringApplicationBuilder(FixturesStatisticsApplication.class)
                .run(args);
    }

}