/*
package ru.t1.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CachedAspect {

    @Value("${cache.ttl-ms}")
    private long defaultTtlMs;

    // Хранилище для индивидуальных кэшей (по типам сущностей)
    private final Map<String, Map<List<Object>, CacheEntry>> individualCaches = new ConcurrentHashMap<>();

    // Общее хранилище для всех сущностей
    private final Map<Integer, CacheEntry> commonCache = new ConcurrentHashMap<>();

    @Around("@annotation(cached)")
    public Object cacheMethodResult(ProceedingJoinPoint joinPoint, Cached cached) throws Throwable {
        String cacheName = cached.cacheName();
        long ttl = cached.ttl() > 0 ? cached.ttl() : defaultTtlMs;

        // Генерируем ключ для поиска в кэше ДО выполнения метода
        CacheKeyInfo keyInfo = generateCacheKey(cacheName, joinPoint);

        // Пытаемся найти значение в кэше
        Object cachedValue = getFromCache(cacheName, keyInfo);
        if (cachedValue != null) {
            log.info("Возвращаем значение из кэша [{}] для ключа {}",
                    cacheName.isEmpty() ? "common" : cacheName, keyInfo.getKey());
            return cachedValue;
        }

        // Если в кэше нет - выполняем метод
        Object result = joinPoint.proceed();

        // Сохраняем результат в кэш (если он не пустой)
        if (result != null && !isEmptyResult(result)) {
            saveToCache(cacheName, keyInfo, result, ttl);
            log.info("Сохранили значение в кэш [{}] для ключа {} с TTL {} мс",
                    cacheName.isEmpty() ? "common" : cacheName, keyInfo.getKey(), ttl);
        }

        return result;
    }

    */
/**
     * Генерирует информацию о ключе для кэша
     *//*

    private CacheKeyInfo generateCacheKey(String cacheName, ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        if (!cacheName.isEmpty()) {
            // Для индивидуального кэша используем ВСЕ АРГУМЕНТЫ как составной ключ
            String methodName = joinPoint.getSignature().getName();
            List<Object> keyList = new ArrayList<>();
            keyList.add(methodName); // для уникальности добавляем сигнатуру метода
            keyList.addAll(Arrays.asList(args.length > 0 ? args : new Object[]{"default"}));

            return new CacheKeyInfo(keyList, args);
        } else {
            // Для общего кэша генерируем хэш из всех аргументов
            int keyHash = generateArgsHash(joinPoint);
            return new CacheKeyInfo(keyHash, args);
        }
    }

    */
/**
     * Получает значение из кэша
     *//*

    private Object getFromCache(String cacheName, CacheKeyInfo keyInfo) {
        if (!cacheName.isEmpty()) {
            // Индивидуальный кэш
            Map<List<Object>, CacheEntry> entityCache = individualCaches.get(cacheName);
            if (entityCache != null) {
                CacheEntry entry = entityCache.get(keyInfo.getKey());
                if (entry != null && !entry.isExpired()) {
                    return entry.value();
                }
                // Удаляем просроченную запись
                if (entry != null && entry.isExpired()) {
                    entityCache.remove(keyInfo.getKey());
                }
            }
        } else {
            // Общий кэш
            CacheEntry entry = commonCache.get(keyInfo.getKey());
            if (entry != null && !entry.isExpired()) {
                return entry.value();
            }
            // Удаляем просроченную запись
            if (entry != null && entry.isExpired()) {
                commonCache.remove(keyInfo.getKey());
            }
        }
        return null;
    }

    */
/**
     * Сохраняет значение в кэш
     *//*

    private void saveToCache(String cacheName, CacheKeyInfo keyInfo, Object result, long ttl) {
        CacheEntry entry = new CacheEntry(result, Instant.now().plusMillis(ttl));

        if (!cacheName.isEmpty()) {
            // Индивидуальный кэш
            Map<List<Object>, CacheEntry> entityCache = individualCaches
                    .computeIfAbsent(cacheName, k -> new ConcurrentHashMap<>());
            entityCache.put((List<Object>) keyInfo.getKey(), entry);
        } else {
            // Общий кэш
            commonCache.put((Integer) keyInfo.getKey(), entry);
        }
    }

    private int generateArgsHash(ProceedingJoinPoint joinPoint) {
        String fullMethodName = joinPoint.getSignature().toShortString(); // Class.method
        Object[] args = joinPoint.getArgs();

        Object[] keyElements = new Object[args.length + 1];
        keyElements[0] = fullMethodName;
        System.arraycopy(args, 0, keyElements, 1, args.length);

        return Arrays.hashCode(keyElements);
    }

    */
/**
     * Проверяет, является ли результат "пустым" (не нужно кэшировать)
     *//*

    private boolean isEmptyResult(Object result) {
        if (result instanceof Optional) {
            return ((Optional<?>) result).isEmpty();
        }
        if (result instanceof Collection) {
            return ((Collection<?>) result).isEmpty();
        }
        return false;
    }

    */
/**
     * Вспомогательный класс для информации о ключе кэша
     *//*

    private static class CacheKeyInfo {
        private final Object key;
        private final Object[] methodArgs;

        public CacheKeyInfo(Object key, Object[] methodArgs) {
            this.key = key;
            this.methodArgs = methodArgs;
        }

        public Object getKey() {
            return key;
        }

        public Object[] getMethodArgs() {
            return methodArgs;
        }
    }

    private record CacheEntry(Object value, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}*/
