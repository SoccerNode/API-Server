package com.SoccerNode.FixturesStatistics;

import com.hooniegit.SpringInitializer.IniConfigApplicationContextInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FixturesStatisticsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FixturesStatisticsApplication.class)
                .initializers(new IniConfigApplicationContextInitializer())
                .run(args);
    }

}