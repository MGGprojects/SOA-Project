package com.example.eventservice.repository;

import com.example.eventservice.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    /** Find events by business ID */
    Page<Event> findByBusinessId(Long businessId, Pageable pageable);

    /** Find events by city (case-insensitive) */
    Page<Event> findByCityIgnoreCase(String city, Pageable pageable);

    /** Find events by city and business ID */
    Page<Event> findByCityIgnoreCaseAndBusinessId(String city, Long businessId, Pageable pageable);

    /** Find events on a specific date (events whose start or end falls on that day) */
    @Query("SELECT e FROM Event e WHERE e.startTime >= :dayStart AND e.startTime < :dayEnd")
    Page<Event> findByDate(@Param("dayStart") Instant dayStart, @Param("dayEnd") Instant dayEnd, Pageable pageable);

    /** Find events by city and date */
    @Query("SELECT e FROM Event e WHERE LOWER(e.city) = LOWER(:city) AND e.startTime >= :dayStart AND e.startTime < :dayEnd")
    Page<Event> findByCityAndDate(@Param("city") String city,
                                  @Param("dayStart") Instant dayStart,
                                  @Param("dayEnd") Instant dayEnd,
                                  Pageable pageable);

    /** Find events by businessId and date */
    @Query("SELECT e FROM Event e WHERE e.businessId = :businessId AND e.startTime >= :dayStart AND e.startTime < :dayEnd")
    Page<Event> findByBusinessIdAndDate(@Param("businessId") Long businessId,
                                        @Param("dayStart") Instant dayStart,
                                        @Param("dayEnd") Instant dayEnd,
                                        Pageable pageable);

    /** Find events at a specific venue that overlap with a given time range */
    @Query("SELECT e FROM Event e WHERE LOWER(e.venue) = LOWER(:venue) AND e.startTime < :endTime AND e.endTime > :startTime")
    List<Event> findOverlappingEvents(@Param("venue") String venue,
                                      @Param("startTime") Instant startTime,
                                      @Param("endTime") Instant endTime);

    /** Find events at a specific venue on a given date */
    @Query("SELECT e FROM Event e WHERE LOWER(e.venue) = LOWER(:venue) AND e.startTime >= :dayStart AND e.startTime < :dayEnd ORDER BY e.startTime")
    List<Event> findByVenueAndDate(@Param("venue") String venue,
                                   @Param("dayStart") Instant dayStart,
                                   @Param("dayEnd") Instant dayEnd);
}
