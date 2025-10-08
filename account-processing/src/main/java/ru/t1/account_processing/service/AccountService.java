package ru.t1.account_processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.entity.enums.AccountStatus;
import ru.t1.account_processing.repository.AccountRepository;

import java.math.BigDecimal;

/**
 * Сервис для бизнес-логики, связанной с банковскими счетами {@link Account}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    /** Репозиторий для работы с таблицей счетов. */
    private final AccountRepository accountRepository;

    /**
     * Создаёт новый банковский счёт для указанного клиента и продукта.
     *
     * @param clientId  идентификатор клиента
     * @param productId идентификатор продукта
     */
    public void createAccountForClientProduct(Long clientId, Long productId) {
        Account account = new Account();
        account.setClientId(clientId);
        account.setProductId(productId);
        account.setBalance(BigDecimal.ZERO);
        account.setInterestRate(BigDecimal.ZERO);
        account.setIsRecalc(false);
        account.setCardExist(false);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
        log.info("Создан новый Account для клиента {} и продукта {}",
                clientId, productId);
    }
}
