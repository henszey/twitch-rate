package com.timelessname.watcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan
@EnableScheduling
@EnableAutoConfiguration
public class App {

  public static void main(String[] args) throws InterruptedException {
    SpringApplication.run(App.class, args);
  }

}