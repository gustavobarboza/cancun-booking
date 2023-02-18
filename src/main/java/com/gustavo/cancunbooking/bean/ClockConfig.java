package com.gustavo.cancunbooking.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock () {
        return Clock.systemUTC();
    }

}
