package com.dental.clinic.management.mapper;

import com.dental.clinic.management.domain.Role;
import com.dental.clinic.management.dto.response.RoleInfoResponse;
import com.dental.clinic.management.dto.request.CreateRoleRequest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public RoleInfoResponse toRoleInfoResponse(Role role) {
        if (role == null)
            return null;
        RoleInfoResponse r = new RoleInfoResponse();
        r.setRoleId(role.getRoleId());
        r.setRoleName(role.getRoleName());
        r.setDescription(role.getDescription());
        r.setRequiresSpecialization(role.getRequiresSpecialization());
        r.setIsActive(role.getIsActive());
        r.setCreatedAt(role.getCreatedAt());
        return r;
    }

    public List<RoleInfoResponse> toRoleInfoResponseList(List<Role> roles) {
        return roles.stream().map(this::toRoleInfoResponse).collect(Collectors.toList());
    }

    public Role toRole(CreateRoleRequest request) {
        if (request == null)
            return null;
        Role role = new Role();
        role.setRoleId(request.getRoleId());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setRequiresSpecialization(request.getRequiresSpecialization() != null
                ? request.getRequiresSpecialization()
                : false);
        role.setIsActive(true);
        return role;
    }
}
