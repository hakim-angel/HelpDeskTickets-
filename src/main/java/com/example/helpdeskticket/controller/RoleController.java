package com.example.helpdeskticket.controller;

import com.example.helpdeskticket.model.Role;
import com.example.helpdeskticket.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.findAllOrderByNameAsc();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        Optional<Role> role = roleService.findById(id);
        return role.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        try {
            Role savedRole = roleService.createRole(role);
            return ResponseEntity.ok(savedRole);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        try {
            Role updatedRole = roleService.updateRole(id, role);
            return ResponseEntity.ok(updatedRole);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Role> getRoleByName(@PathVariable String name) {
        Optional<Role> role = roleService.findByName(name);
        return role.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/exists/{name}")
    public ResponseEntity<Boolean> checkRoleNameExists(@PathVariable String name) {
        boolean exists = roleService.existsByName(name);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/initialize-defaults")
    public ResponseEntity<Void> initializeDefaultRoles() {
        roleService.initializeDefaultRoles();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/user-count")
    public ResponseEntity<Integer> getUserCountForRole(@PathVariable Long id) {
        int userCount = roleService.getUserCountForRole(id);
        return ResponseEntity.ok(userCount);
    }

    @GetMapping("/{id}/has-users")
    public ResponseEntity<Boolean> checkRoleHasUsers(@PathVariable Long id) {
        boolean hasUsers = roleService.isRoleAssignedToUsers(id);
        return ResponseEntity.ok(hasUsers);
    }

    @GetMapping("/statistics/count")
    public ResponseEntity<Long> getTotalRoleCount() {
        long count = roleService.getTotalRoleCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/usage")
    public ResponseEntity<List<Object[]>> getRoleUsageStatistics() {
        List<Object[]> statistics = roleService.getRoleUsageStatistics();
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Role>> createRoles(@RequestBody List<Role> roles) {
        try {
            List<Role> savedRoles = roleService.createRoles(roles);
            return ResponseEntity.ok(savedRoles);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/validate-name/{name}")
    public ResponseEntity<Boolean> validateRoleName(@PathVariable String name) {
        boolean isValid = roleService.isValidRoleName(name);
        return ResponseEntity.ok(isValid);
    }
}