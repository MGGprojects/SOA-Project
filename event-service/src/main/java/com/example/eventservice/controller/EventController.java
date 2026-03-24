package com.example.eventservice.controller;

import com.example.eventservice.dto.CreateEventRequest;
import com.example.eventservice.model.Event;
import com.example.eventservice.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public Event createEvent(@RequestBody CreateEventRequest request) {
        return eventService.createEvent(request);
    }

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        boolean deleted = eventService.deleteEvent(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}