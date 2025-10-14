package ru.t1.account_processing.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.repository.AccountRepository;
import ru.t1.starter.aop.annotation.HttpIncomeRequestLog;

/**
 * REST-контроллер для работы с сущностью {@link Account}.
 * Предоставляет API для получения информации о счетах клиентов.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    /** Репозиторий для доступа к данным по счетам */
    private final AccountRepository accountRepository;

    /**
     * Получение идентификатора счёта по идентификатору клиента и продукта.
     *
     * Ищет в БД запись {@link Account}, соответствующую указанным
     * clientId и productId. Если счёт найден —
     * возвращает его идентификатор, иначе возвращает HTTP 404. (решил возвращать null)
     *
     * @param clientId  идентификатор клиента
     * @param productId идентификатор продукта
     * @return {@link ResponseEntity} с идентификатором счёта или 404 Not Found (решил возвращать null)
     */
    @GetMapping("/get/by-client/{clientId}/product/{productId}")
    @HttpIncomeRequestLog
    public ResponseEntity<Long> getAccountId(
            @PathVariable("clientId") Long clientId,
            @PathVariable("productId") Long productId
    ) {
        Account account = accountRepository.findByClientIdAndProductId(clientId, productId);
        if (account != null) {
            return ResponseEntity.ok(account.getId());
        } else {
            //return ResponseEntity.notFound().build();
            return ResponseEntity.ok(null); // возврат null вместо account, чтобы не было ошибки 404
        }
    }
}

