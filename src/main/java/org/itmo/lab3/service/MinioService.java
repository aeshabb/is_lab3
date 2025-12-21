package org.itmo.lab3.service;

import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Сервис для работы с MinIO (S3-совместимое хранилище).
 * 
 * Обеспечивает:
 * - Загрузку файлов в MinIO
 * - Скачивание файлов из MinIO
 * - Удаление файлов из MinIO
 * - Проверку существования файлов
 */
@Service
public class MinioService implements InitializingBean {

    private static final Logger logger = Logger.getLogger(MinioService.class.getName());

    private final MinioClient minioClient;
    private final String bucketName;

    @Autowired
    public MinioService(MinioClient minioClient, @Qualifier("minioBucket") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    /**
     * Инициализация: создание bucket если он не существует
     */
    @Override
    public void afterPropertiesSet() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!bucketExists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
                );
                logger.info("Created MinIO bucket: " + bucketName);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to initialize MinIO bucket: " + e.getMessage(), e);
        }
    }

    /**
     * Загрузить файл в MinIO.
     * 
     * @param content содержимое файла
     * @param originalFileName оригинальное имя файла
     * @param contentType MIME тип файла
     * @return уникальное имя объекта в MinIO
     */
    public String uploadFile(byte[] content, String originalFileName, String contentType) 
            throws MinioUploadException {
        String objectName = generateObjectName(originalFileName);
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, content.length, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build()
            );
            logger.info("Uploaded file to MinIO: " + objectName);
            return objectName;
        } catch (Exception e) {
            throw new MinioUploadException("Failed to upload file to MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Скачать файл из MinIO.
     * 
     * @param objectName имя объекта в MinIO
     * @return содержимое файла
     */
    public byte[] downloadFile(String objectName) throws MinioDownloadException {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new MinioDownloadException("Failed to download file from MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Удалить файл из MinIO.
     * 
     * @param objectName имя объекта в MinIO
     */
    public void deleteFile(String objectName) throws MinioDeleteException {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            logger.info("Deleted file from MinIO: " + objectName);
        } catch (Exception e) {
            throw new MinioDeleteException("Failed to delete file from MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Проверить существование файла в MinIO.
     * 
     * @param objectName имя объекта в MinIO
     * @return true если файл существует
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            logger.log(Level.WARNING, "Error checking file existence: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error checking file existence: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Генерация уникального имени объекта.
     */
    private String generateObjectName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    public String getBucketName() {
        return bucketName;
    }

    // Custom exceptions
    public static class MinioUploadException extends Exception {
        public MinioUploadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class MinioDownloadException extends Exception {
        public MinioDownloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class MinioDeleteException extends Exception {
        public MinioDeleteException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
