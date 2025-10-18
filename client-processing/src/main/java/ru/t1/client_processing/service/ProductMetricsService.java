package ru.t1.client_processing.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.t1.client_processing.entity.enums.ProductKey;
import ru.t1.client_processing.entity.enums.ProductStatus;
import ru.t1.client_processing.repository.ClientProductRepository;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductMetricsService {

    private final MeterRegistry meterRegistry;
    private final ClientProductRepository clientProductRepository;

    private final Map<ProductKey, AtomicLong> gaugeValues = new ConcurrentHashMap<>();

    /**
     * Инициализирует метрики при старте приложения
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("Инициализация метрик при старте...");

        // Инициализируем AtomicLong для каждого типа продукта
        Arrays.stream(ProductKey.values()).forEach(productKey -> {
            AtomicLong value = new AtomicLong(0);
            gaugeValues.put(productKey, value);

            // Регистрируем gauge один раз при старте
            meterRegistry.gauge("client_products_active",
                    Arrays.asList(io.micrometer.core.instrument.Tag.of("type", productKey.name())),
                    value);
        });

        updateProductMetrics();
    }

    /**
     * Обновляет метрики для всех типов продуктов
     */
    public void updateProductMetrics() {
        log.info("Обновление метрик продуктов...");

        Arrays.stream(ProductKey.values()).forEach(productKey -> {
            try {
                long activeCount = clientProductRepository.countByProductKeyAndStatus(
                        productKey, ProductStatus.ACTIVE);

                // Обновляем значение AtomicLong
                AtomicLong gaugeValue = gaugeValues.get(productKey);
                if (gaugeValue != null) {
                    gaugeValue.set(activeCount);
                }

                log.info("Продукт {}: {} активных", productKey, activeCount);

            } catch (Exception e) {
                log.error("Ошибка для продукта {}: {}", productKey, e.getMessage());
            }
        });
    }
}