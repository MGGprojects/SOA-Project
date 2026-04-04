package com.example.eventservice.service;

import com.example.eventservice.client.BusinessClient;
import com.example.eventservice.dto.*;
import com.example.eventservice.messaging.EventPublisher;
import com.example.eventservice.model.Event;
import com.example.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final BusinessClient businessClient;
    private final EventPublisher eventPublisher;

    // ── Venue operating hours (configurable defaults) ────────────────────
    private static final int VENUE_OPEN_HOUR = 8;   // 08:00
    private static final int VENUE_CLOSE_HOUR = 23;  // 23:00

    /**
     * Creates a new event.
     * - Calls Business Service to verify the user has permission to create events for the business.
     * - Checks for venue time conflicts.
     * - Persists to database.
     * - Publishes an event-created message to RabbitMQ.
     */
    public EventResponse createEvent(CreateEventRequest request, Long userId) {
        Long businessId = parseBusinessId(request.getBusinessId());

        // 1. Check permission via Business Service
        try {
            BusinessPermissionResponse permission = businessClient.canCreateEvents(businessId, userId);
            if (!permission.isCanCreateEvents()) {
                throw new IllegalStateException("User " + userId + " is not allowed to create events for business " + businessId);
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not verify business permission (business-service may be down): {}", e.getMessage());
            // Fail open for now – in production you'd fail closed
        }

        // 2. Parse times
        Instant startTime = Instant.parse(request.getStartTime());
        Instant endTime = Instant.parse(request.getEndTime());

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }

        // 3. Check venue conflicts
        List<Event> conflicts = eventRepository.findOverlappingEvents(request.getVenue(), startTime, endTime);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Venue '" + request.getVenue() + "' has a scheduling conflict for the requested time range");
        }

        // 4. Persist
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setVenue(request.getVenue());
        event.setCity(request.getCity());
        event.setBusinessId(businessId);
        event.setCreatorUserId(userId);

        Event saved = eventRepository.save(event);

        // 5. Publish to RabbitMQ (fire-and-forget)
        try {
            EventCreatedMessage message = new EventCreatedMessage(
                    saved.getId().toString(),
                    saved.getTitle(),
                    saved.getVenue(),
                    saved.getStartTime().toString(),
                    saved.getEndTime().toString(),
                    saved.getBusinessId()
            );
            eventPublisher.publishEventCreated(message);
            log.info("Published event-created message for eventId={}", saved.getId());
        } catch (Exception e) {
            log.warn("Failed to publish event-created message: {}", e.getMessage());
        }

        return toEventResponse(saved);
    }

    /**
     * Searches / lists events with optional filters and pagination.
     */
    public EventListResponse getEvents(String city, String date, String businessId, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), limit, Sort.by("startTime").ascending());

        Page<Event> eventPage;

        Instant dayStart = null;
        Instant dayEnd = null;
        if (date != null && !date.isBlank()) {
            try {
                LocalDate localDate = LocalDate.parse(date);
                dayStart = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                dayEnd = localDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            } catch (DateTimeParseException e) {
                log.warn("Invalid date format '{}', ignoring date filter", date);
            }
        }

        Long bizId = null;
        if (businessId != null && !businessId.isBlank()) {
            try {
                bizId = Long.parseLong(businessId);
            } catch (NumberFormatException e) {
                log.warn("Invalid businessId '{}', ignoring filter", businessId);
            }
        }

        boolean hasCity = city != null && !city.isBlank();
        boolean hasDate = dayStart != null;
        boolean hasBiz = bizId != null;

        if (hasCity && hasDate) {
            eventPage = eventRepository.findByCityAndDate(city, dayStart, dayEnd, pageable);
        } else if (hasCity && hasBiz) {
            eventPage = eventRepository.findByCityIgnoreCaseAndBusinessId(city, bizId, pageable);
        } else if (hasCity) {
            eventPage = eventRepository.findByCityIgnoreCase(city, pageable);
        } else if (hasDate && hasBiz) {
            eventPage = eventRepository.findByBusinessIdAndDate(bizId, dayStart, dayEnd, pageable);
        } else if (hasDate) {
            eventPage = eventRepository.findByDate(dayStart, dayEnd, pageable);
        } else if (hasBiz) {
            eventPage = eventRepository.findByBusinessId(bizId, pageable);
        } else {
            eventPage = eventRepository.findAll(pageable);
        }

        List<EventResponse> events = eventPage.getContent().stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());

        return new EventListResponse(events, (int) eventPage.getTotalElements(), page);
    }

    /**
     * Returns detailed event information including business info.
     */
    public Optional<EventDetailResponse> getEventById(String eventId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(eventId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        return eventRepository.findById(uuid)
                .map(event -> {
                    // Try to fetch business info from Business Service
                    EventDetailResponse.BusinessInfo businessInfo;
                    try {
                        BusinessProfileResponse profile = businessClient.getBusinessProfile(event.getBusinessId());
                        businessInfo = new EventDetailResponse.BusinessInfo(
                                profile.getBusinessId(),
                                profile.getName()
                        );
                    } catch (Exception e) {
                        log.warn("Could not fetch business profile for businessId={}: {}", event.getBusinessId(), e.getMessage());
                        businessInfo = new EventDetailResponse.BusinessInfo(
                                String.valueOf(event.getBusinessId()),
                                "Unknown Business"
                        );
                    }

                    return new EventDetailResponse(
                            event.getId().toString(),
                            event.getTitle(),
                            event.getDescription(),
                            event.getStartTime().toString(),
                            event.getEndTime().toString(),
                            event.getVenue(),
                            businessInfo
                    );
                });
    }

    /**
     * Updates an existing event.
     */
    public Optional<EventResponse> updateEvent(String eventId, UpdateEventRequest request, Long userId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(eventId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        return eventRepository.findById(uuid)
                .map(existing -> {
                    // Update fields if provided
                    if (request.getTitle() != null) existing.setTitle(request.getTitle());
                    if (request.getDescription() != null) existing.setDescription(request.getDescription());

                    Instant newStart = existing.getStartTime();
                    Instant newEnd = existing.getEndTime();
                    String newVenue = existing.getVenue();

                    if (request.getStartTime() != null) {
                        newStart = Instant.parse(request.getStartTime());
                        existing.setStartTime(newStart);
                    }
                    if (request.getEndTime() != null) {
                        newEnd = Instant.parse(request.getEndTime());
                        existing.setEndTime(newEnd);
                    }
                    if (request.getVenue() != null) {
                        newVenue = request.getVenue();
                        existing.setVenue(newVenue);
                    }

                    // Check venue conflicts (excluding this event itself)
                    List<Event> conflicts = eventRepository.findOverlappingEvents(newVenue, newStart, newEnd);
                    conflicts.removeIf(e -> e.getId().equals(existing.getId()));
                    if (!conflicts.isEmpty()) {
                        throw new IllegalStateException("Venue '" + newVenue + "' has a scheduling conflict for the requested time range");
                    }

                    Event updated = eventRepository.save(existing);
                    return toEventResponse(updated);
                });
    }

    /**
     * Deletes/cancels an event.
     */
    public Optional<DeleteEventResponse> deleteEvent(String eventId, Long userId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(eventId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        return eventRepository.findById(uuid)
                .map(event -> {
                    eventRepository.delete(event);
                    return new DeleteEventResponse("Event cancelled");
                });
    }

    /**
     * Returns venue availability for a given date.
     * Calculates free time slots by subtracting booked events from the venue's operating hours.
     */
    public VenueAvailabilityResponse getVenueAvailability(String venue, String date) {
        String baseDate = (date != null && !date.isBlank()) ? date : LocalDate.now().toString();

        LocalDate localDate;
        try {
            localDate = LocalDate.parse(baseDate);
        } catch (DateTimeParseException e) {
            localDate = LocalDate.now();
            baseDate = localDate.toString();
        }

        Instant dayStart = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant dayEnd = localDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        // Venue operating window
        Instant venueOpen = localDate.atTime(VENUE_OPEN_HOUR, 0).toInstant(ZoneOffset.UTC);
        Instant venueClose = localDate.atTime(VENUE_CLOSE_HOUR, 0).toInstant(ZoneOffset.UTC);

        // Get all events at this venue on this date, sorted by start time
        List<Event> bookedEvents = eventRepository.findByVenueAndDate(venue, dayStart, dayEnd);

        // Calculate free slots
        List<VenueAvailabilityResponse.TimeSlot> freeSlots = new ArrayList<>();
        Instant cursor = venueOpen;

        for (Event event : bookedEvents) {
            Instant eventStart = event.getStartTime();
            Instant eventEnd = event.getEndTime();

            // Clamp to venue hours
            if (eventStart.isBefore(venueOpen)) eventStart = venueOpen;
            if (eventEnd.isAfter(venueClose)) eventEnd = venueClose;

            if (cursor.isBefore(eventStart)) {
                freeSlots.add(new VenueAvailabilityResponse.TimeSlot(
                        cursor.toString(),
                        eventStart.toString()
                ));
            }
            if (eventEnd.isAfter(cursor)) {
                cursor = eventEnd;
            }
        }

        // Remaining time after last event
        if (cursor.isBefore(venueClose)) {
            freeSlots.add(new VenueAvailabilityResponse.TimeSlot(
                    cursor.toString(),
                    venueClose.toString()
            ));
        }

        return new VenueAvailabilityResponse(venue, freeSlots);
    }

    // ── Helper methods ───────────────────────────────────────────────────

    private EventResponse toEventResponse(Event event) {
        return new EventResponse(
                event.getId().toString(),
                event.getTitle(),
                event.getDescription(),
                event.getStartTime().toString(),
                event.getEndTime().toString(),
                event.getVenue(),
                String.valueOf(event.getBusinessId())
        );
    }

    private Long parseBusinessId(String businessId) {
        if (businessId == null || businessId.isBlank()) {
            throw new IllegalArgumentException("businessId is required");
        }
        try {
            return Long.parseLong(businessId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid businessId: " + businessId);
        }
    }
}
