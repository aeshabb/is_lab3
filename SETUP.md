# Инструкция по настройке лабораторной работы 3

## Доступ к приложению

Подключение к серверу с прокидыванием порта:
```bash
ssh -L 8080:localhost:23525 s408256@se.ifmo.ru
```

Формат: `-L локальный_порт:хост:удаленный_порт`
- **8080** - локальный порт на вашей машине
- **23525** - порт WildFly на сервере se.ifmo.ru

После запуска приложение доступно по адресу: **http://localhost:8080/lab3**

---

## Запуск JMeter тестов

```bash
~/apache-jmeter-5.6.3/bin/jmeter -n -t organization-load-test.jmx -l results-new.jtl
```

---

## 1. Установка MinIO на сервере se.ifmo.ru (FreeBSD, без Docker)

**Важно**: MinIO не предоставляет готовые бинарники для FreeBSD. Бинарник уже скомпилирован и находится в директории `minio/` проекта.

### Шаг 1: Загрузка бинарника на сервер

```bash
# Из директории проекта
scp minio/minio-freebsd s408256@se.ifmo.ru:~/minio/minio
```

### Шаг 2: Настройка на сервере

Подключаемся к серверу:
```bash
ssh s408256@se.ifmo.ru
```

Настраиваем MinIO:
```bash
# Создаем директории
mkdir -p ~/minio ~/minio-data
cd ~/minio

# Делаем файл исполняемым (если скачивали бинарник)
chmod +x minio
```

### Шаг 4: Настройка переменных окружения

```bash
# Устанавливаем учетные данные
export MINIO_ROOT_USER=minioadmin
export MINIO_ROOT_PASSWORD=minioadmin
```

### Шаг 5: Запуск MinIO

```bash
# Запуск MinIO на порту 9000
cd ~/minio
./minio server ~/minio-data --address ":9000" --console-address ":9001"
```

Для запуска в фоновом режиме:

```bash
nohup ./minio server ~/minio-data --address ":9000" --console-address ":9001" > minio.log 2>&1 &
```

### Шаг 6: Проверка работы

На сервере:
```bash
# Проверка что процесс запущен
ps aux | grep minio

# Проверка доступности API
curl http://localhost:9000/minio/health/live
```

### Шаг 7: Настройка приложения

Файл `src/main/resources/minio.properties` уже настроен:

```properties
minio.endpoint=http://localhost:9000
minio.accessKey=minioadmin
minio.secretKey=minioadmin
minio.bucket=import-files
```

MinIO работает на том же сервере, что и WildFly, поэтому используется localhost.

---

## 2. Конфигурация Druid Connection Pool

### Параметры пула соединений (JpaConfig.java):

| Параметр | Значение | Описание |
|----------|----------|----------|
| `initialSize` | 5 | Начальное количество соединений в пуле |
| `minIdle` | 5 | Минимальное количество простаивающих соединений |
| `maxActive` | 20 | Максимальное количество активных соединений |
| `maxWait` | 60000 | Максимальное время ожидания соединения (мс) |
| `timeBetweenEvictionRunsMillis` | 60000 | Интервал проверки соединений (мс) |
| `minEvictableIdleTimeMillis` | 300000 | Минимальное время жизни idle соединения (мс) |
| `validationQuery` | SELECT 1 | SQL-запрос для проверки валидности соединения |
| `testWhileIdle` | true | Проверять соединения при простое |
| `testOnBorrow` | false | Не проверять при получении (для производительности) |
| `testOnReturn` | false | Не проверять при возврате |
| `poolPreparedStatements` | true | Кэширование prepared statements |
| `maxPoolPreparedStatementPerConnectionSize` | 20 | Размер кэша prepared statements |

### Влияние параметров:

- **initialSize/minIdle**: Обеспечивают наличие готовых соединений при старте
- **maxActive**: Ограничивает нагрузку на БД
- **testWhileIdle**: Предотвращает использование "мертвых" соединений
- **poolPreparedStatements**: Ускоряет выполнение повторяющихся запросов

---

## 3. Конфигурация L2 JPA Cache (Ehcache)

### Параметры Hibernate (JpaConfig.java):

| Параметр | Значение | Описание |
|----------|----------|----------|
| `hibernate.cache.use_second_level_cache` | true | Включение L2 кэша |
| `hibernate.cache.use_query_cache` | true | Включение кэша запросов |
| `hibernate.cache.region.factory_class` | JCacheRegionFactory | Провайдер кэша |
| `hibernate.generate_statistics` | true | Сбор статистики для мониторинга |

### Параметры Ehcache (ehcache.xml):

| Параметр | Значение | Влияние на хранение |
|----------|----------|---------------------|
| `heap` | entries/MB | Хранение в куче JVM (быстрый доступ) |
| `offheap` | MB | Хранение вне кучи (не влияет на GC) |
| `disk` | MB | Персистентное хранение на диске |
| `ttl` | minutes | Время жизни записи в кэше |

### Стратегии кэширования (CacheConcurrencyStrategy):

- **READ_WRITE**: Для данных, которые читаются и обновляются (используется)
- **READ_ONLY**: Для данных, которые только читаются
- **NONSTRICT_READ_WRITE**: Для данных с редкими обновлениями
- **TRANSACTIONAL**: Для полной транзакционной изоляции

---

## 4. Управление логированием кэша (AOP)

### REST API эндпоинты:

```bash
# Включить логирование
curl -X POST http://localhost:8080/lab3/api/cache/logging/enable

# Отключить логирование
curl -X POST http://localhost:8080/lab3/api/cache/logging/disable

# Получить статус
curl http://localhost:8080/lab3/api/cache/logging/status

# Получить статистику
curl http://localhost:8080/lab3/api/cache/statistics

# Сбросить статистику
curl -X POST http://localhost:8080/lab3/api/cache/statistics/reset
```

### Формат статистики:

```json
{
  "secondLevelCacheHits": 150,
  "secondLevelCacheMisses": 25,
  "secondLevelCachePuts": 50,
  "queryCacheHits": 30,
  "queryCacheMisses": 10,
  "queryCachePuts": 15,
  "hitRatio": 85.7
}
```

---

## 5. Распределенная транзакция (2PC)

### Алгоритм двухфазного коммита:

```
┌─────────────────────────────────────────────────────────────┐
│                    ФАЗА 1: PREPARE                          │
│   1. Загрузка файла в MinIO                                 │
│   2. Получение objectName                                   │
├─────────────────────────────────────────────────────────────┤
│                    ФАЗА 2: VALIDATE                         │
│   3. Парсинг JSON                                          │
│   4. Валидация данных                                       │
├─────────────────────────────────────────────────────────────┤
│                    ФАЗА 3: COMMIT                           │
│   5. Сохранение данных в БД                                 │
│   6. Сохранение истории с ссылкой на файл                   │
├─────────────────────────────────────────────────────────────┤
│                    ROLLBACK (при ошибке)                    │
│   7. Удаление файла из MinIO                                │
│   8. Откат транзакции БД                                    │
└─────────────────────────────────────────────────────────────┘
```

### Сценарии отказов:

1. **Отказ MinIO**: Транзакция не начинается, пользователь получает ошибку
2. **Отказ БД**: Файл удаляется из MinIO, транзакция откатывается
3. **Ошибка бизнес-логики**: Полный откат обоих ресурсов

### Параллельные запросы:

- Использование `Isolation.SERIALIZABLE` для избежания гонок в БД
- Уникальные имена объектов в MinIO (UUID) исключают конфликты
- Атомарные операции обеспечивают консистентность

### Ситуации при параллельных запросах:

1. **Два пользователя импортируют организацию с одинаковым именем**:
   - Первый запрос успешно сохраняет организацию
   - Второй запрос получает ошибку уникальности при проверке
   - Файл второго пользователя удаляется из MinIO (rollback)

2. **Одновременный импорт разных файлов**:
   - Каждый файл получает уникальный UUID в MinIO
   - Транзакции БД изолированы (SERIALIZABLE)
   - Оба импорта выполняются последовательно

3. **Отказ MinIO во время параллельных запросов**:
   - Все запросы, не успевшие загрузить файл, завершаются с ошибкой
   - Никакие данные не записываются в БД

---

## 6. Конфигурация standalone.xml (WildFly)

В файле `standalone.xml` уже настроен datasource PostgreSQLDS с пулом соединений:

```xml
<datasource jndi-name="java:jboss/datasources/PostgreSQLDS" pool-name="PostgreSQLDS" enabled="true">
    <connection-url>jdbc:postgresql://pg:5432/studs</connection-url>
    <driver>postgresql</driver>
    <pool>
        <min-pool-size>5</min-pool-size>
        <max-pool-size>20</max-pool-size>
        <prefill>true</prefill>
    </pool>
    <security user-name="s408256" password="a3ag1NfP3rO3Gezo"/>
</datasource>
```

**Важно**: Данные подключения в `JpaConfig.java` должны совпадать с `standalone.xml`:
- URL: `jdbc:postgresql://pg:5432/studs`
- User: `s408256`
- Password: `a3ag1NfP3rO3Gezo`

---

## 7. Сборка и деплой

```bash
# Сборка проекта
mvn clean package

# Деплой на WildFly (на сервере se.ifmo.ru)
cp target/lab3.war $JBOSS_HOME/standalone/deployments/
```

---

## 8. Остановка сервисов после демонстрации

```bash
# Остановка MinIO
pkill -f minio

# Проверка что MinIO остановлен
ps aux | grep minio

# Остановка WildFly
$JBOSS_HOME/bin/jboss-cli.sh --connect command=:shutdown
```