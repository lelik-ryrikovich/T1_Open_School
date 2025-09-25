package ru.t1.client_processing.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.t1.client_processing.repository.ClientRepository;

@Component
@RequiredArgsConstructor
public class ClientIdGenerator {
    private final ClientRepository clientRepository;

    /**
     * Генерирует clientId в формате XXFFNNNNNNNN
     * @param prefix код региона (XX) + код филиала (FF) = documentPrefix
     * @return
     */
    public String generateClientId(String prefix) {

        // Находим максимальный существующий clientId с таким префиксом
        String maxClientId = clientRepository.findMaxClientIdByPrefix(prefix);

        long nextSequence;
        if (maxClientId != null && maxClientId.startsWith(prefix)) {
            // Извлекаем последовательную часть из существующего ID
            String sequentialPart = maxClientId.substring(prefix.length()); // после префикса
            nextSequence = Long.parseLong(sequentialPart) + 1;
        } else {
            // Если нет существующих ID с таким префиксом, начинаем с 1
            nextSequence = 1;
        }

        return String.format("%s%08d", prefix, nextSequence);
    }
}
