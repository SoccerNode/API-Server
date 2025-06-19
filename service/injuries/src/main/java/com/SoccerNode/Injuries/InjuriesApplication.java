package com.SoccerNode.Injuries;

import com.hooniegit.SpringInitializer.IniConfigApplicationContextInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class InjuriesApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(InjuriesApplication.class)
                .initializers(new IniConfigApplicationContextInitializer())
                .run(args);
    }

}