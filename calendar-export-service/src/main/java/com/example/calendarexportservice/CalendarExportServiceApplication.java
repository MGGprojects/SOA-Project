package com.example.calendarexportservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CalendarExportServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalendarExportServiceApplication.class, args);
    }

}
