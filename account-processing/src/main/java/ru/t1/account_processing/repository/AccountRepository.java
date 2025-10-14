package ru.t1.account_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.account_processing.entity.Account;
import ru.t1.starter.aop.annotation.Cached;

public interface AccountRepository extends JpaRepository<Account, Long> {
   @Cached(cacheName = "Account")
   Account findByClientIdAndProductId(Long clientId, Long productId);
}
