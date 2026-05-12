package com.gdoc.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gdoc")
public class GdocServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GdocServerApplication.class, args);
    }
}
