package com.SoccerNode.FixturesLineups;

import com.hooniegit.SpringInitializer.IniConfigApplicationContextInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FixturesLineupsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FixturesLineupsApplication.class)
                .initializers(new IniConfigApplicationContextInitializer())
                .run(args);
    }

}