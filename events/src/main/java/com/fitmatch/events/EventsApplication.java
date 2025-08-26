package com.fitmatch.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.fitmatch"})
@EnableFeignClients(basePackages = "com.fitmatch.events.client")
public class EventsApplication {

  public static void main(String[] args) {
    SpringApplication.run(EventsApplication.class, args);
  }
}
