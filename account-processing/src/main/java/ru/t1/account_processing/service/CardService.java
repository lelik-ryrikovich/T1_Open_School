package ru.t1.account_processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.entity.Card;
import ru.t1.account_processing.entity.enums.AccountStatus;
import ru.t1.account_processing.entity.enums.CardStatus;
import ru.t1.account_processing.entity.enums.PaymentSystem;
import ru.t1.account_processing.exception.AccountIsArrestedException;
import ru.t1.account_processing.exception.AccountNotExistForClientException;
import ru.t1.account_processing.exception.AccountNotFoundException;
import ru.t1.account_processing.repository.AccountRepository;
import ru.t1.account_processing.repository.CardRepository;
import ru.t1.starter.aop.annotation.LogDatasourceError;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;

    /**
     * Создаёт новую банковскую карту, привязанную к существующему счёту клиента.
     * <p>Выполняет:
     * <ul>
     *     <li>Проверку существования счёта;</li>
     *     <li>Проверку принадлежности счёта клиенту;</li>
     *     <li>Проверку, что счёт не арестован;</li>
     *     <li>Создание активной карты указанной платёжной системы.</li>
     * </ul>
     *
     * @param accountId      идентификатор счёта
     * @param clientId       идентификатор клиента
     * @param paymentSystem  платёжная система
     */
    @LogDatasourceError
    public void createCard(Long accountId, Long clientId, String paymentSystem) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Счёт не найден: " + accountId));

        if (account.getStatus() == AccountStatus.ARRESTED) {
            throw new AccountIsArrestedException("Счет " + accountId + " заблокирован");
        }

        if (!Objects.equals(clientId, account.getClientId())) {
            throw new AccountNotExistForClientException(
                    "Счёт " + accountId + " не принадлежит клиенту: " + clientId);
        }

        Card card = new Card();
        String cardId = String.format("%016d",
                ThreadLocalRandom.current().nextLong(1_0000_0000_0000_000L, 9_9999_9999_9999_999L));
        card.setCardId(cardId);
        card.setAccountId(accountId);
        card.setPaymentSystem(PaymentSystem.valueOf(paymentSystem.toUpperCase()));
        card.setStatus(CardStatus.ACTIVE);

        cardRepository.save(card);
        log.info("Создана карта {} для клиента {} по счёту {}", card.getId(), clientId, accountId);
    }
}
