package com.dental.clinic.management.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dental.clinic.management.domain.Account;
import com.dental.clinic.management.domain.Employee;
import com.dental.clinic.management.domain.Permission;
import com.dental.clinic.management.domain.Role;
import com.dental.clinic.management.domain.Specialization;
import com.dental.clinic.management.domain.enums.AccountStatus;
import com.dental.clinic.management.repository.AccountRepository;
import com.dental.clinic.management.repository.EmployeeRepository;
import com.dental.clinic.management.repository.PermissionRepository;
import com.dental.clinic.management.repository.RoleRepository;
import com.dental.clinic.management.repository.SpecializationRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for system setup and initialization.
 * Used for creating initial admin accounts and test data.
 */
@RestController
@RequestMapping("/api/v1/setup")
@Tag(name = "Setup", description = "System initialization and setup operations")
public class SetupController {

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final SpecializationRepository specializationRepository;
    private final PasswordEncoder passwordEncoder;

    public SetupController(
            AccountRepository accountRepository,
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            SpecializationRepository specializationRepository,
            PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.specializationRepository = specializationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * {@code POST /setup/create-admin} : Create initial admin account with default
     * permissions.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and
     *         confirmation message.
     */
    @Operation(summary = "Create admin account", description = "Initialize the system by creating default admin account with all permissions. Use for first-time setup only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin account created successfully or already exists", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error during setup", content = @Content)
    })
    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdmin() {
        // Kiểm tra nếu admin đã tồn tại
        if (accountRepository.existsByUsername("admin")) {
            return ResponseEntity.ok("Admin already exists!");
        }

        // 1. Tạo Permissions
        Permission userRead = createPermission("PERM_USER_READ", "USER_READ", "Đọc thông tin người dùng", "USER");
        Permission adminAccess = createPermission("PERM_ADMIN_ACCESS", "ADMIN_ACCESS", "Truy cập quản trị", "ADMIN");

        // 2. Tạo Roles
        Role adminRole = createRole("ROLE_ADMIN", "ADMIN", "Quản trị viên", Set.of(userRead, adminAccess));

        // 3. Tạo Specialization
        Specialization generalSpec = createSpecialization("SPEC_GENERAL", "GENERAL", "General Dentistry",
                "Nha khoa tổng quát");

        // 4. Tạo Admin Account
        Account adminAccount = createAccount(
                "ACC_ADMIN",
                "admin@dental.com",
                "admin",
                "123456",
                Set.of(adminRole));

        // 5. Tạo Admin Employee Profile
        createEmployee(
                "EMP_ADM001",
                adminAccount,
                "ADM001",
                "Admin",
                "System",
                "0123456789",
                LocalDate.of(1990, 1, 1),
                "HCM, VN",
                Set.of(generalSpec));

        return ResponseEntity.ok("""
                Admin created successfully!
                 Email: admin@dental.com
                 Username: admin
                 Password: 123456
                """);
    }

    private Permission createPermission(String id, String name, String description, String module) {
        Permission permission = new Permission();
        permission.setPermissionId(id);
        permission.setPermissionName(name);
        permission.setDescription(description);
        permission.setModule(module);
        permission.setIsActive(true);
        return permissionRepository.save(permission);
    }

    private Role createRole(String id, String name, String description, Set<Permission> permissions) {
        Role role = new Role();
        role.setRoleId(id);
        role.setRoleName(name);
        role.setDescription(description);
        role.setPermissions(permissions);
        role.setIsActive(true);
        return roleRepository.save(role);
    }

    private Specialization createSpecialization(String id, String code, String name, String description) {
        Specialization spec = new Specialization();
        spec.setSpecializationId(id);
        spec.setSpecializationCode(code);
        spec.setSpecializationName(name);
        spec.setDescription(description);
        spec.setIsActive(true);
        return specializationRepository.save(spec);
    }

    private Account createAccount(String id, String email, String username, String password, Set<Role> roles) {
        Account account = new Account();
        account.setAccountId(id);
        account.setEmail(email);
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setRoles(roles);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    private Employee createEmployee(String id, Account account, String employeeCode, String firstName, String lastName,
            String phone, LocalDate dateOfBirth, String address, Set<Specialization> specializations) {
        Employee employee = new Employee();
        employee.setEmployeeId(id);
        employee.setAccount(account);
        employee.setEmployeeCode(employeeCode);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setPhone(phone);
        employee.setDateOfBirth(dateOfBirth);
        employee.setAddress(address);
        employee.setSpecializations(specializations);
        employee.setIsActive(true);
        employee.setCreatedAt(LocalDateTime.now());
        // Set default role as ADMIN
        employee.setRoleId("ADMIN");
        return employeeRepository.save(employee);
    }
}
