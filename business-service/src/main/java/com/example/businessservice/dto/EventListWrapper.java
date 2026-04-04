package com.example.businessservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper DTO for the response from the Event Service's GET /api/events endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventListWrapper {
    private List<EventResponse> events;
    private int total;
    private int page;
}
