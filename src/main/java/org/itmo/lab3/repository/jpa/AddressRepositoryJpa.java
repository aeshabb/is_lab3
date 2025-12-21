package org.itmo.lab3.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.itmo.lab3.model.Address;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class AddressRepositoryJpa {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Address save(Address address) {
        if (address.getId() == null) {
            entityManager.persist(address);
            return address;
        } else {
            return entityManager.merge(address);
        }
    }

    public Optional<Address> findById(Long id) {
        Address address = entityManager.find(Address.class, id);
        return Optional.ofNullable(address);
    }

    public List<Address> findAll() {
        TypedQuery<Address> query = entityManager.createQuery(
            "SELECT a FROM Address a", 
            Address.class
        );
        return query.getResultList();
    }

    public Optional<Address> findByStreetAndZipCode(String street, String zipCode) {
        TypedQuery<Address> query = entityManager.createQuery(
            "SELECT a FROM Address a WHERE a.street = :street AND a.zipCode = :zipCode", 
            Address.class
        );
        query.setParameter("street", street);
        query.setParameter("zipCode", zipCode);
        List<Address> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Transactional
    public void deleteById(Long id) {
        Address address = entityManager.find(Address.class, id);
        if (address != null) {
            entityManager.remove(address);
        }
    }

    public boolean existsById(Long id) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(a) FROM Address a WHERE a.id = :id", 
            Long.class
        );
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }

    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(a) FROM Address a", 
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
        entityManager.createQuery("DELETE FROM Address a").executeUpdate();
    }

    public boolean existsByZipCode(String zipCode) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(a) FROM Address a WHERE a.zipCode = :zipCode", 
            Long.class
        );
        query.setParameter("zipCode", zipCode);
        return query.getSingleResult() > 0;
    }

    public boolean existsByZipCodeAndIdNot(String zipCode, Long id) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(a) FROM Address a WHERE a.zipCode = :zipCode AND a.id != :id", 
            Long.class
        );
        query.setParameter("zipCode", zipCode);
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }
}
