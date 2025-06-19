package com.SoccerNode.PlayersTeams;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PlayersTeamsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PlayersTeamsApplication.class)
                .run(args);
    }

}