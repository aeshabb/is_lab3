package org.itmo.lab3.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.itmo.lab3.model.Coordinates;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class CoordinatesRepositoryJpa {

    @PersistenceContext
    private EntityManager entityManager;


    @Transactional
    public Coordinates save(Coordinates coordinates) {
        if (coordinates.getId() == null) {
            entityManager.persist(coordinates);
            return coordinates;
        } else {
            return entityManager.merge(coordinates);
        }
    }

 
    public Optional<Coordinates> findById(Long id) {
        Coordinates coordinates = entityManager.find(Coordinates.class, id);
        return Optional.ofNullable(coordinates);
    }

    public List<Coordinates> findAll() {
        TypedQuery<Coordinates> query = entityManager.createQuery(
            "SELECT c FROM Coordinates c", 
            Coordinates.class
        );
        return query.getResultList();
    }

    /**
     * Найти по X и Y
     */
    public List<Coordinates> findByXAndY(Integer x, Float y) {
        TypedQuery<Coordinates> query = entityManager.createQuery(
            "SELECT c FROM Coordinates c WHERE c.x = :x AND c.y = :y", 
            Coordinates.class
        );
        query.setParameter("x", x);
        query.setParameter("y", y);
        return query.getResultList();
    }

    /**
     * Удалить по ID
     */
    @Transactional
    public void deleteById(Long id) {
        Coordinates coordinates = entityManager.find(Coordinates.class, id);
        if (coordinates != null) {
            entityManager.remove(coordinates);
        }
    }

    /**
     * Проверить существование по ID
     */
    public boolean existsById(Long id) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(c) FROM Coordinates c WHERE c.id = :id", 
            Long.class
        );
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }

    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(c) FROM Coordinates c", 
            Long.class
        );
        return query.getSingleResult();
    }

    @Transactional
    public void flush() {
        entityManager.flush();
    }

    @Transactional
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM Coordinates c").executeUpdate();
    }
}
