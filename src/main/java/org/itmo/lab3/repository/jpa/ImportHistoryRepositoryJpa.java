package org.itmo.lab3.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.itmo.lab3.model.ImportHistory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ImportHistoryRepositoryJpa {

    @PersistenceContext
    private EntityManager entityManager;

    public ImportHistory save(ImportHistory importHistory) {
        if (importHistory.getId() == null) {
            entityManager.persist(importHistory);
            return importHistory;
        } else {
            return entityManager.merge(importHistory);
        }
    }

    public Optional<ImportHistory> findById(Long id) {
        ImportHistory importHistory = entityManager.find(ImportHistory.class, id);
        return Optional.ofNullable(importHistory);
    }

    public List<ImportHistory> findAll() {
        return entityManager.createQuery("SELECT ih FROM ImportHistory ih ORDER BY ih.timestamp DESC", ImportHistory.class)
                .getResultList();
    }

    public void flush() {
        entityManager.flush();
    }
}
