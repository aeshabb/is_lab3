package org.itmo.lab3.aspect;

import jakarta.persistence.EntityManagerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * AOP Аспект для логирования статистики L2 JPA Cache.
 */
@Aspect
@Component
public class CacheStatisticsAspect {

    private static final Logger logger = Logger.getLogger(CacheStatisticsAspect.class.getName());
    
    private final AtomicBoolean loggingEnabled = new AtomicBoolean(false);
    private final Statistics statistics;

    @Autowired
    public CacheStatisticsAspect(EntityManagerFactory entityManagerFactory) {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        this.statistics = sessionFactory.getStatistics();
        this.statistics.setStatisticsEnabled(true);
    }

    /**
     * Включить логирование статистики кэша
     */
    public void enableLogging() {
        loggingEnabled.set(true);
        try {
            this.statistics.setStatisticsEnabled(true);
        } catch (Exception e) {
            logger.warning("Failed to enable statistics collection: " + e.getMessage());
        }
        logger.info("Cache statistics logging ENABLED");
    }

    /**
     * Отключить логирование статистики кэша
     */
    public void disableLogging() {
        loggingEnabled.set(false);
        try {
            this.statistics.setStatisticsEnabled(false);
        } catch (Exception e) {
            logger.warning("Failed to disable statistics collection: " + e.getMessage());
        }
        logger.info("Cache statistics logging DISABLED");
    }

    /**
     * Проверить, включено ли логирование
     */
    public boolean isLoggingEnabled() {
        return loggingEnabled.get();
    }

    /**
     * Получить текущую статистику кэша
     */
    public CacheStatisticsDto getStatistics() {
        return new CacheStatisticsDto(
            statistics.getSecondLevelCacheHitCount(),
            statistics.getSecondLevelCacheMissCount(),
            statistics.getSecondLevelCachePutCount(),
            statistics.getQueryCacheHitCount(),
            statistics.getQueryCacheMissCount(),
            statistics.getQueryCachePutCount()
        );
    }

    /**
     * Сбросить статистику кэша
     */
    public void resetStatistics() {
        statistics.clear();
        logger.info("Cache statistics cleared");
    }

    /**
     * AOP advice, который логирует статистику кэша после каждой операции с репозиторием
     */
    @Around("execution(* org.itmo.lab3.repository..*.*(..))")
    public Object logCacheStatistics(ProceedingJoinPoint joinPoint) throws Throwable {
        long hitsBefore = statistics.getSecondLevelCacheHitCount();
        long missesBefore = statistics.getSecondLevelCacheMissCount();
        
        Object result = joinPoint.proceed();
        
        if (loggingEnabled.get()) {
            long hitsAfter = statistics.getSecondLevelCacheHitCount();
            long missesAfter = statistics.getSecondLevelCacheMissCount();
            
            long newHits = hitsAfter - hitsBefore;
            long newMisses = missesAfter - missesBefore;
            
            if (newHits > 0 || newMisses > 0) {
                logger.info(String.format(
                    "[CACHE] %s.%s - Hits: %d, Misses: %d | Total - Hits: %d, Misses: %d",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    newHits,
                    newMisses,
                    hitsAfter,
                    missesAfter
                ));
            }
        }
        
        return result;
    }

    /**
     * DTO для передачи статистики кэша
     */
    public static class CacheStatisticsDto {
        private final long secondLevelCacheHits;
        private final long secondLevelCacheMisses;
        private final long secondLevelCachePuts;
        private final long queryCacheHits;
        private final long queryCacheMisses;
        private final long queryCachePuts;

        public CacheStatisticsDto(long secondLevelCacheHits, long secondLevelCacheMisses, 
                                  long secondLevelCachePuts, long queryCacheHits, 
                                  long queryCacheMisses, long queryCachePuts) {
            this.secondLevelCacheHits = secondLevelCacheHits;
            this.secondLevelCacheMisses = secondLevelCacheMisses;
            this.secondLevelCachePuts = secondLevelCachePuts;
            this.queryCacheHits = queryCacheHits;
            this.queryCacheMisses = queryCacheMisses;
            this.queryCachePuts = queryCachePuts;
        }

        public long getSecondLevelCacheHits() { return secondLevelCacheHits; }
        public long getSecondLevelCacheMisses() { return secondLevelCacheMisses; }
        public long getSecondLevelCachePuts() { return secondLevelCachePuts; }
        public long getQueryCacheHits() { return queryCacheHits; }
        public long getQueryCacheMisses() { return queryCacheMisses; }
        public long getQueryCachePuts() { return queryCachePuts; }
        
        public double getHitRatio() {
            long total = secondLevelCacheHits + secondLevelCacheMisses;
            return total > 0 ? (double) secondLevelCacheHits / total * 100 : 0;
        }
    }
}
