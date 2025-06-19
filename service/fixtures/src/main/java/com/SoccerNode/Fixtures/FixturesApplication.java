package com.SoccerNode.Fixtures;

import com.hooniegit.SpringInitializer.IniConfigApplicationContextInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FixturesApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FixturesApplication.class)
                .initializers(new IniConfigApplicationContextInitializer())
                .run(args);
    }

}