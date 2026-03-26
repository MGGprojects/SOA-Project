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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                + "The service internally calls the Event Service to fetch event details.\n\n"
                + "**Note:** This endpoint calls the Event Service at http://localhost:8082 to retrieve event data, "
                + "then generates a standard iCalendar (.ics) file.\n\n"
                + "**Dummy Values (from Event Service):**\n"
                + "- eventId: any UUID (e.g., \"550e8400-e29b-41d4-a716-446655440000\")\n"
                + "- Returns .ics file with event details from Event Service"
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
    public ResponseEntity<byte[]> exportEventAsIcs(
        @Parameter(description = "Event ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String eventId
    ) {
        String icsContent = calendarExportService.generateIcsFile(eventId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"event.ics\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(icsContent.getBytes());
    }

    @GetMapping("/events/{eventId}/link")
    @Operation(
        summary = "Get calendar links for an event",
        description = "Returns pre-formatted calendar links for Google Calendar, Outlook, and a direct .ics download link "
                + "for easy one-click adding of an event to a calendar.\n\n"
                + "**Note:** This endpoint calls the Event Service at http://localhost:8082 to retrieve event data, "
                + "then generates calendar links.\n\n"
                + "**Dummy Values (from Event Service):**\n"
                + "- eventId: any UUID (e.g., \"550e8400-e29b-41d4-a716-446655440000\")\n"
                + "- Returns Google Calendar, Outlook, and .ics download links"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Calendar links generated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CalendarLinksResponse.class),
                examples = @ExampleObject(
                    value = "{\"calendarLink\":\"http://localhost:8085/api/exports/events/550e8400-e29b-41d4-a716-446655440000\",\"googleLink\":\"https://calendar.google.com/calendar/render?action=TEMPLATE&text=Tech+Conference+2026&dates=20260615T090000Z/20260615T170000Z&details=Annual+technology+conference&location=Convention+Center\",\"outlookLink\":\"https://outlook.live.com/calendar/0/action/compose?allday=false&subject=Tech+Conference+2026&startdt=2026-06-15T09%3A00%3A00Z&enddt=2026-06-15T17%3A00%3A00Z&body=Annual+technology+conference&location=Convention+Center\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Event not found"),
        @ApiResponse(responseCode = "502", description = "Event Service unavailable")
    })
    public ResponseEntity<CalendarLinksResponse> getCalendarLinks(
        @Parameter(description = "Event ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String eventId
    ) {
        CalendarLinksResponse response = calendarExportService.generateCalendarLinks(eventId);
        return ResponseEntity.ok(response);
    }
}
