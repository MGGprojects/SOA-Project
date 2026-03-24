package com.example.eventservice.service;

import com.example.eventservice.client.BusinessClient;
import com.example.eventservice.client.UserClient;
import com.example.eventservice.dto.BusinessPermissionResponse;
import com.example.eventservice.dto.CreateEventRequest;
import com.example.eventservice.dto.EventCreatedMessage;
import com.example.eventservice.dto.UserValidationResponse;
import com.example.eventservice.messaging.EventPublisher;
import com.example.eventservice.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final UserClient userClient;
    private final BusinessClient businessClient;
    private final EventPublisher eventPublisher;

    private final List<Event> events = new ArrayList<>();
    private Long currentId = 1L;

    public Event createEvent(CreateEventRequest request) {
        UserValidationResponse userValidation = userClient.validateUser(request.getUserId());

        if (!userValidation.isValid()) {
            throw new RuntimeException("This user is not valid.");
        }

        BusinessPermissionResponse businessPermission =
                businessClient.canCreateEvents(request.getBusinessId(), request.getUserId());

        if (!businessPermission.isCanCreateEvents()) {
            throw new RuntimeException("This business doesn't allow this user to create events.");
        }

        Event event = new Event(
                currentId++,
                request.getUserId(),
                request.getBusinessId(),
                request.getTitle(),
                request.getVenue(),
                request.getStartTime(),
                request.getEndTime()
        );

        events.add(event);

        EventCreatedMessage message = new EventCreatedMessage(
                event.getId(),
                event.getTitle(),
                event.getVenue()
        );

        eventPublisher.publishEventCreated(message);

        return event;
    }

    public List<Event> getAllEvents() {
        return events;
    }

    public boolean deleteEvent(Long id) {
        return events.removeIf(event -> event.getId().equals(id));
    }
}