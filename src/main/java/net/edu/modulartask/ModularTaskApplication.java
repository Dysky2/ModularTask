package net.edu.modulartask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ModularTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModularTaskApplication.class, args);
    }

}
