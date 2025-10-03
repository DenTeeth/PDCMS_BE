package com.dental.clinic.management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.dental.clinic.management.domain.Employee;

/**
 * Spring Data JPA repository for the {@link Employee} entity.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String>, JpaSpecificationExecutor<Employee> {

  Optional<Employee> findOneByEmployeeCode(String employeeCode);

  Optional<Employee> findOneByAccountAccountId(String accountId);

  Optional<Employee> findOneByAccountUsername(String username);

  Optional<Employee> findOneByAccountEmail(String email);

  List<Employee> findByIsActive(Boolean isActive);

  Boolean existsByEmployeeCode(String employeeCode);
}
