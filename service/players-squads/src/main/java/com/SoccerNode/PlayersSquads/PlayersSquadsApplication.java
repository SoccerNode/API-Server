package com.SoccerNode.PlayersSquads;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PlayersSquadsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PlayersSquadsApplication.class)
                .run(args);
    }

}