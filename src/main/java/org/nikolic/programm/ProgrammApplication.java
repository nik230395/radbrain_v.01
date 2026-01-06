package org.nikolic.programm;

import org.springframework.boot.SpringApplication;
import org.springframework. boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProgrammApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProgrammApplication. class, args);
    }
}