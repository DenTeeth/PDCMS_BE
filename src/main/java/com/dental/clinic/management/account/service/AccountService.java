package com.dental.clinic.management.account.service;

import com.dental.clinic.management.exception.BadRequestAlertException;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

import com.dental.clinic.management.account.domain.Account;
import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.role.domain.Role;
import com.dental.clinic.management.role.repository.RoleRepository;

import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;

    public AccountService(
            AccountRepository accountRepository,
            RoleRepository roleRepository) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional
    public void assignRolesToAccount(String accountId, List<String> roleIds) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Account not found with ID: " + accountId,
                        "account",
                        "accountnotfound"));

        Set<Role> roles = new HashSet<>();
        for (String roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new BadRequestAlertException(
                            "Role not found with ID: " + roleId,
                            "role",
                            "rolenotfound"));
            roles.add(role);
        }

        account.setRoles(roles);
        accountRepository.save(account);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional(readOnly = true)
    public List<String> getAccountRoles(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Account not found with ID: " + accountId,
                        "account",
                        "accountnotfound"));

        return account.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }
}
