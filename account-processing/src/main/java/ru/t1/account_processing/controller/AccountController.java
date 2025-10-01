/*
package ru.t1.account_processing.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.repository.AccountRepository;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;

    @GetMapping("/get/by-client/{clientId}/product/{productId}")
    public ResponseEntity<Long> getAccountId(
            @PathVariable("clientId") Long clientId,
            @PathVariable("productId") Long productId
    ) {
        Account account = accountRepository.findByClientIdAndProductId(clientId, productId);
        if (account != null) {
            return ResponseEntity.ok(account.getId());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

*/
