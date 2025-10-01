package ru.t1.account_processing.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
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
import ru.t1.dto.KafkaMessageClientCard;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Kafka-консьюмер для обработки сообщений из топика {@code client_cards}.
 * Отвечает за создание банковских карт, привязанных к существующим счетам клиента.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerClientCardService {
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;

    /**
     * Обработка сообщений из Kafka-топика {@code client_cards}.
     *
     * @param message сообщение о банковской карте клиента
     */
    @KafkaListener(topics = "client_cards", groupId = "account-processing-group")
    public void consume(KafkaMessageClientCard message) {
        log.info("Получено сообщение из топика client_cards: {}", message);
        try {
            if ("CREATE".equalsIgnoreCase(message.getOperation())) {

                // Проверка существования счета
                Account account = accountRepository.findById(message.getAccountId())
                        .orElseThrow(() -> new AccountNotFoundException("Счёт не найден: " + message.getAccountId()));

                // Проверка, что счёт не заблокирован
                if (account.getStatus() == AccountStatus.ARRESTED) {
                    throw new AccountIsArrestedException("Счет " + message.getAccountId() + " заблокирован");
                }

                // Проверка принадлежности счета клиенту
                if (!Objects.equals(message.getClientId(), account.getClientId())) {
                    throw new AccountNotExistForClientException(
                            "Счёт " + message.getAccountId() + " не принадлежит клиенту: " + message.getClientId());
                }

                // Создание карты
                Card card = new Card();
                String cardId = String.format("%016d",
                        ThreadLocalRandom.current().nextLong(1_0000_0000_0000_000L, 9_9999_9999_9999_999L));
                card.setCardId(cardId);
                card.setAccountId(account.getId());
                card.setPaymentSystem(PaymentSystem.valueOf(message.getPaymentSystem().toUpperCase()));
                card.setStatus(CardStatus.ACTIVE);

                cardRepository.save(card);

                log.info("Создана карта {} для клиента {} по счёту {}",
                        card.getId(), message.getClientId(), message.getAccountId());
            } else {
                log.info("Операция {} пока не поддерживается", message.getOperation());
            }
        } catch (AccountNotFoundException | AccountNotExistForClientException | AccountIsArrestedException ex) {
            log.error("Ошибка бизнес-логики при обработке сообщения {}: {}", message, ex.getMessage());
        } catch (Exception ex) {
            log.error("Неожиданная ошибка при обработке сообщения {}: {}", message, ex.getMessage(), ex);
        }
    }
}
