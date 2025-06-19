package com.SoccerNode.FixturesEvents;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FixturesEventsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FixturesEventsApplication.class)
                .run(args);
    }

}