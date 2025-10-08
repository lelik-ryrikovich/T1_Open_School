package ru.t1.client_processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.aop.LogDatasourceError;
import ru.t1.client_processing.dto.ClientProductRequest;
import ru.t1.client_processing.dto.ClientProductResponse;
import ru.t1.client_processing.entity.Client;
import ru.t1.client_processing.entity.ClientProduct;
import ru.t1.client_processing.entity.Product;
import ru.t1.client_processing.entity.enums.ProductKey;
import ru.t1.client_processing.entity.enums.ProductStatus;
import ru.t1.client_processing.exception.ClientNotFoundException;
import ru.t1.client_processing.exception.ClientProductAlreadyExistsException;
import ru.t1.client_processing.exception.ClientProductNotFoundException;
import ru.t1.client_processing.exception.ProductNotFoundException;
import ru.t1.client_processing.kafka.KafkaProducerService;
import ru.t1.client_processing.repository.ClientProductRepository;
import ru.t1.client_processing.repository.ClientRepository;
import ru.t1.client_processing.repository.ProductRepository;

import ru.t1.dto.KafkaMessageClientProduct;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления продуктами клиента.
 * Выполняет CRUD-операции и отправляет события о них в Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientProductService {

    private final ClientProductRepository clientProductRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final KafkaProducerService kafkaProducerService;

    @Value("${app.kafka.topics.client-products}")
    private String clientProductsTopic;

    @Value("${app.kafka.topics.client-credit-products}")
    private String clientCreditProductsTopic;

    // Продукты для МС-2
    private static final List<ProductKey> MS2_PRODUCTS = Arrays.asList(
            ProductKey.DC, ProductKey.CC, ProductKey.NS, ProductKey.PENS
    );

    // Продукты для МС-3
    private static final List<ProductKey> MS3_PRODUCTS = Arrays.asList(
            ProductKey.IPO, ProductKey.PC, ProductKey.AC
    );

    /**
     * Добавление продукта клиенту.
     *
     * @param request запрос с параметрами продукта
     * @return информация о добавленном продукте клиента
     * @throws ClientNotFoundException если клиент не найден
     * @throws ProductNotFoundException если продукт не найден
     * @throws ClientProductAlreadyExistsException если продукт уже привязан к клиенту
     */
    @Transactional
    @LogDatasourceError
    public ClientProductResponse addProductToClient(ClientProductRequest request) {
        log.info("Adding product {} to client {}", request.getProductId(), request.getClientId());

        // Проверяем существование клиента и продукта
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + request.getClientId()));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + request.getProductId()));

        // Проверяем, не добавлен ли уже продукт клиенту
        if (clientProductRepository.existsByClientIdAndProductId(client.getId(), product.getId())) {
            throw new ClientProductAlreadyExistsException(
                    "Product " + product.getName() + " already exists for client " + client.getClientId());
        }

        // Создаем ClientProduct
        ClientProduct clientProduct = new ClientProduct();
        clientProduct.setClient(client);
        clientProduct.setProduct(product);
        clientProduct.setOpenDate(request.getOpenDate() != null ? request.getOpenDate() : LocalDateTime.now());
        clientProduct.setCloseDate(request.getCloseDate());
        clientProduct.setStatus(ProductStatus.valueOf(request.getStatus()));

        ClientProduct savedClientProduct = clientProductRepository.save(clientProduct);
        log.info("Product added to client successfully. ClientProduct ID: {}", savedClientProduct.getId());

        // Отправляем сообщение в Kafka
        sendKafkaMessage("CREATE", savedClientProduct);

        return mapToResponse(savedClientProduct);
    }

    /**
     * Получение всех продуктов клиента.
     *
     * @param clientId идентификатор клиента
     * @return список продуктов клиента
     * @throws ClientNotFoundException если клиент не найден
     */
    @LogDatasourceError
    public List<ClientProductResponse> getClientProducts(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ClientNotFoundException("Client not found with id: " + clientId);
        }

        return clientProductRepository.findByClientId(clientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получение конкретного продукта клиента.
     *
     * @param clientProductId идентификатор продукта клиента
     * @return информация о продукте клиента
     * @throws ClientProductNotFoundException если продукт клиента не найден
     */
    @LogDatasourceError
    public ClientProductResponse getClientProduct(Long clientProductId) {
        ClientProduct clientProduct = clientProductRepository.findById(clientProductId)
                .orElseThrow(() -> new ClientProductNotFoundException("Client product not found with id: " + clientProductId));

        return mapToResponse(clientProduct);
    }

    /**
     * Обновление данных продукта клиента.
     *
     * @param clientProductId идентификатор продукта клиента
     * @param request обновлённые данные продукта
     * @return обновлённый продукт клиента
     * @throws ClientProductNotFoundException если продукт клиента не найден
     */
    @Transactional
    @LogDatasourceError
    public ClientProductResponse updateClientProduct(Long clientProductId, ClientProductRequest request) {
        log.info("Updating client product with id: {}", clientProductId);

        ClientProduct clientProduct = clientProductRepository.findById(clientProductId)
                .orElseThrow(() -> new ClientProductNotFoundException("Client product not found with id: " + clientProductId));

        // Обновляем поля
        if (request.getCloseDate() != null) {
            clientProduct.setCloseDate(request.getCloseDate());
        }
        if (request.getStatus() != null) {
            clientProduct.setStatus(ProductStatus.valueOf(request.getStatus()));
        }

        ClientProduct updatedClientProduct = clientProductRepository.save(clientProduct);
        log.info("Client product updated successfully. ID: {}", clientProductId);

        // Отправляем сообщение в Kafka
        sendKafkaMessage("UPDATE", updatedClientProduct);

        return mapToResponse(updatedClientProduct);
    }

    /**
     * Удаление продукта у клиента.
     *
     * @param clientProductId идентификатор продукта клиента
     * @throws ClientProductNotFoundException если продукт клиента не найден
     */
    @Transactional
    @LogDatasourceError
    public void removeProductFromClient(Long clientProductId) {
        log.info("Removing client product with id: {}", clientProductId);

        ClientProduct clientProduct = clientProductRepository.findById(clientProductId)
                .orElseThrow(() -> new ClientProductNotFoundException("Client product not found with id: " + clientProductId));

        // Сохраняем данные для отправки в Kafka перед удалением
        ClientProductResponse response = mapToResponse(clientProduct);

        clientProductRepository.delete(clientProduct);
        log.info("Client product removed successfully. ID: {}", clientProductId);

        // Отправляем сообщение в Kafka
        sendKafkaMessageForDelete(response);
    }

    /**
     * Вспомогательный метод для отправки сообщений в Kafka
     * @param operation Вид операции
     * @param clientProduct Клиентский продукт
     */
    private void sendKafkaMessage(String operation, ClientProduct clientProduct) {
        try {
            KafkaMessageClientProduct message = new KafkaMessageClientProduct();
            message.setOperation(operation);
            message.setClientProductId(clientProduct.getId());
            message.setProductName(clientProduct.getProduct().getName());
            message.setClientId(clientProduct.getClient().getId());
            message.setProductId(clientProduct.getProduct().getId());
            message.setProductKey(clientProduct.getProduct().getKey().name());
            message.setOpenDate(clientProduct.getOpenDate());
            message.setCloseDate(clientProduct.getCloseDate());
            message.setTimestamp(LocalDateTime.now());
            message.setStatus(clientProduct.getStatus().toString());

            // Определяем в какой топик отправлять
            ProductKey productKey = clientProduct.getProduct().getKey();
            String topic = getTopicForProductKey(productKey);

            kafkaProducerService.sendMessage(topic, message);
            log.info("Message sent to Kafka topic {} for product key {}", topic, productKey);

        } catch (Exception e) {
            log.error("Failed to send message to Kafka for client product {}", clientProduct.getId(), e);
            // Не прерываем основную операцию из-за ошибки Kafka
        }
    }

    private void sendKafkaMessageForDelete(ClientProductResponse response) {
        try {
            KafkaMessageClientProduct message = new KafkaMessageClientProduct();
            message.setOperation("DELETE");
            message.setClientProductId(response.getId());
            message.setClientId(response.getClientId());
            message.setProductId(response.getProductId());
            message.setProductKey(response.getProductKey().name());
            message.setTimestamp(LocalDateTime.now());

            String topic = getTopicForProductKey(response.getProductKey());
            kafkaProducerService.sendMessage(topic, message);
            log.info("Delete message sent to Kafka topic {}", topic);

        } catch (Exception e) {
            log.error("Failed to send delete message to Kafka for client product {}", response.getId(), e);
        }
    }

    /**
     * Определение нужного топика по ключу продукта
     * @param productKey Ключ продукта
     * @return Название топика
     */
    private String getTopicForProductKey(ProductKey productKey) {
        if (MS2_PRODUCTS.contains(productKey)) {
            //return kafkaTopicService.getClientProductsTopic();
            return clientProductsTopic;
        } else if (MS3_PRODUCTS.contains(productKey)) {
            //return kafkaTopicService.getClientCreditProductsTopic();
            return clientCreditProductsTopic;
        }
        return null;
    }

    /**
     * Конвертация в ответ
     * @param clientProduct Продукт клиента
     * @return Ответ
     */
    private ClientProductResponse mapToResponse(ClientProduct clientProduct) {
        ClientProductResponse response = new ClientProductResponse();
        response.setId(clientProduct.getId());
        response.setClientId(clientProduct.getClient().getId());
        response.setProductId(clientProduct.getProduct().getId());
        response.setProductName(clientProduct.getProduct().getName());
        response.setProductKey(clientProduct.getProduct().getKey());
        response.setOpenDate(clientProduct.getOpenDate());
        response.setCloseDate(clientProduct.getCloseDate());
        response.setStatus(clientProduct.getStatus());
        return response;
    }
}