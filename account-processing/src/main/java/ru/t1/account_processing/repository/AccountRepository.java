package ru.t1.account_processing.repository;

import aj.org.objectweb.asm.commons.Remapper;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.account_processing.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
   Account findByClientIdAndProductId(Long clientId, Long productId);
}
