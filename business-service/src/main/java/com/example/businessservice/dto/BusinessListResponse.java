package com.example.businessservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessListResponse {
    private List<BusinessProfileResponse> businesses;
    private int total;
    private int page;
}
