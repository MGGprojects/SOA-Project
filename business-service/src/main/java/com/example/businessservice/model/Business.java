package com.example.businessservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "businesses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    private String city;

    private String contactEmail;

    private String phone;

    @Column(length = 1000)
    private String description;

    /** The user ID of the business owner */
    @Column(nullable = false)
    private Long ownerId;
}
