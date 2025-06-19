package com.SoccerNode.TeamsStatistics;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TeamsStatisticsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(TeamsStatisticsApplication.class)
                .run(args);
    }

}