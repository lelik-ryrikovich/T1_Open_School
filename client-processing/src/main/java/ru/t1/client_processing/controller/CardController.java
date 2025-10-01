package ru.t1.client_processing.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.client_processing.service.ClientCardService;
import ru.t1.dto.KafkaMessageClientCard;

/**
 * REST-контроллер для работы с банковскими картами клиента.
 * Отвечает за создание карт и отправку запросов в Kafka.
 */
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    private final ClientCardService clientCardService;

    /**
     * Создание новой карты для клиента.
     *
     * @param request данные о карте
     * @return сообщение об успешной отправке запроса
     */
    @PostMapping
    public ResponseEntity<String> createCard(@RequestBody KafkaMessageClientCard request) {
        clientCardService.sendCardCreateRequest(request);
        return ResponseEntity.ok("Запрос на создание карты отправлен");
    }
}
