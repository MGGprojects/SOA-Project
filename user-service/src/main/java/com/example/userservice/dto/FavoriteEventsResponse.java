package com.example.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response object containing a user's favorite events")
public class FavoriteEventsResponse {

    @Schema(description = "List of favorite event IDs", example = "[\"evt-001\", \"evt-002\"]")
    private List<String> events;

    @Schema(description = "Total number of favorite events", example = "2")
    private int total;

    public FavoriteEventsResponse() {}

    public FavoriteEventsResponse(List<String> events, int total) {
        this.events = events;
        this.total = total;
    }

    public List<String> getEvents() { return events; }
    public void setEvents(List<String> events) { this.events = events; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
