package org.itmo.lab3.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.itmo.lab3.model.Address;
import org.itmo.lab3.model.Coordinates;
import org.itmo.lab3.model.Organization;
import org.itmo.lab3.repository.jpa.AddressRepositoryJpa;
import org.itmo.lab3.repository.jpa.CoordinatesRepositoryJpa;
import org.itmo.lab3.repository.jpa.OrganizationRepositoryJpa;
import org.itmo.lab3.util.Page;
import org.itmo.lab3.util.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class OrganizationService {

    private final OrganizationRepositoryJpa organizationRepository;
    private final AddressRepositoryJpa addressRepository;
    private final CoordinatesRepositoryJpa coordinatesRepository;
    private final WebSocketNotificationService notificationService;
    private final Validator validator;

    @Autowired
    public OrganizationService(OrganizationRepositoryJpa organizationRepository,
                               AddressRepositoryJpa addressRepository,
                               CoordinatesRepositoryJpa coordinatesRepository,
                               WebSocketNotificationService notificationService,
                               Validator validator) {
        this.organizationRepository = organizationRepository;
        this.addressRepository = addressRepository;
        this.coordinatesRepository = coordinatesRepository;
        this.notificationService = notificationService;
        this.validator = validator;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Organization createOrganization(Organization organization) {
        if (organization.getCreationDate() == null) {
            organization.setCreationDate(java.time.LocalDate.now());
        }
        
        // Bean Validation (структурные ограничения)
        validateOrganization(organization);
        // Бизнес-проверки уникальности
        ensureUniqueNameAndRatingForCreate(organization);
        
        if (organization.getCoordinates() != null) {
            handleCoordinates(organization);
        }

        if (organization.getPostalAddress() != null) {
            Address existing = addressRepository
                    .findByStreetAndZipCode(organization.getPostalAddress().getStreet(),
                            organization.getPostalAddress().getZipCode())
                    .orElse(null);
            if (existing != null) {
                organization.setPostalAddress(existing);
            }
        }

        if (organization.getOfficialAddress() != null) {
            Address existing = addressRepository
                    .findByStreetAndZipCode(organization.getOfficialAddress().getStreet(),
                            organization.getOfficialAddress().getZipCode())
                    .orElse(null);
            if (existing != null) {
                organization.setOfficialAddress(existing);
            }
        }

        try {
            Organization saved = organizationRepository.save(organization);
            organizationRepository.flush();
            Organization reloaded = organizationRepository.findById(saved.getId())
                    .orElse(saved);
            notificationService.notifyOrganizationCreated(reloaded.getId());
            return reloaded;
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Неверное значение координат: " + ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    private void ensureUniqueNameAndRatingForCreate(Organization organization) {
        // имя
        if (organizationRepository.existsByName(organization.getName())) {
            throw new IllegalArgumentException("Организация с таким именем уже существует");
        }
        // рейтинг
        if (!organizationRepository.findByRating(organization.getRating()).isEmpty()) {
            throw new IllegalArgumentException("Организация с таким рейтингом уже существует");
        }
    }

    private void validateOrganization(Organization organization) {
        Set<ConstraintViolation<Organization>> violations = validator.validate(organization);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Ошибки валидации: ");
            boolean first = true;
            for (ConstraintViolation<Organization> violation : violations) {
                if (!first) {
                    sb.append("; ");
                }
                sb.append(violation.getMessage());
                first = false;
            }
            throw new IllegalArgumentException(sb.toString());
        }
    }

    @Transactional(readOnly = true)
    public Optional<Organization> getOrganizationById(Long id) {
        return organizationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Organization> getAllOrganizations(PageRequest pageRequest) {
        Page<Organization> page = organizationRepository.findAll(pageRequest);
        page.getContent().forEach(this::initializeOrganization);
        return page;
    }

    @Transactional(readOnly = true)
    public List<Organization> getAllOrganizations() {
        List<Organization> organizations = organizationRepository.findAll();
        organizations.forEach(this::initializeOrganization);
        return organizations;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Organization updateOrganization(Organization organization) {
        if (!organizationRepository.existsById(organization.getId())) {
            throw new IllegalArgumentException("Organization not found");
        }
        
        try {
            if (organization.getCreationDate() == null) {
                organization.setCreationDate(java.time.LocalDate.now());
            }

            // Структурная валидация
            validateOrganization(organization);
            // Уникальность с учётом текущей сущности: допускаем то же имя/рейтинг
            ensureUniqueNameAndRatingForUpdate(organization);
            
            if (organization.getCoordinates() != null) {
                handleCoordinates(organization);
            }

            if (organization.getPostalAddress() != null && organization.getPostalAddress().getId() == null) {
                Address existing = addressRepository
                        .findByStreetAndZipCode(organization.getPostalAddress().getStreet(),
                                organization.getPostalAddress().getZipCode())
                        .orElse(null);
                if (existing != null) {
                    organization.setPostalAddress(existing);
                }
            }

            if (organization.getOfficialAddress() != null && organization.getOfficialAddress().getId() == null) {
                Address existing = addressRepository
                        .findByStreetAndZipCode(organization.getOfficialAddress().getStreet(),
                                organization.getOfficialAddress().getZipCode())
                        .orElse(null);
                if (existing != null) {
                    organization.setOfficialAddress(existing);
                }
            }

            Organization updated = organizationRepository.save(organization);
            organizationRepository.flush();
            Organization reloaded = organizationRepository.findById(updated.getId())
                    .orElse(updated);
            notificationService.notifyOrganizationUpdated(reloaded.getId());
            return reloaded;
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Неверное значение координат: " + ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    private void ensureUniqueNameAndRatingForUpdate(Organization organization) {
        // имя: существует кто-то другой с таким же именем?
        if (organizationRepository.existsByNameAndIdNot(organization.getName(), organization.getId())) {
            throw new IllegalArgumentException("Организация с таким именем уже существует");
        }
        // рейтинг: находим всех с таким рейтингом и проверяем, что нет других сущностей
        java.util.List<Organization> sameRating = organizationRepository.findByRating(organization.getRating());
        for (Organization other : sameRating) {
            if (!other.getId().equals(organization.getId())) {
                throw new IllegalArgumentException("Организация с таким рейтингом уже существует");
            }
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean deleteOrganization(Long id) {
        if (!organizationRepository.existsById(id)) {
            return false;
        }
        
        organizationRepository.deleteById(id);
        notificationService.notifyOrganizationDeleted(id);
        return true;
    }

    @Transactional(readOnly = true)
    public List<Organization> findOrganizationsByName(String name) {
        return organizationRepository.findByNameIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Organization> findOrganizationsByNameContaining(String name) {
        return organizationRepository.findByNameContainingIgnoreCase(name);
    }

    public int deleteOrganizationsByRating(Double rating) {
        List<Organization> organizations = organizationRepository.findByRating(rating);
        
        int count = organizations.size();
        organizationRepository.deleteAll(organizations);
        
        for (Organization org : organizations) {
            notificationService.notifyOrganizationDeleted(org.getId());
        }
        
        return count;
    }

    @Transactional(readOnly = true)
    public List<Organization> findOrganizationsByNameSubstring(String nameSubstring) {
        return organizationRepository.findByNameContainingIgnoreCase(nameSubstring);
    }

    @Transactional(readOnly = true)
    public List<Address> getUniquePostalAddresses() {
        return organizationRepository.findDistinctPostalAddresses();
    }

    public Organization mergeOrganizations(Long targetOrganizationId, Long sourceOrganizationId) {
        Organization target = organizationRepository.findById(targetOrganizationId)
                .orElseThrow(() -> new IllegalArgumentException("Целевая организация не найдена"));
        
        Organization source = organizationRepository.findById(sourceOrganizationId)
                .orElseThrow(() -> new IllegalArgumentException("Поглощаемая организация не найдена"));
        
        if (target.getId().equals(source.getId())) {
            throw new IllegalArgumentException("Нельзя поглотить организацию самой собой");
        }
        
        target.setEmployeesCount(target.getEmployeesCount() + source.getEmployeesCount());
        organizationRepository.save(target);
        
        organizationRepository.deleteById(source.getId());
        notificationService.notifyOrganizationDeleted(source.getId());
        
        notificationService.notifyOrganizationUpdated(target.getId());
        
        return target;
    }

    @Transactional(readOnly = true)
    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Coordinates> getAllCoordinates() {
        return coordinatesRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Coordinates getCoordinatesById(Long id) {
        return coordinatesRepository.findById(id).orElse(null);
    }
    
    public Organization addEmployeeToOrganization(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Организация не найдена"));
        
        organization.setEmployeesCount(organization.getEmployeesCount() + 1);
        Organization updated = organizationRepository.save(organization);
        notificationService.notifyOrganizationUpdated(updated.getId());
        return updated;
    }

    private void handleCoordinates(Organization organization) {
        Coordinates incoming = organization.getCoordinates();
        if (incoming == null) {
            return;
        }
        if (incoming.getId() != null) {
            Optional<Coordinates> existing = coordinatesRepository.findById(incoming.getId());
            if (existing.isPresent()) {
                Coordinates stored = existing.get();
                if (!Objects.equals(stored.getX(), incoming.getX()) ||
                        !Objects.equals(stored.getY(), incoming.getY())) {
                    Coordinates resolved = resolveCoordinates(incoming.getX(), incoming.getY());
                    organization.setCoordinates(resolved);
                } else {
                    organization.setCoordinates(stored);
                }
                return;
            }
        }
        Coordinates resolved = resolveCoordinates(incoming.getX(), incoming.getY());
        organization.setCoordinates(resolved);
    }

    private Coordinates resolveCoordinates(Integer x, Float y) {
        List<Coordinates> existingList = coordinatesRepository.findByXAndY(x, y);
        if (!existingList.isEmpty()) {
            return existingList.get(0);
        }
        Coordinates coordinates = new Coordinates();
        coordinates.setX(x);
        coordinates.setY(y);

        Coordinates saved = coordinatesRepository.save(coordinates);
        return saved;
    }

    private void initializeOrganization(Organization org) {
        if (org == null) {
            return;
        }
        if (org.getCoordinates() != null) {
            org.getCoordinates().getId();
            org.getCoordinates().getX();
            org.getCoordinates().getY();
        }
        if (org.getPostalAddress() != null) {
            org.getPostalAddress().getId();
            org.getPostalAddress().getStreet();
            org.getPostalAddress().getZipCode();
        }
        if (org.getOfficialAddress() != null) {
            org.getOfficialAddress().getId();
            org.getOfficialAddress().getStreet();
            org.getOfficialAddress().getZipCode();
        }
        org.getId();
        org.getName();
        org.getCreationDate();
        org.getAnnualTurnover();
        org.getEmployeesCount();
        org.getRating();
        org.getType();
    }
}

