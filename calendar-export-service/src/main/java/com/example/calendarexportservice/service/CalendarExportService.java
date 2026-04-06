package com.example.calendarexportservice.service;

import com.example.calendarexportservice.client.EventClient;
import com.example.calendarexportservice.dto.CalendarLinksResponse;
import com.example.calendarexportservice.dto.EventDetailResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class CalendarExportService {

    private static final DateTimeFormatter ICS_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final EventClient eventClient;

    @Value("${server.port:8085}")
    private String serverPort;

    public CalendarExportService(EventClient eventClient) {
        this.eventClient = eventClient;
    }

    /**
     * Fetches event details from the Event Service via Feign and generates an .ics calendar file content.
     *
     * @throws EventNotFoundException if the event is not found (404 from Event Service)
     * @throws EventServiceUnavailableException if the Event Service is unreachable
     */
    public String generateIcsFile(String eventId) {
        EventDetailResponse event = fetchEvent(eventId);

        String startFormatted = formatToIcs(event.getStartTime());
        String endFormatted = formatToIcs(event.getEndTime());

        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//Calendar Export Service//EN\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");
        ics.append("BEGIN:VEVENT\r\n");
        ics.append("UID:").append(event.getEventId()).append("@calendar-export-service\r\n");
        ics.append("DTSTART:").append(startFormatted).append("\r\n");
        ics.append("DTEND:").append(endFormatted).append("\r\n");
        ics.append("SUMMARY:").append(escapeIcsText(event.getTitle())).append("\r\n");
        ics.append("DESCRIPTION:").append(escapeIcsText(event.getDescription())).append("\r\n");
        ics.append("LOCATION:").append(escapeIcsText(event.getVenue())).append("\r\n");
        if (event.getBusiness() != null) {
            ics.append("ORGANIZER:").append(escapeIcsText(event.getBusiness().getName())).append("\r\n");
        }
        ics.append("END:VEVENT\r\n");
        ics.append("END:VCALENDAR\r\n");

        return ics.toString();
    }

    /**
     * Fetches event details from the Event Service via Feign and generates calendar links
     * for Google Calendar, Outlook, and a generic .ics download link.
     *
     * @throws EventNotFoundException if the event is not found (404 from Event Service)
     * @throws EventServiceUnavailableException if the Event Service is unreachable
     */
    public CalendarLinksResponse generateCalendarLinks(String eventId) {
        EventDetailResponse event = fetchEvent(eventId);

        String googleLink = buildGoogleCalendarLink(event);
        String outlookLink = buildOutlookCalendarLink(event);
        String calendarLink = buildIcsDownloadLink(eventId);

        return new CalendarLinksResponse(calendarLink, googleLink, outlookLink);
    }

    /**
     * Fetches event details from the Event Service.
     * Translates Feign exceptions into domain-specific exceptions.
     */
    private EventDetailResponse fetchEvent(String eventId) {
        try {
            EventDetailResponse event = eventClient.getEventById(eventId);
            if (event == null) {
                throw new EventNotFoundException("Event not found: " + eventId);
            }
            return event;
        } catch (FeignException.NotFound e) {
            log.warn("Event not found in Event Service: eventId={}", eventId);
            throw new EventNotFoundException("Event not found: " + eventId);
        } catch (FeignException e) {
            log.error("Error calling Event Service for eventId={}: status={}, message={}",
                    eventId, e.status(), e.getMessage());
            throw new EventServiceUnavailableException("Event Service unavailable: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error calling Event Service for eventId={}: {}", eventId, e.getMessage());
            throw new EventServiceUnavailableException("Event Service unavailable: " + e.getMessage());
        }
    }

    private String buildGoogleCalendarLink(EventDetailResponse event) {
        String startFormatted = formatToIcs(event.getStartTime());
        String endFormatted = formatToIcs(event.getEndTime());

        return "https://calendar.google.com/calendar/render?action=TEMPLATE"
                + "&text=" + urlEncode(event.getTitle())
                + "&dates=" + startFormatted + "/" + endFormatted
                + "&details=" + urlEncode(event.getDescription())
                + "&location=" + urlEncode(event.getVenue());
    }

    private String buildOutlookCalendarLink(EventDetailResponse event) {
        return "https://outlook.live.com/calendar/0/action/compose?allday=false"
                + "&subject=" + urlEncode(event.getTitle())
                + "&startdt=" + urlEncode(event.getStartTime())
                + "&enddt=" + urlEncode(event.getEndTime())
                + "&body=" + urlEncode(event.getDescription())
                + "&location=" + urlEncode(event.getVenue());
    }

    private String buildIcsDownloadLink(String eventId) {
        return "http://localhost:" + serverPort + "/api/exports/events/" + eventId;
    }

    private String formatToIcs(String isoDateTime) {
        Instant instant = Instant.parse(isoDateTime);
        return ICS_DATE_FORMAT.format(instant);
    }

    private String escapeIcsText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace(",", "\\,")
                   .replace(";", "\\;")
                   .replace("\n", "\\n");
    }

    private String urlEncode(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // ── Custom exception classes ─────────────────────────────────────────

    public static class EventNotFoundException extends RuntimeException {
        public EventNotFoundException(String message) {
            super(message);
        }
    }

    public static class EventServiceUnavailableException extends RuntimeException {
        public EventServiceUnavailableException(String message) {
            super(message);
        }
    }
}
