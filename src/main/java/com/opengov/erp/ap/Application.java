package com.opengov.erp.ap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        
        // Disable web server if command-line arguments are provided (batch job execution)
        if (args.length > 0) {
            app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
        }
        
        app.run(args);
    }
}
