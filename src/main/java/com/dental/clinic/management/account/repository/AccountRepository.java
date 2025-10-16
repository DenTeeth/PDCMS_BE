package com.dental.clinic.management.account.repository;

import com.dental.clinic.management.account.enums.AccountStatus;
import com.dental.clinic.management.account.domain.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Account} entity.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findOneByEmail(String email);

    Optional<Account> findOneByUsername(String username);

    Boolean existsByEmail(String email);

    Boolean existsByUsername(String username);

    /**
     * Fetch account by email eagerly with roles and permissions.
     */
    @Query("SELECT a FROM Account a JOIN FETCH a.roles r JOIN FETCH r.permissions WHERE a.email = :email")
    Optional<Account> findByEmailWithRolesAndPermissions(@Param("email") String email);

    /**
     * Fetch account by username eagerly with roles and permissions.
     */
    @Query("SELECT a FROM Account a JOIN FETCH a.roles r JOIN FETCH r.permissions WHERE a.username = :username")
    Optional<Account> findByUsernameWithRolesAndPermissions(@Param("username") String username);

    /**
     * Fetch account by id eagerly with roles and permissions.
     */
    @Query("SELECT a FROM Account a JOIN FETCH a.roles r JOIN FETCH r.permissions WHERE a.accountId = :accountId")
    Optional<Account> findByAccountIdWithRolesAndPermissions(@Param("accountId") String accountId);

    /**
     * Find all active accounts.
     */
    List<Account> findByStatus(AccountStatus status);
}
