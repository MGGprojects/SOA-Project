package com.example.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO mirroring the response from the Business Service's GET /api/businesses/{id} endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessProfileResponse {
    private String businessId;
    private String name;
    private String address;
    private String city;
    private String contactEmail;
    private String phone;
    private String description;
    private String ownerId;
}
