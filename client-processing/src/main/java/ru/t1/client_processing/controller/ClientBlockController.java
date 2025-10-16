package ru.t1.client_processing.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.t1.client_processing.service.ClientBlockService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/clients")
@RequiredArgsConstructor
public class ClientBlockController {

    private final ClientBlockService clientBlockService;

    @PostMapping("/{clientId}/block")
    @PreAuthorize("hasRole('MASTER')") // Только MASTER может блокировать
    public ResponseEntity<?> blockClient(
            @PathVariable("clientId") Long clientId,
            @RequestBody BlockRequest request) {

        clientBlockService.blockClient(clientId, request.getReason(), request.getExpirationDate());
        return ResponseEntity.ok("Client blocked successfully");
    }

    @PostMapping("/{clientId}/unblock")
    @PreAuthorize("hasRole('MASTER')") // Только MASTER может разблокировать
    public ResponseEntity<?> unblockClient(@PathVariable Long clientId) {
        clientBlockService.unblockClient(clientId);
        return ResponseEntity.ok("Client unblocked successfully");
    }

    @Data
    public static class BlockRequest {
        private String reason;
        private LocalDateTime expirationDate; // null для бессрочной блокировки
    }
}