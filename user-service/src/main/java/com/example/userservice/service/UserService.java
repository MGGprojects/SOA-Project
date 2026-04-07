package com.example.userservice.service;

import com.example.userservice.dto.*;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Creates a new user profile.
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request, Long authUserId, String email) {
        if (userRepository.findByAuthUserId(authUserId).isPresent()) {
            throw new IllegalArgumentException("A profile already exists for auth user '" + authUserId + "'");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setAuthUserId(authUserId);
        user.setEmail(email);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        User saved = userRepository.save(user);
        log.info("Created user profile: id={}, email={}", saved.getId(), saved.getEmail());
        return toUserResponse(saved);
    }

    /**
     * Retrieves a user profile by ID.
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUser(String userId) {
        return userRepository.findById(userId)
                .map(this::toUserResponse);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserByAuthUserId(Long authUserId) {
        return userRepository.findByAuthUserId(authUserId)
                .map(this::toUserResponse);
    }

    /**
     * Updates an existing user profile. Only non-null fields are updated.
     */
    @Transactional
    public Optional<UserResponse> updateUser(String userId, UpdateUserRequest request) {
        return userRepository.findById(userId)
                .map(existing -> {
                    if (request.getFirstName() != null) {
                        existing.setFirstName(request.getFirstName());
                    }
                    if (request.getLastName() != null) {
                        existing.setLastName(request.getLastName());
                    }
                    User updated = userRepository.save(existing);
                    log.info("Updated user profile: id={}", updated.getId());
                    return toUserResponse(updated);
                });
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserEntity(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * Adds an event to the user's favourites.
     */
    @Transactional
    public boolean addFavoriteEvent(String userId, String eventId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();
        user.getFavoriteEventIds().add(eventId);
        userRepository.save(user);
        log.info("Added event {} to favorites for user {}", eventId, userId);
        return true;
    }

    /**
     * Removes an event from the user's favourites.
     */
    @Transactional
    public boolean removeFavoriteEvent(String userId, String eventId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();
        boolean removed = user.getFavoriteEventIds().remove(eventId);
        if (removed) {
            userRepository.save(user);
            log.info("Removed event {} from favorites for user {}", eventId, userId);
        }
        return true; // user exists, operation is valid even if event wasn't in favorites
    }

    /**
     * Returns the user's favourite event IDs with pagination.
     */
    @Transactional(readOnly = true)
    public Optional<FavoriteEventsResponse> getFavoriteEvents(String userId, int page, int limit) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return Optional.empty();
        }

        User user = optUser.get();
        List<String> allFavorites = new ArrayList<>(user.getFavoriteEventIds());
        int total = allFavorites.size();

        // Apply pagination (page is 1-based)
        int fromIndex = Math.max((page - 1) * limit, 0);
        int toIndex = Math.min(fromIndex + limit, total);

        List<String> pagedFavorites;
        if (fromIndex >= total) {
            pagedFavorites = List.of();
        } else {
            pagedFavorites = allFavorites.subList(fromIndex, toIndex);
        }

        return Optional.of(new FavoriteEventsResponse(pagedFavorites, total));
    }

    /**
     * Converts a User entity to a UserResponse DTO.
     */
    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getAuthUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt().toString()
        );
    }
}
