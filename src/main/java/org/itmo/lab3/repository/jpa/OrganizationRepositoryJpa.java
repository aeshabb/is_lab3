package org.itmo.lab3.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.itmo.lab3.model.Address;
import org.itmo.lab3.model.Organization;
import org.itmo.lab3.util.PageRequest;
import org.itmo.lab3.util.PageImpl;
import org.itmo.lab3.util.Page;
import org.itmo.lab3.util.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class OrganizationRepositoryJpa {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Organization save(Organization organization) {
        if (organization.getId() == null) {
            entityManager.persist(organization);
            return organization;
        } else {
            return entityManager.merge(organization);
        }
    }

    public Optional<Organization> findById(Long id) {
        Organization organization = entityManager.find(Organization.class, id);
        return Optional.ofNullable(organization);
    }

    public List<Organization> findAll() {
        TypedQuery<Organization> query = entityManager.createQuery(
            "SELECT o FROM Organization o", 
            Organization.class
        );
        return query.getResultList();
    }

    public Page<Organization> findAll(PageRequest pageRequest) {
        // Запрос для данных
        String jpql = "SELECT o FROM Organization o";
        if (pageRequest.getSort() != null && pageRequest.getSort().isSorted()) {
            jpql += " ORDER BY " + buildOrderBy(pageRequest.getSort());
        }
        
        TypedQuery<Organization> query = entityManager.createQuery(jpql, Organization.class);
        query.setFirstResult(pageRequest.getPageNumber() * pageRequest.getPageSize());
        query.setMaxResults(pageRequest.getPageSize());
        List<Organization> content = query.getResultList();
        
        // Запрос для подсчёта общего количества
        TypedQuery<Long> countQuery = entityManager.createQuery(
            "SELECT COUNT(o) FROM Organization o", 
            Long.class
        );
        Long total = countQuery.getSingleResult();
        
        return new PageImpl<>(content, pageRequest, total);
    }

    public List<Organization> findByNameIgnoreCase(String name) {
        TypedQuery<Organization> query = entityManager.createQuery(
            "SELECT o FROM Organization o WHERE LOWER(o.name) = LOWER(:name)", 
            Organization.class
        );
        query.setParameter("name", name);
        return query.getResultList();
    }

    public List<Organization> findByNameContainingIgnoreCase(String nameSubstring) {
        TypedQuery<Organization> query = entityManager.createQuery(
            "SELECT o FROM Organization o WHERE LOWER(o.name) LIKE LOWER(:substring)", 
            Organization.class
        );
        query.setParameter("substring", "%" + nameSubstring + "%");
        return query.getResultList();
    }

    public List<Organization> findByRating(Double rating) {
        TypedQuery<Organization> query = entityManager.createQuery(
            "SELECT o FROM Organization o WHERE o.rating = :rating", 
            Organization.class
        );
        query.setParameter("rating", rating);
        return query.getResultList();
    }

    public long countByRating(Double rating) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(o) FROM Organization o WHERE o.rating = :rating",
            Long.class
        );
        query.setParameter("rating", rating);
        return query.getSingleResult();
    }

    public Page<Organization> findByEmployeesCountLessThan(Long maxEmployees, PageRequest pageRequest) {
        String jpql = "SELECT o FROM Organization o WHERE o.employeesCount < :maxEmployees";
        if (pageRequest.getSort() != null && pageRequest.getSort().isSorted()) {
            jpql += " ORDER BY " + buildOrderBy(pageRequest.getSort());
        }
        
        TypedQuery<Organization> query = entityManager.createQuery(jpql, Organization.class);
        query.setParameter("maxEmployees", maxEmployees);
        query.setFirstResult(pageRequest.getPageNumber() * pageRequest.getPageSize());
        query.setMaxResults(pageRequest.getPageSize());
        List<Organization> content = query.getResultList();

        TypedQuery<Long> countQuery = entityManager.createQuery(
            "SELECT COUNT(o) FROM Organization o WHERE o.employeesCount < :maxEmployees", 
            Long.class
        );
        countQuery.setParameter("maxEmployees", maxEmployees);
        Long total = countQuery.getSingleResult();
        
        return new PageImpl<>(content, pageRequest, total);
    }

    public List<Address> findDistinctPostalAddresses() {
        TypedQuery<Address> query = entityManager.createQuery(
            "SELECT DISTINCT o.postalAddress FROM Organization o WHERE o.postalAddress IS NOT NULL", 
            Address.class
        );
        return query.getResultList();
    }

    @Transactional
    public int deleteByRatingLessThan(Integer minRating) {
        return entityManager.createQuery(
            "DELETE FROM Organization o WHERE o.rating < :minRating"
        ).setParameter("minRating", minRating).executeUpdate();
    }

    @Transactional
    public void deleteById(Long id) {
        Organization organization = entityManager.find(Organization.class, id);
        if (organization != null) {
            entityManager.remove(organization);
        }
    }

    @Transactional
    public void deleteAll(Iterable<Organization> organizations) {
        for (Organization org : organizations) {
            if (entityManager.contains(org)) {
                entityManager.remove(org);
            } else {
                entityManager.remove(entityManager.merge(org));
            }
        }
    }

    public boolean existsById(Long id) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(o) FROM Organization o WHERE o.id = :id", 
            Long.class
        );
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }

    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(o) FROM Organization o", 
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
        entityManager.createQuery("DELETE FROM Organization o").executeUpdate();
    }

    public boolean existsByName(String name) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(o) FROM Organization o WHERE o.name = :name", 
            Long.class
        );
        query.setParameter("name", name);
        return query.getSingleResult() > 0;
    }

    public boolean existsByNameAndIdNot(String name, Long id) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(o) FROM Organization o WHERE o.name = :name AND o.id != :id", 
            Long.class
        );
        query.setParameter("name", name);
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }

    private String buildOrderBy(Sort sort) {
        if (sort.isSorted()) {
            StringBuilder sb = new StringBuilder();
            for (Sort.Order order : sort) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append("o.").append(order.getProperty())
                  .append(" ").append(order.getDirection());
            }
            return sb.toString();
        }
        return "o.id ASC";
    }
}
