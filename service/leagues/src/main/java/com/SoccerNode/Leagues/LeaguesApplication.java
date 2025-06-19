package com.SoccerNode.Leagues;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LeaguesApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(LeaguesApplication.class)
                .run(args);
    }

}