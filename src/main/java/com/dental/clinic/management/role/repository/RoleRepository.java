package com.dental.clinic.management.role.repository;

import com.dental.clinic.management.role.domain.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data JPA repository for the {@link Role} entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findOneByRoleName(String roleName);

    Boolean existsByRoleName(String roleName);

    @EntityGraph(attributePaths = { "permissions", "baseRole" })
    @Query("SELECT r FROM Role r WHERE r.isActive = true")
    List<Role> findAllActiveRoles();

    @EntityGraph(attributePaths = { "permissions", "baseRole" })
    @Query("SELECT r FROM Role r WHERE r.roleId = :roleId")
    Optional<Role> findByIdWithPermissions(@Param("roleId") String roleId);

    @EntityGraph(attributePaths = { "permissions", "baseRole" })
    @Query("SELECT r FROM Role r WHERE r.roleName IN :roleNames AND r.isActive = true")
    Set<Role> findOneByRoleNamesWithPermissions(@Param("roleNames") Set<String> roleNames);
}
