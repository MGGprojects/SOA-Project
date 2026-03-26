package com.example.calendarexportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalendarLinksResponse {
    private String calendarLink;
    private String googleLink;
    private String outlookLink;
}
