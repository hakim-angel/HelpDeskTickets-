package com.example.helpdeskticket.service;

import com.example.helpdeskticket.model.User;
import com.example.helpdeskticket.model.UserProfile;
import com.example.helpdeskticket.repository.UserProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    // Basic CRUD operations
    @Transactional(readOnly = true)
    public Page<UserProfile> findAll(Pageable pageable) {
        return userProfileRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<UserProfile> findById(Long id) {
        return userProfileRepository.findById(id);
    }

    public UserProfile createUserProfile(UserProfile userProfile) {
        // Validate required user association
        if (userProfile.getUser() == null) {
            throw new IllegalArgumentException("UserProfile must be associated with a User");
        }

        // Ensure one-to-one relationship (no existing profile for this user)
        if (userProfileRepository.existsByUser(userProfile.getUser())) {
            throw new IllegalArgumentException("User already has a profile");
        }

        // Validate phone format if provided
        if (userProfile.getPhone() != null && !isValidPhoneNumber(userProfile.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        // Validate bio length if provided
        if (userProfile.getBio() != null && userProfile.getBio().length() > 500) {
            throw new IllegalArgumentException("Bio cannot exceed 500 characters");
        }

        return userProfileRepository.save(userProfile);
    }

    public UserProfile updateUserProfile(Long id, UserProfile profileDetails) {
        UserProfile existingProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UserProfile not found with id: " + id));

        // Validate phone format if provided
        if (profileDetails.getPhone() != null && !isValidPhoneNumber(profileDetails.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        // Validate bio length if provided
        if (profileDetails.getBio() != null && profileDetails.getBio().length() > 500) {
            throw new IllegalArgumentException("Bio cannot exceed 500 characters");
        }

        // Update fields (user cannot be changed in profile update)
        existingProfile.setBio(profileDetails.getBio());
        existingProfile.setPhone(profileDetails.getPhone());

        return userProfileRepository.save(existingProfile);
    }

    public void deleteUserProfile(Long id) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UserProfile not found with id: " + id));
        
        userProfileRepository.deleteById(id);
    }

    // User-specific operations
    @Transactional(readOnly = true)
    public Optional<UserProfile> findByUser(User user) {
        return userProfileRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public Optional<UserProfile> findByUserId(Long userId) {
        // Create a user object with just the ID to search
        User user = new User();
        user.setId(userId);
        return userProfileRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public boolean existsByUser(User user) {
        return userProfileRepository.existsByUser(user);
    }

    @Transactional(readOnly = true)
    public boolean existsByUserId(Long userId) {
        User user = new User();
        user.setId(userId);
        return userProfileRepository.existsByUser(user);
    }

    // Profile management operations
    public UserProfile createOrUpdateProfile(User user, String bio, String phone) {
        Optional<UserProfile> existingProfile = findByUser(user);
        
        if (existingProfile.isPresent()) {
            // Update existing profile
            UserProfile profile = existingProfile.get();
            profile.setBio(bio);
            profile.setPhone(phone);
            return userProfileRepository.save(profile);
        } else {
            // Create new profile
            UserProfile newProfile = new UserProfile();
            newProfile.setUser(user);
            newProfile.setBio(bio);
            newProfile.setPhone(phone);
            return userProfileRepository.save(newProfile);
        }
    }

    public UserProfile updateBio(Long userId, String bio) {
        Optional<UserProfile> profileOpt = findByUserId(userId);
        if (profileOpt.isPresent()) {
            UserProfile profile = profileOpt.get();
            
            // Validate bio length
            if (bio != null && bio.length() > 500) {
                throw new IllegalArgumentException("Bio cannot exceed 500 characters");
            }
            
            profile.setBio(bio);
            return userProfileRepository.save(profile);
        }
        throw new IllegalArgumentException("UserProfile not found for user id: " + userId);
    }

    public UserProfile updatePhone(Long userId, String phone) {
        Optional<UserProfile> profileOpt = findByUserId(userId);
        if (profileOpt.isPresent()) {
            UserProfile profile = profileOpt.get();
            
            // Validate phone format
            if (phone != null && !isValidPhoneNumber(phone)) {
                throw new IllegalArgumentException("Invalid phone number format");
            }
            
            profile.setPhone(phone);
            return userProfileRepository.save(profile);
        }
        throw new IllegalArgumentException("UserProfile not found for user id: " + userId);
    }

    // Validation methods - CHANGED TO PUBLIC
    public boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional
        }
        
        // Basic phone validation - adjust regex based on your requirements
        return phone.matches("^[+]?[0-9\\s\\-\\(\\)]{10,20}$");
    }

    public boolean isValidBio(String bio) {
        return bio == null || bio.length() <= 500;
    }

    // Search operations (if you implement the custom query)
    /*
    @Transactional(readOnly = true)
    public List<UserProfile> findByPhoneContaining(String phone) {
        return userProfileRepository.findByPhoneContaining(phone);
    }
    */

    // Utility methods
    @Transactional(readOnly = true)
    public String getUserBio(Long userId) {
        return findByUserId(userId)
                .map(UserProfile::getBio)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public String getUserPhone(Long userId) {
        return findByUserId(userId)
                .map(UserProfile::getPhone)
                .orElse(null);
    }

    // Profile completeness check
    @Transactional(readOnly = true)
    public boolean isProfileComplete(Long userId) {
        Optional<UserProfile> profile = findByUserId(userId);
        return profile.map(p -> p.getBio() != null && p.getPhone() != null)
                     .orElse(false);
    }

    @Transactional(readOnly = true)
    public double getProfileCompletenessPercentage(Long userId) {
        Optional<UserProfile> profile = findByUserId(userId);
        if (profile.isEmpty()) {
            return 0.0;
        }

        UserProfile p = profile.get();
        int filledFields = 0;
        int totalFields = 2; // bio and phone

        if (p.getBio() != null && !p.getBio().trim().isEmpty()) {
            filledFields++;
        }
        if (p.getPhone() != null && !p.getPhone().trim().isEmpty()) {
            filledFields++;
        }

        return (filledFields * 100.0) / totalFields;
    }

    // Bulk operations
    public void deleteProfilesByUserIds(List<Long> userIds) {
        for (Long userId : userIds) {
            findByUserId(userId).ifPresent(profile -> 
                userProfileRepository.deleteById(profile.getId())
            );
        }
    }

    // Statistics
    @Transactional(readOnly = true)
    public long getTotalProfileCount() {
        return userProfileRepository.count();
    }

    @Transactional(readOnly = true)
    public long getProfilesWithPhoneCount() {
        // This would be more efficient with a custom repository method
        return userProfileRepository.findAll().stream()
                .filter(profile -> profile.getPhone() != null && !profile.getPhone().trim().isEmpty())
                .count();
    }

    @Transactional(readOnly = true)
    public long getProfilesWithBioCount() {
        // This would be more efficient with a custom repository method
        return userProfileRepository.findAll().stream()
                .filter(profile -> profile.getBio() != null && !profile.getBio().trim().isEmpty())
                .count();
    }

    // Soft delete compatibility
    public void softDeleteUserProfile(Long id) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UserProfile not found with id: " + id));
        
        userProfileRepository.deleteById(id);
    }

    public void softDeleteProfileByUserId(Long userId) {
        findByUserId(userId).ifPresent(profile -> 
            userProfileRepository.deleteById(profile.getId())
        );
    }
}