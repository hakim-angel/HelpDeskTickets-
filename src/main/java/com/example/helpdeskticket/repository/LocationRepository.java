package com.example.helpdeskticket.repository;



import com.example.helpdeskticket.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    // Derived: Find by name (exact)
    Optional<Location> findByName(String name);

    // Exists check (e.g., prevent duplicate names under same parent)
    boolean existsByNameAndParentId(String name, Long parentId);

    // Find provinces (top-level: parent null), sorted by name
    List<Location> findByParentIsNullOrderByNameAsc();

    // Paginated children of a location (e.g., districts under province)
    Page<Location> findByParentId(Long parentId, Pageable pageable);

    // Custom: Find province by code or name (for bidirectional user filter)
    @Query("SELECT l FROM Location l WHERE l.code = :codeOrName OR l.name = :codeOrName AND l.parent IS NULL")
    Optional<Location> findProvinceByCodeOrName(@Param("codeOrName") String codeOrName);

    // Custom: Get all descendants (simplified; for full recursion, use native CTE below)
    @Query("SELECT l2 FROM Location l1 JOIN l1.children l2 WHERE l1.id = :parentId")
    List<Location> findDirectChildren(@Param("parentId") Long parentId);

    // Example native recursive query for full hierarchy (PostgreSQL CTE for province from village)
    /*
    @Query(value = "WITH RECURSIVE location_tree AS (" +
                   "  SELECT id, parent_id, name, code, level FROM locations WHERE id = :villageId " +
                   "  UNION ALL " +
                   "  SELECT l.id, l.parent_id, l.name, l.code, l.level FROM locations l " +
                   "  INNER JOIN location_tree lt ON l.id = lt.parent_id " +
                   ") SELECT * FROM location_tree WHERE level = 1", nativeQuery = true)
    Optional<Location> findProvinceFromVillageNative(@Param("villageId") Long villageId);
    */
}