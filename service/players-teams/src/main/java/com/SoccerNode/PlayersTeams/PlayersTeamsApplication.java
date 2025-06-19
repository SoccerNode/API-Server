package com.SoccerNode.PlayersTeams;

import com.hooniegit.SpringInitializer.IniConfigApplicationContextInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PlayersTeamsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PlayersTeamsApplication.class)
                .initializers(new IniConfigApplicationContextInitializer())
                .run(args);
    }

}