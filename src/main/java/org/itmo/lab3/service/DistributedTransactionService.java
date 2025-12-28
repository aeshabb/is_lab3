package org.itmo.lab3.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.itmo.lab3.dto.OrganizationImportDto;
import org.itmo.lab3.model.ImportHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
public class DistributedTransactionService {

    private static final Logger logger = Logger.getLogger(DistributedTransactionService.class.getName());

    private final MinioService minioService;
    private final ImportService importService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DistributedTransactionService(MinioService minioService, 
                                         ImportService importService) {
        this.minioService = minioService;
        this.importService = importService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Выполнить распределенный импорт с двухфазным коммитом
     */
    public ImportResult executeDistributedImport(MultipartFile file, String username) {
        String objectName = null;
        TransactionPhase currentPhase = TransactionPhase.INIT;
        
        try {
            // ФАЗА 1: PREPARE - Загрузка файла в MinIO
            currentPhase = TransactionPhase.PREPARE_MINIO;
            logger.info("[2PC] Phase 1: PREPARE - Uploading file to MinIO");
            
            byte[] fileContent = file.getBytes();
            objectName = minioService.uploadFile(
                fileContent, 
                file.getOriginalFilename(), 
                file.getContentType()
            );
            
            logger.info("[2PC] Phase 1: PREPARE - File uploaded: " + objectName);
            
            // ФАЗА 1: Парсинг JSON
            currentPhase = TransactionPhase.VALIDATE;
            logger.info("[2PC] Phase 1: Parsing JSON");
            
            List<OrganizationImportDto> organizations = objectMapper.readValue(
                fileContent, 
                new TypeReference<List<OrganizationImportDto>>() {}
            );
            
            if (organizations.isEmpty()) {
                throw new IllegalArgumentException("Файл не содержит организаций");
            }
            
            // ФАЗА 2: COMMIT - Сохранение данных в БД
            currentPhase = TransactionPhase.COMMIT_DB;
            logger.info("[2PC] Phase 2: COMMIT - Saving data to database");
            
            ImportHistory history = importService.importOrganizationsWithFile(
                organizations, 
                username, 
                objectName
            );
            
            logger.info("[2PC] Transaction COMMITTED successfully");
            
            return new ImportResult(true, history, null);
            
        } catch (MinioService.MinioUploadException e) {
            // Ошибка на этапе загрузки в MinIO - ничего откатывать не нужно
            logger.log(Level.SEVERE, "[2PC] ROLLBACK - MinIO upload failed", e);
            return new ImportResult(false, null, "Ошибка загрузки файла в хранилище: " + e.getMessage());
            
        } catch (Exception e) {
            // Ошибка после успешной загрузки в MinIO - нужно откатить
            logger.log(Level.SEVERE, "[2PC] ROLLBACK - Error in phase: " + currentPhase, e);
            
            // Откат MinIO если файл был загружен
            if (objectName != null) {
                rollbackMinio(objectName);
            }
            
            return new ImportResult(false, null, "Ошибка импорта: " + e.getMessage());
        }
    }

    /**
     * Откат загрузки файла в MinIO.
     */
    private void rollbackMinio(String objectName) {
        try {
            logger.info("[2PC] ROLLBACK - Deleting file from MinIO: " + objectName);
            minioService.deleteFile(objectName);
            logger.info("[2PC] ROLLBACK - File deleted successfully");
        } catch (Exception e) {
            // Логируем ошибку, но не пробрасываем - основная ошибка важнее
            logger.log(Level.SEVERE, "[2PC] ROLLBACK FAILED - Could not delete file from MinIO: " + objectName, e);
        }
    }

    /**
     * Скачать файл импорта из MinIO.
     * 
     * @param objectName имя объекта в MinIO
     * @return содержимое файла
     */
    public byte[] downloadImportFile(String objectName) throws MinioService.MinioDownloadException {
        return minioService.downloadFile(objectName);
    }

    /**
     * Проверить доступность MinIO.
     */
    public boolean isMinioAvailable() {
        try {
            // Попытка выполнить простую операцию
            return minioService.getBucketName() != null;
        } catch (Exception e) {
            return false;
        }
    }

    // Вспомогательные классы
    
    public enum TransactionPhase {
        INIT,
        PREPARE_MINIO,
        VALIDATE,
        COMMIT_DB
    }

    public static class ImportResult {
        private final boolean success;
        private final ImportHistory history;
        private final String errorMessage;

        public ImportResult(boolean success, ImportHistory history, String errorMessage) {
            this.success = success;
            this.history = history;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public ImportHistory getHistory() {
            return history;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
