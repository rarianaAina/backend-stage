package com.nrstudio.portail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PortailApplication {
  public static void main(String[] args) { SpringApplication.run(PortailApplication.class, args); }
}