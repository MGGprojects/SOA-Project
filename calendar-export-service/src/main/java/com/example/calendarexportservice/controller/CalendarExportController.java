package com.example.calendarexportservice.controller;

import com.example.calendarexportservice.dto.CalendarLinksResponse;
import com.example.calendarexportservice.service.CalendarExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/exports")
@Tag(name = "Calendar Export", description = "APIs for exporting events as calendar files and generating calendar links")
public class CalendarExportController {

    private final CalendarExportService calendarExportService;

    public CalendarExportController(CalendarExportService calendarExportService) {
        this.calendarExportService = calendarExportService;
    }

    @GetMapping("/events/{eventId}")
    @Operation(
        summary = "Export event as .ics file",
        description = "Generates and returns an .ics calendar file for a specific event. "
                + "The service calls the Event Service via Feign to fetch event details, "
                + "then generates a standard iCalendar (.ics) file."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "ICS file generated successfully",
            content = @Content(
                mediaType = "text/calendar",
                examples = @ExampleObject(
                    value = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//Calendar Export Service//EN\r\nCALSCALE:GREGORIAN\r\nMETHOD:PUBLISH\r\nBEGIN:VEVENT\r\nUID:550e8400-e29b-41d4-a716-446655440000@calendar-export-service\r\nDTSTART:20260615T090000Z\r\nDTEND:20260615T170000Z\r\nSUMMARY:Tech Conference 2026\r\nDESCRIPTION:Annual technology conference\r\nLOCATION:Convention Center\r\nORGANIZER:Tech Corp\r\nEND:VEVENT\r\nEND:VCALENDAR\r\n"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Event not found"),
        @ApiResponse(responseCode = "502", description = "Event Service unavailable")
    })
    public ResponseEntity<?> exportEventAsIcs(
        @Parameter(description = "Event ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String eventId
    ) {
        try {
            String icsContent = calendarExportService.generateIcsFile(eventId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/calendar"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"event.ics\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(icsContent.getBytes());
        } catch (CalendarExportService.EventNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (CalendarExportService.EventServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/events/{eventId}/link")
    @Operation(
        summary = "Get calendar links for an event",
        description = "Returns pre-formatted calendar links for Google Calendar, Outlook, and a direct .ics download link "
                + "for easy one-click adding of an event to a calendar. "
                + "The service calls the Event Service via Feign to retrieve event data."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Calendar links generated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CalendarLinksResponse.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Event not found"),
        @ApiResponse(responseCode = "502", description = "Event Service unavailable")
    })
    public ResponseEntity<?> getCalendarLinks(
        @Parameter(description = "Event ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String eventId
    ) {
        try {
            CalendarLinksResponse response = calendarExportService.generateCalendarLinks(eventId);
            return ResponseEntity.ok(response);
        } catch (CalendarExportService.EventNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (CalendarExportService.EventServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
