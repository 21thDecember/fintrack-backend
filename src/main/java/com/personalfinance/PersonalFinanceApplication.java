package com.personalfinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class PersonalFinanceApplication {

    public static void main(String[] args) {
        // Thiết lập múi giờ mặc định sang UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(PersonalFinanceApplication.class, args);
    }
}
