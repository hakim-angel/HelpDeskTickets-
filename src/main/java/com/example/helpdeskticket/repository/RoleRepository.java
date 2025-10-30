package com.example.helpdeskticket.repository;

import com.example.helpdeskticket.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Derived: Find by name (unique)
    Optional<Role> findByName(String name);

    // Exists by name
    boolean existsByName(String name);

    // FIXED: All roles, sorted by name
    List<Role> findAllByOrderByNameAsc();

    // Custom: Roles with user count (using @Query for aggregation)
    @Query("SELECT r.name, COUNT(u) FROM Role r LEFT JOIN r.users u GROUP BY r.name")
    List<Object[]> findRoleNamesWithUserCounts();
}