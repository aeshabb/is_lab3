package org.itmo.lab3.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_history")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ImportHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "import_history_seq")
    @SequenceGenerator(name = "import_history_seq", sequenceName = "import_history_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // SUCCESS, FAILED

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "imported_count")
    private Integer importedCount;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "file_object_name", length = 255)
    private String fileObjectName; // Имя файла в MinIO

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public ImportHistory() {
    }

    public ImportHistory(String status, String username, Integer importedCount, String errorMessage) {
        this.status = status;
        this.username = username;
        this.importedCount = importedCount;
        this.errorMessage = errorMessage;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(Integer importedCount) {
        this.importedCount = importedCount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getFileObjectName() {
        return fileObjectName;
    }

    public void setFileObjectName(String fileObjectName) {
        this.fileObjectName = fileObjectName;
    }
}
