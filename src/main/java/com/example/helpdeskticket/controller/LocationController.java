package com.example.helpdeskticket.controller;

import com.example.helpdeskticket.model.Location;
import com.example.helpdeskticket.service.LocationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // Basic CRUD operations
    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        List<Location> locations = locationService.findAll();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
        Optional<Location> location = locationService.findById(id);
        return location.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Location> createLocation(@RequestBody Location location) {
        try {
            Location savedLocation = locationService.save(location);
            return ResponseEntity.ok(savedLocation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable Long id, @RequestBody Location location) {
        try {
            Location updatedLocation = locationService.update(id, location);
            return ResponseEntity.ok(updatedLocation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        try {
            locationService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Custom business operations
    @GetMapping("/name/{name}")
    public ResponseEntity<Location> getLocationByName(@PathVariable String name) {
        Optional<Location> location = locationService.findByName(name);
        return location.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkLocationExists(
            @RequestParam String name,
            @RequestParam(required = false) Long parentId) {
        boolean exists = locationService.existsByNameAndParentId(name, parentId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<Location>> getProvinces() {
        List<Location> provinces = locationService.findProvinces();
        return ResponseEntity.ok(provinces);
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<Page<Location>> getChildrenByParentId(
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Location> children = locationService.findChildrenByParentId(parentId, pageable);
        return ResponseEntity.ok(children);
    }

    @GetMapping("/province/search/{codeOrName}")
    public ResponseEntity<Location> findProvinceByCodeOrName(@PathVariable String codeOrName) {
        Optional<Location> province = locationService.findProvinceByCodeOrName(codeOrName);
        return province.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{parentId}/direct-children")
    public ResponseEntity<List<Location>> getDirectChildren(@PathVariable Long parentId) {
        List<Location> children = locationService.findDirectChildren(parentId);
        return ResponseEntity.ok(children);
    }

    // Advanced hierarchy operations
    @GetMapping("/{locationId}/hierarchy")
    public ResponseEntity<List<Location>> getFullHierarchy(@PathVariable Long locationId) {
        List<Location> hierarchy = locationService.findFullHierarchy(locationId);
        return ResponseEntity.ok(hierarchy);
    }

    @GetMapping("/{childLocationId}/province")
    public ResponseEntity<Location> getProvinceByChildLocation(@PathVariable Long childLocationId) {
        Optional<Location> province = locationService.findProvinceByChildLocation(childLocationId);
        return province.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{parentId}/descendants")
    public ResponseEntity<List<Location>> getAllDescendants(@PathVariable Long parentId) {
        List<Location> descendants = locationService.findAllDescendants(parentId);
        return ResponseEntity.ok(descendants);
    }

    // Helper methods
    @GetMapping("/{id}/is-province")
    public ResponseEntity<Boolean> isProvince(@PathVariable Long id) {
        boolean isProvince = locationService.isProvince(id);
        return ResponseEntity.ok(isProvince);
    }

    @GetMapping("/{id}/has-children")
    public ResponseEntity<Boolean> hasChildren(@PathVariable Long id) {
        boolean hasChildren = locationService.hasChildren(id);
        return ResponseEntity.ok(hasChildren);
    }

    // Bulk operations
    @GetMapping("/bulk")
    public ResponseEntity<List<Location>> getLocationsByIds(@RequestParam List<Long> ids) {
        List<Location> locations = locationService.findAllByIds(ids);
        return ResponseEntity.ok(locations);
    }

    // Search operations
    @GetMapping("/search")
    public ResponseEntity<List<Location>> searchByName(@RequestParam String name) {
        List<Location> locations = locationService.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok(locations);
    }

    // Validation endpoint
    @PostMapping("/validate-hierarchy")
    public ResponseEntity<Boolean> validateLocationHierarchy(@RequestBody Location location) {
        boolean isValid = locationService.isValidLocationHierarchy(location);
        return ResponseEntity.ok(isValid);
    }
}