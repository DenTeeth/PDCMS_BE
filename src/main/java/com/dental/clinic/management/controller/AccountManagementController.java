package com.dental.clinic.management.controller;

import com.dental.clinic.management.service.AccountService;
import com.dental.clinic.management.utils.annotation.ApiMessage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountManagementController {

    private final AccountService accountService;

    public AccountManagementController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/{accountId}/roles")
    @ApiMessage("Assign multiple roles to user successfully")
    public ResponseEntity<Void> assignRolesToAccount(
            @PathVariable String accountId,
            @RequestBody List<String> roleIds) {
        accountService.assignRolesToAccount(accountId, roleIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{accountId}/roles")
    @ApiMessage("Get roles of user successfully")
    public ResponseEntity<List<String>> getAccountRoles(@PathVariable String accountId) {
        List<String> roles = accountService.getAccountRoles(accountId);
        return ResponseEntity.ok().body(roles);
    }
}