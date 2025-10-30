package com.example.helpdeskticket.service;

import com.example.helpdeskticket.model.Location;
import com.example.helpdeskticket.repository.LocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    // Basic CRUD operations
    @Transactional(readOnly = true)
    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    public Location save(Location location) {
        // Validate hierarchy before saving
        if (!isValidLocationHierarchy(location)) {
            throw new IllegalArgumentException("Invalid location hierarchy");
        }
        
        // Check for duplicate names under same parent
        Long parentId = location.getParent() != null ? location.getParent().getId() : null;
        if (locationRepository.existsByNameAndParentId(location.getName(), parentId)) {
            throw new IllegalArgumentException("Location with name '" + location.getName() + 
                    "' already exists under this parent");
        }
        
        return locationRepository.save(location);
    }

    public Location update(Long id, Location location) {
        // Check if location exists
        Location existingLocation = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));
        
        // Validate hierarchy
        if (!isValidLocationHierarchy(location)) {
            throw new IllegalArgumentException("Invalid location hierarchy");
        }
        
        // Check for duplicate names under same parent (excluding current location)
        Long parentId = location.getParent() != null ? location.getParent().getId() : null;
        Optional<Location> duplicate = locationRepository.findByName(location.getName());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id) && 
            ((duplicate.get().getParent() == null && parentId == null) || 
             (duplicate.get().getParent() != null && duplicate.get().getParent().getId().equals(parentId)))) {
            throw new IllegalArgumentException("Location with name '" + location.getName() + 
                    "' already exists under this parent");
        }
        
        // Update fields
        existingLocation.setName(location.getName());
        existingLocation.setCode(location.getCode());
        existingLocation.setParent(location.getParent());
        existingLocation.setLevel(location.getLevel());
        
        return locationRepository.save(existingLocation);
    }

    public void deleteById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));
        
        // Check if location has children
        List<Location> children = locationRepository.findDirectChildren(id);
        if (!children.isEmpty()) {
            throw new IllegalStateException("Cannot delete location that has child locations");
        }
        
        locationRepository.deleteById(id);
    }

    // Custom business logic operations
    @Transactional(readOnly = true)
    public Optional<Location> findByName(String name) {
        return locationRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public boolean existsByNameAndParentId(String name, Long parentId) {
        return locationRepository.existsByNameAndParentId(name, parentId);
    }

    @Transactional(readOnly = true)
    public List<Location> findProvinces() {
        return locationRepository.findByParentIsNullOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Page<Location> findChildrenByParentId(Long parentId, Pageable pageable) {
        return locationRepository.findByParentId(parentId, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Location> findProvinceByCodeOrName(String codeOrName) {
        return locationRepository.findProvinceByCodeOrName(codeOrName);
    }

    @Transactional(readOnly = true)
    public List<Location> findDirectChildren(Long parentId) {
        return locationRepository.findDirectChildren(parentId);
    }

    @Transactional(readOnly = true)
    public boolean isValidLocationHierarchy(Location location) {
        // Provinces (level 1) should not have a parent
        if (location.getLevel() == 1 && location.getParent() != null) {
            return false;
        }
        
        // Non-province locations must have a parent
        if (location.getLevel() > 1 && location.getParent() == null) {
            return false;
        }
        
        // Level should be consistent with parent's level
        if (location.getParent() != null && location.getParent().getId() != null) {
            // Get the actual parent level from database
            Integer parentLevel = locationRepository.findById(location.getParent().getId())
                    .map(Location::getLevel)
                    .orElseThrow(() -> new IllegalArgumentException("Parent location not found with id: " + location.getParent().getId()));
            
            if (location.getLevel() != parentLevel + 1) {
                return false;
            }
        }
        
        return true;
    }

    // Advanced hierarchy operations
    @Transactional(readOnly = true)
    public List<Location> findFullHierarchy(Long locationId) {
        // This would typically use a recursive query to get all descendants
        // For now, returning direct children. You can implement full recursion as needed.
        return findDirectChildren(locationId);
    }

    @Transactional(readOnly = true)
    public Optional<Location> findProvinceByChildLocation(Long childLocationId) {
        // This would use the native recursive query commented in the repository
        // For now, we'll traverse up the hierarchy
        return locationRepository.findById(childLocationId)
                .map(location -> {
                    Location current = location;
                    while (current.getParent() != null) {
                        current = current.getParent();
                    }
                    return current;
                });
    }

    @Transactional(readOnly = true)
    public List<Location> findAllDescendants(Long parentId) {
        // This method would benefit from the native recursive CTE query
        // For now, we'll get direct children. You can implement full recursion later.
        return findDirectChildren(parentId);
    }

    // Additional helper methods
    @Transactional(readOnly = true)
    public boolean isProvince(Long locationId) {
        return locationRepository.findById(locationId)
                .map(location -> location.getLevel() == 1)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean hasChildren(Long locationId) {
        return !findDirectChildren(locationId).isEmpty();
    }

    // Bulk operations
    @Transactional(readOnly = true)
    public List<Location> findAllByIds(List<Long> ids) {
        return locationRepository.findAllById(ids);
    }

    // Search operations
    @Transactional(readOnly = true)
    public List<Location> findByNameContainingIgnoreCase(String name) {
        // You would need to add this method to your repository
        // For now, this is a placeholder
        return locationRepository.findAll().stream()
                .filter(location -> location.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }
}