package com.SoccerNode.Standings;

import com.hooniegit.SpringInitializer.IniConfigApplicationContextInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StandingsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(StandingsApplication.class)
                .initializers(new IniConfigApplicationContextInitializer())
                .run(args);
    }

}