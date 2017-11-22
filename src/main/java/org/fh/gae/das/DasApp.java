package org.fh.gae.das;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DasApp {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DasApp.class);
        app.addListeners(new DasAppEventListener());
        app.run(args);
    }
}
