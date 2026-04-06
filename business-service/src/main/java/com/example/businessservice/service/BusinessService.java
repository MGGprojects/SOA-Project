package com.example.businessservice.service;

import com.example.businessservice.client.EventClient;
import com.example.businessservice.dto.*;
import com.example.businessservice.model.Business;
import com.example.businessservice.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final EventClient eventClient;

    /**
     * Creates a new business profile.
     */
    public BusinessProfileResponse createBusiness(CreateBusinessRequest request, Long ownerId) {
        Business business = new Business();
        business.setName(request.getName());
        business.setAddress(request.getAddress());
        business.setCity(request.getCity());
        business.setContactEmail(request.getContactEmail());
        business.setPhone(request.getPhone());
        business.setDescription(request.getDescription());
        business.setOwnerId(ownerId);

        Business saved = businessRepository.save(business);
        return toProfileResponse(saved);
    }

    /**
     * Retrieves a business profile by ID.
     */
    public Optional<BusinessProfileResponse> getBusiness(Long businessId) {
        return businessRepository.findById(businessId)
                .map(this::toProfileResponse);
    }

    /**
     * Updates an existing business profile.
     */
    public Optional<BusinessProfileResponse> updateBusiness(Long businessId, CreateBusinessRequest request) {
        return businessRepository.findById(businessId)
                .map(existing -> {
                    if (request.getName() != null) existing.setName(request.getName());
                    if (request.getAddress() != null) existing.setAddress(request.getAddress());
                    if (request.getCity() != null) existing.setCity(request.getCity());
                    if (request.getContactEmail() != null) existing.setContactEmail(request.getContactEmail());
                    if (request.getPhone() != null) existing.setPhone(request.getPhone());
                    if (request.getDescription() != null) existing.setDescription(request.getDescription());
                    Business updated = businessRepository.save(existing);
                    return toProfileResponse(updated);
                });
    }

    /**
     * Lists businesses with optional city filter and pagination.
     */
    public BusinessListResponse listBusinesses(String city, int page, int limit) {
        // page is 1-based from the API, convert to 0-based for Spring Data
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), limit);

        Page<Business> businessPage;
        if (city != null && !city.isBlank()) {
            businessPage = businessRepository.findByCityIgnoreCase(city, pageable);
        } else {
            businessPage = businessRepository.findAll(pageable);
        }

        List<BusinessProfileResponse> businesses = businessPage.getContent().stream()
                .map(this::toProfileResponse)
                .collect(Collectors.toList());

        return new BusinessListResponse(businesses, (int) businessPage.getTotalElements(), page);
    }

    /**
     * Gets all events for a business by calling the Event Service via Feign.
     */
    public List<EventResponse> getBusinessEvents(Long businessId) {
        // Verify the business exists first
        if (!businessRepository.existsById(businessId)) {
            return null; // signals not found
        }

        try {
            EventListWrapper wrapper = eventClient.getEvents(String.valueOf(businessId), 1, 100);
            return wrapper != null && wrapper.getEvents() != null
                    ? wrapper.getEvents()
                    : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch events from Event Service for businessId={}: {}", businessId, e.getMessage());
            // Return empty list if event service is unavailable
            return Collections.emptyList();
        }
    }

    /**
     * Checks whether a user has permission to create events for a given business.
     * A user can create events if they are the owner of the business.
     */
    public BusinessPermissionResponse canCreateEvents(Long businessId, Long userId) {
        boolean allowed = businessRepository.findById(businessId)
                .map(business -> business.getOwnerId().equals(userId))
                .orElse(false);

        return new BusinessPermissionResponse(businessId, userId, allowed);
    }

    /**
     * Converts a Business entity to a BusinessProfileResponse DTO.
     */
    private BusinessProfileResponse toProfileResponse(Business business) {
        return new BusinessProfileResponse(
                String.valueOf(business.getId()),
                business.getName(),
                business.getAddress(),
                business.getCity(),
                business.getContactEmail(),
                business.getPhone(),
                business.getDescription(),
                String.valueOf(business.getOwnerId())
        );
    }
}
