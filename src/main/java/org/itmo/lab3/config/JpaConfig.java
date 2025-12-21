package org.itmo.lab3.config;

import com.alibaba.druid.pool.DruidDataSource;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.sql.SQLException;
import java.util.Properties;


@Configuration
@EnableTransactionManagement
public class JpaConfig {

    /**
     * Druid Connection Pool Configuration
     * 
     * Параметры конфигурации пула соединений Druid:
     * - initialSize: начальное количество соединений в пуле (5)
     * - minIdle: минимальное количество простаивающих соединений (5)
     * - maxActive: максимальное количество активных соединений (20)
     * - maxWait: максимальное время ожидания соединения в мс (60000)
     * - timeBetweenEvictionRunsMillis: интервал проверки соединений на валидность (60000 мс)
     * - minEvictableIdleTimeMillis: минимальное время жизни простаивающего соединения (300000 мс)
     * - validationQuery: SQL-запрос для проверки валидности соединения
     * - testWhileIdle: проверять соединения при простое
     * - testOnBorrow: проверять соединение перед выдачей
     * - testOnReturn: проверять соединение при возврате в пул
     * - poolPreparedStatements: кэширование prepared statements
     * - maxPoolPreparedStatementPerConnectionSize: размер кэша prepared statements
     */
    @Bean(initMethod = "init", destroyMethod = "close")
    public DataSource dataSource() throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        
        // Основные параметры подключения (соответствуют standalone.xml)
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://pg:5432/studs");
        dataSource.setUsername("s408256");
        dataSource.setPassword("a3ag1NfP3rO3Gezo");
        
        // Параметры пула соединений
        dataSource.setInitialSize(5);           // Начальный размер пула
        dataSource.setMinIdle(5);               // Минимум простаивающих соединений
        dataSource.setMaxActive(20);            // Максимум активных соединений
        dataSource.setMaxWait(60000);           // Макс. время ожидания соединения (мс)
        
        // Параметры проверки и очистки соединений
        dataSource.setTimeBetweenEvictionRunsMillis(60000);  // Интервал проверки (мс)
        dataSource.setMinEvictableIdleTimeMillis(300000);    // Мин. время жизни idle соединения
        dataSource.setValidationQuery("SELECT 1");           // Запрос для проверки соединения
        dataSource.setTestWhileIdle(true);                   // Проверять idle соединения
        dataSource.setTestOnBorrow(false);                   // Не проверять при получении
        dataSource.setTestOnReturn(false);                   // Не проверять при возврате
        
        // Кэширование prepared statements
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws SQLException {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("org.itmo.lab3.model");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.use_sql_comments", "true");
        
        // L2 Cache Configuration with Ehcache
        // Включение кэша второго уровня
        properties.setProperty("hibernate.cache.use_second_level_cache", "true");
        // Включение кэша запросов
        properties.setProperty("hibernate.cache.use_query_cache", "true");
        // Указание провайдера кэша (JCache с Ehcache)
        properties.setProperty("hibernate.cache.region.factory_class", 
            "org.hibernate.cache.jcache.JCacheRegionFactory");
        // Путь к конфигурации Ehcache
        properties.setProperty("hibernate.javax.cache.uri", "classpath:ehcache.xml");
        // Включение статистики для мониторинга кэша
        properties.setProperty("hibernate.generate_statistics", "true");
        
        em.setJpaProperties(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(@Autowired EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
}

