package com.example.helpdeskticket.controller;

import com.example.helpdeskticket.model.UserProfile;
import com.example.helpdeskticket.service.UserProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user-profiles")
@CrossOrigin(origins = "*")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ResponseEntity<Page<UserProfile>> getAllProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserProfile> profiles = userProfileService.findAll(pageable);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getProfileById(@PathVariable Long id) {
        Optional<UserProfile> profile = userProfileService.findById(id);
        return profile.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserProfile> createProfile(@RequestBody UserProfile profile) {
        try {
            UserProfile savedProfile = userProfileService.createUserProfile(profile);
            return ResponseEntity.ok(savedProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> updateProfile(@PathVariable Long id, @RequestBody UserProfile profile) {
        try {
            UserProfile updatedProfile = userProfileService.updateUserProfile(id, profile);
            return ResponseEntity.ok(updatedProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        try {
            userProfileService.deleteUserProfile(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfile> getProfileByUserId(@PathVariable Long userId) {
        Optional<UserProfile> profile = userProfileService.findByUserId(userId);
        return profile.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/exists")
    public ResponseEntity<Boolean> checkProfileExists(@PathVariable Long userId) {
        boolean exists = userProfileService.existsByUserId(userId);
        return ResponseEntity.ok(exists);
    }

    @PatchMapping("/user/{userId}/bio")
    public ResponseEntity<UserProfile> updateBio(@PathVariable Long userId, @RequestBody String bio) {
        try {
            UserProfile updatedProfile = userProfileService.updateBio(userId, bio);
            return ResponseEntity.ok(updatedProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/user/{userId}/phone")
    public ResponseEntity<UserProfile> updatePhone(@PathVariable Long userId, @RequestBody String phone) {
        try {
            UserProfile updatedProfile = userProfileService.updatePhone(userId, phone);
            return ResponseEntity.ok(updatedProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}/completeness")
    public ResponseEntity<Double> getProfileCompleteness(@PathVariable Long userId) {
        double completeness = userProfileService.getProfileCompletenessPercentage(userId);
        return ResponseEntity.ok(completeness);
    }

    @GetMapping("/user/{userId}/complete")
    public ResponseEntity<Boolean> isProfileComplete(@PathVariable Long userId) {
        boolean isComplete = userProfileService.isProfileComplete(userId);
        return ResponseEntity.ok(isComplete);
    }

    @GetMapping("/user/{userId}/bio")
    public ResponseEntity<String> getUserBio(@PathVariable Long userId) {
        String bio = userProfileService.getUserBio(userId);
        return ResponseEntity.ok(bio != null ? bio : "");
    }

    @GetMapping("/user/{userId}/phone")
    public ResponseEntity<String> getUserPhone(@PathVariable Long userId) {
        String phone = userProfileService.getUserPhone(userId);
        return ResponseEntity.ok(phone != null ? phone : "");
    }

    @PostMapping("/user/{userId}/complete")
    public ResponseEntity<UserProfile> createOrUpdateProfile(
            @PathVariable Long userId,
            @RequestParam String bio,
            @RequestParam String phone) {
        try {
            // You'll need to fetch the user object here
            // For now, this is a placeholder implementation
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/statistics/count")
    public ResponseEntity<Long> getTotalProfileCount() {
        long count = userProfileService.getTotalProfileCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/with-phone")
    public ResponseEntity<Long> getProfilesWithPhoneCount() {
        long count = userProfileService.getProfilesWithPhoneCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/with-bio")
    public ResponseEntity<Long> getProfilesWithBioCount() {
        long count = userProfileService.getProfilesWithBioCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/validate/phone/{phone}")
    public ResponseEntity<Boolean> validatePhoneNumber(@PathVariable String phone) {
        boolean isValid = userProfileService.isValidPhoneNumber(phone);
        return ResponseEntity.ok(isValid);
    }
}