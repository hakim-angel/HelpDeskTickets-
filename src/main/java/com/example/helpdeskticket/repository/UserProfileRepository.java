package com.example.helpdeskticket.repository;



import com.example.helpdeskticket.model.User;
import com.example.helpdeskticket.model.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    // Derived: Find by user (1:1)
    Optional<UserProfile> findByUser(User user);

    // Exists by user (ensure one-to-one)
    boolean existsByUser(User user);

    // Paginated (rare for profiles, but for completeness)
    Page<UserProfile> findAll(Pageable pageable);

    // Custom: Find by phone (partial)
    /*
    @Query("SELECT up FROM UserProfile up WHERE up.phone LIKE %:phone%")
    List<UserProfile> findByPhoneContaining(@Param("phone") String phone);
    */
}