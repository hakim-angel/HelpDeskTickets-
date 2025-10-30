package com.example.helpdeskticket.repository;

import com.example.helpdeskticket.model.Location;
import com.example.helpdeskticket.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Derived: Find by email (unique)
    Optional<User> findByEmail(String email);

    // Exists by email (for registration check)
    boolean existsByEmail(String email);

    // Find by first name (partial match), sorted by createdAt desc
    List<User> findByFirstNameContainingIgnoreCaseOrderByCreatedAtDesc(String firstName);

    // Paginated users by location (e.g., all in a village)
    Page<User> findByLocationId(Long locationId, Pageable pageable);

    // Custom: Users by province code/name (bidirectional: join hierarchy)
    @Query("SELECT DISTINCT u FROM User u JOIN u.location loc " +
           "WHERE loc.id IN (SELECT child.id FROM Location child WHERE child.parent IN " +
           "(SELECT prov FROM Location prov WHERE prov.code = :codeOrName OR prov.name = :codeOrName))")
    List<User> findByProvinceCodeOrName(@Param("codeOrName") String codeOrName);

    // FIXED: Get province from user's village (traversal)
    @Query("SELECT l FROM Location l WHERE l.id = (" +
           "SELECT loc.parent.parent.parent.id FROM User u JOIN u.location loc WHERE u.id = :userId" +
           ")")
    Optional<Location> findProvinceByUserId(@Param("userId") Long userId);

    // Alternative: Simplified version for direct location hierarchy
    @Query("SELECT loc FROM Location loc WHERE loc.level = 1 AND loc.id = (" +
           "SELECT l.parent.parent.parent.id FROM Location l WHERE l.id = " +
           "(SELECT u.location.id FROM User u WHERE u.id = :userId)" +
           ")")
    Optional<Location> findProvinceByUserLocation(@Param("userId") Long userId);

    // Paginated all users, with sort example in service: findAll(Sort.by("createdAt").descending())
    Page<User> findAll(Pageable pageable);
}