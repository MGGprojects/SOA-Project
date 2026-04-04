package com.example.businessservice.repository;

import com.example.businessservice.model.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    /** Find businesses filtered by city (case-insensitive) */
    Page<Business> findByCityIgnoreCase(String city, Pageable pageable);

    /** Find all businesses owned by a specific user */
    List<Business> findByOwnerId(Long ownerId);
}
