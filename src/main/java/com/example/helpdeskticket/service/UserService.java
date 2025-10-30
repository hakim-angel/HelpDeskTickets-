package com.example.helpdeskticket.service;

import com.example.helpdeskticket.model.User;
import com.example.helpdeskticket.model.Location;
import com.example.helpdeskticket.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Basic CRUD operations
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email '" + user.getEmail() + "' already exists");
        }

        // Validate required fields
        if (user.getLocation() == null) {
            throw new IllegalArgumentException("User must have a location");
        }

        // Hash password before saving
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Check if email is being changed and if it already exists
        if (!existingUser.getEmail().equals(userDetails.getEmail()) && 
            userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException("User with email '" + userDetails.getEmail() + "' already exists");
        }

        // Update fields
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setLocation(userDetails.getLocation());

        // Only update password if provided and not already encrypted
        if (userDetails.getPassword() != null && !userDetails.getPassword().startsWith("$2a$")) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // Check if user has tickets before deletion (optional business rule)
        if (!user.getTickets().isEmpty()) {
            throw new IllegalStateException("Cannot delete user with existing tickets");
        }
        
        userRepository.deleteById(id);
    }

    // Authentication and user management
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Search and filter operations
    @Transactional(readOnly = true)
    public List<User> findByFirstNameContaining(String firstName) {
        return userRepository.findByFirstNameContainingIgnoreCaseOrderByCreatedAtDesc(firstName);
    }

    @Transactional(readOnly = true)
    public Page<User> findByLocationId(Long locationId, Pageable pageable) {
        return userRepository.findByLocationId(locationId, pageable);
    }

    @Transactional(readOnly = true)
    public List<User> findByProvinceCodeOrName(String codeOrName) {
        return userRepository.findByProvinceCodeOrName(codeOrName);
    }

    // Role management
    public void addRoleToUser(Long userId, Object role) {
        // This is a placeholder - you'll need to implement based on your Role entity
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        // Assuming you have a Role entity and user.getRoles() returns a Set<Role>
        // user.getRoles().add(role);
        // userRepository.save(user);
    }

    public void removeRoleFromUser(Long userId, Object role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        // user.getRoles().remove(role);
        // userRepository.save(user);
    }

    // Profile management
    public void updateUserProfile(Long userId, Object userProfile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        // Assuming you have a UserProfile entity
        // user.setUserProfile(userProfile);
        // userRepository.save(user);
    }

    // Location-based operations
    // In UserService, update this method:
// In UserService, update this method:
@Transactional(readOnly = true)
public Optional<Location> findProvinceByUserVillage(Long userId) {
    return userRepository.findProvinceByUserId(userId);
}
    @Transactional(readOnly = true)
    public List<User> findUsersInSameProvince(Long userId) {
        Optional<Location> province = findProvinceByUserVillage(userId);
        if (province.isPresent()) {
            return userRepository.findByProvinceCodeOrName(province.get().getCode());
        }
        return List.of();
    }

    // Utility methods
    @Transactional(readOnly = true)
    public String getUserFullName(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getFirstName() + " " + user.getLastName())
                .orElse("Unknown User");
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email, Long excludeUserId) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        return existingUser.isEmpty() || (excludeUserId != null && existingUser.get().getId().equals(excludeUserId));
    }

    // Bulk operations
    @Transactional(readOnly = true)
    public List<User> findAllByIds(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

    // Statistics and reporting
    @Transactional(readOnly = true)
    public long countUsersByLocation(Long locationId) {
        return userRepository.findByLocationId(locationId, Pageable.unpaged()).getTotalElements();
    }

    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepository.count();
    }

    // Soft delete operations
    public void softDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // The @SQLDelete annotation will handle the soft delete automatically
        userRepository.deleteById(id);
    }

    // User activation/deactivation (if you add an 'enabled' field)
    public void deactivateUser(Long userId) {
        // If you add an 'enabled' boolean field to User entity
        // User user = userRepository.findById(userId).orElseThrow(...);
        // user.setEnabled(false);
        // userRepository.save(user);
    }

    public void activateUser(Long userId) {
        // User user = userRepository.findById(userId).orElseThrow(...);
        // user.setEnabled(true);
        // userRepository.save(user);
    }
}