package com.hooniegit.Archiver;

import com.hooniegit.SpringInitializer.IniConfigApplicationContextInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Run Command
 * $ java -Dconfig.path=D:/WAT/interface/config/archiver/config.ini -jar ArchiverApplication.jar
 */
@EnableScheduling
@SpringBootApplication
public class ArchiverApplication {

    public static void main(String[] args) {
        // Run Spring Application with .ini Initializer
        new SpringApplicationBuilder(ArchiverApplication.class)
				.initializers(new IniConfigApplicationContextInitializer())
                .run(args);
    }

}