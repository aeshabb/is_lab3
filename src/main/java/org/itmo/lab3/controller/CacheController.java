package org.itmo.lab3.controller;

import org.itmo.lab3.aspect.CacheStatisticsAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST контроллер для управления логированием статистики L2 кэша.
 * 
 * Эндпоинты:
 * - GET /api/cache/statistics - получить текущую статистику
 * - POST /api/cache/logging/enable - включить логирование
 * - POST /api/cache/logging/disable - отключить логирование
 * - GET /api/cache/logging/status - получить статус логирования
 * - POST /api/cache/statistics/reset - сбросить статистику
 */
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheStatisticsAspect cacheStatisticsAspect;

    @Autowired
    public CacheController(CacheStatisticsAspect cacheStatisticsAspect) {
        this.cacheStatisticsAspect = cacheStatisticsAspect;
    }

    /**
     * Получить текущую статистику кэша
     */
    @GetMapping("/statistics")
    public ResponseEntity<CacheStatisticsAspect.CacheStatisticsDto> getStatistics() {
        return ResponseEntity.ok(cacheStatisticsAspect.getStatistics());
    }

    /**
     * Включить логирование статистики кэша
     */
    @PostMapping("/logging/enable")
    public ResponseEntity<Map<String, Object>> enableLogging() {
        cacheStatisticsAspect.enableLogging();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("loggingEnabled", true);
        response.put("message", "Cache statistics logging enabled");
        return ResponseEntity.ok(response);
    }

    /**
     * Отключить логирование статистики кэша
     */
    @PostMapping("/logging/disable")
    public ResponseEntity<Map<String, Object>> disableLogging() {
        cacheStatisticsAspect.disableLogging();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("loggingEnabled", false);
        response.put("message", "Cache statistics logging disabled");
        return ResponseEntity.ok(response);
    }

    /**
     * Получить статус логирования
     */
    @GetMapping("/logging/status")
    public ResponseEntity<Map<String, Object>> getLoggingStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("loggingEnabled", cacheStatisticsAspect.isLoggingEnabled());
        return ResponseEntity.ok(response);
    }

    /**
     * Сбросить статистику кэша
     */
    @PostMapping("/statistics/reset")
    public ResponseEntity<Map<String, Object>> resetStatistics() {
        cacheStatisticsAspect.resetStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cache statistics reset");
        return ResponseEntity.ok(response);
    }
}
