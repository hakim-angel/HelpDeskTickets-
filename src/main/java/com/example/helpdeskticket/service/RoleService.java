package com.example.helpdeskticket.service;

import com.example.helpdeskticket.model.Role;
import com.example.helpdeskticket.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // Basic CRUD operations
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Role> findAllOrderByNameAsc() {
        return roleRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    public Role createRole(Role role) {
        // Validate role name
        if (role.getName() == null || role.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }

        // Check if role name already exists
        if (roleRepository.existsByName(role.getName())) {
            throw new IllegalArgumentException("Role with name '" + role.getName() + "' already exists");
        }

        // Ensure name is in uppercase for consistency
        role.setName(role.getName().toUpperCase());

        return roleRepository.save(role);
    }

    public Role updateRole(Long id, Role roleDetails) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));

        // Validate role name
        if (roleDetails.getName() == null || roleDetails.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }

        // Check if role name is being changed and if it already exists
        if (!existingRole.getName().equals(roleDetails.getName()) && 
            roleRepository.existsByName(roleDetails.getName())) {
            throw new IllegalArgumentException("Role with name '" + roleDetails.getName() + "' already exists");
        }

        // Update fields
        existingRole.setName(roleDetails.getName().toUpperCase());

        return roleRepository.save(existingRole);
    }

    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
        
        // Check if role has users assigned
        if (!role.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete role that has users assigned. Remove users from role first.");
        }
        
        roleRepository.deleteById(id);
    }

    // Role-specific operations
    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name.toUpperCase());
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name.toUpperCase());
    }

    // Role assignment management
    @Transactional(readOnly = true)
    public int getUserCountForRole(Long roleId) {
        return roleRepository.findById(roleId)
                .map(role -> role.getUsers().size())
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public boolean isRoleAssignedToUsers(Long roleId) {
        return getUserCountForRole(roleId) > 0;
    }

    // Bulk operations
    public List<Role> createRoles(List<Role> roles) {
        // Validate all roles first
        for (Role role : roles) {
            if (role.getName() == null || role.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Role name cannot be null or empty");
            }
            if (roleRepository.existsByName(role.getName())) {
                throw new IllegalArgumentException("Role with name '" + role.getName() + "' already exists");
            }
            role.setName(role.getName().toUpperCase());
        }
        
        return roleRepository.saveAll(roles);
    }

    // Default roles initialization
    public void initializeDefaultRoles() {
        List<String> defaultRoleNames = List.of("CUSTOMER", "AGENT", "ADMIN", "SUPERVISOR");
        
        for (String roleName : defaultRoleNames) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
    }

    // Search and validation
    @Transactional(readOnly = true)
    public boolean isValidRoleName(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return false;
        }
        
        // Add any additional validation rules for role names
        return roleName.matches("^[A-Z_]+$"); // Only uppercase letters and underscores
    }

    @Transactional(readOnly = true)
    public List<Role> findRolesByNames(List<String> roleNames) {
        return roleNames.stream()
                .map(name -> roleRepository.findByName(name.toUpperCase()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    // Role hierarchy and permissions (placeholder for future expansion)
    @Transactional(readOnly = true)
    public boolean hasPermission(String roleName, String permission) {
        // This is a placeholder for permission checking logic
        // You can expand this based on your permission system
        return roleRepository.findByName(roleName.toUpperCase())
                .isPresent(); // Simple check - expand as needed
    }

    // Statistics and reporting
    @Transactional(readOnly = true)
    public long getTotalRoleCount() {
        return roleRepository.count();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getRoleUsageStatistics() {
        return roleRepository.findRoleNamesWithUserCounts();
    }

    // Soft delete compatibility
    public void softDeleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
        
        // Check if role has users before soft deletion
        if (!role.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete role that has users assigned");
        }
        
        roleRepository.deleteById(id);
    }

    // Role validation for user assignment
    public boolean canAssignRoleToUser(Long roleId) {
        return roleRepository.findById(roleId)
                .map(role -> !role.getIsDeleted())
                .orElse(false);
    }

    // Get all active roles (non-deleted)
    @Transactional(readOnly = true)
    public List<Role> findAllActiveRoles() {
        // The @Where clause already filters deleted roles, so findAll() returns only active ones
        return roleRepository.findAll();
    }
}