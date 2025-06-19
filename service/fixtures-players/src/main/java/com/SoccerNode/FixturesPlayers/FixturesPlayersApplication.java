package com.SoccerNode.FixturesPlayers;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FixturesPlayersApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FixturesPlayersApplication.class)
                .run(args);
    }

}