package com.dental.clinic.management.repository;

import com.dental.clinic.management.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findOneByEmployeeCode(String employeeCode);

    Optional<User> findOneByAccountAccountId(String accountId);

    Optional<User> findOneByAccountUsername(String username);

    Optional<User> findOneByAccountEmail(String email);

    Boolean existsByEmployeeCode(String employeeCode);
}
