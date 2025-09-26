package com.dental.clinic.management.repository;

import com.dental.clinic.management.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Spring Data JPA repository for the {@link Permission} entity.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    Optional<Permission> findOneByPermissionName(String permissionName);

    Boolean existsByPermissionName(String permissionName);

    List<Permission> findByModule(String module);

    /**
     * Return all active permissions.
     */
    @Query("SELECT p FROM Permission p WHERE p.isActive = true")
    List<Permission> findAllActivePermissions();

    List<Permission> findByModuleAndIsActive(String module, Boolean isActive);
}
