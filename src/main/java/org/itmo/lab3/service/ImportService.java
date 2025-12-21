package org.itmo.lab3.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.itmo.lab3.dto.OrganizationImportDto;
import org.itmo.lab3.model.*;
import org.itmo.lab3.repository.jpa.AddressRepositoryJpa;
import org.itmo.lab3.repository.jpa.CoordinatesRepositoryJpa;
import org.itmo.lab3.repository.jpa.ImportHistoryRepositoryJpa;
import org.itmo.lab3.repository.jpa.OrganizationRepositoryJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class ImportService {

    private final OrganizationRepositoryJpa organizationRepository;
    private final AddressRepositoryJpa addressRepository;
    private final CoordinatesRepositoryJpa coordinatesRepository;
    private final ImportHistoryRepositoryJpa importHistoryRepository;
    private final Validator validator;
    private final WebSocketNotificationService notificationService;

    @Autowired
    public ImportService(OrganizationRepositoryJpa organizationRepository,
                        AddressRepositoryJpa addressRepository,
                        CoordinatesRepositoryJpa coordinatesRepository,
                        ImportHistoryRepositoryJpa importHistoryRepository,
                        Validator validator,
                        WebSocketNotificationService notificationService) {
        this.organizationRepository = organizationRepository;
        this.addressRepository = addressRepository;
        this.coordinatesRepository = coordinatesRepository;
        this.importHistoryRepository = importHistoryRepository;
        this.validator = validator;
        this.notificationService = notificationService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public ImportHistory importOrganizations(List<OrganizationImportDto> dtos, String username) {
        ImportHistory history = new ImportHistory();
        history.setUsername(username);
        history.setTimestamp(java.time.LocalDateTime.now());

        try {
            // Валидация всех объектов
            List<String> validationErrors = new ArrayList<>();
            for (int i = 0; i < dtos.size(); i++) {
                OrganizationImportDto dto = dtos.get(i);
                Set<ConstraintViolation<OrganizationImportDto>> violations = validator.validate(dto);
                if (!violations.isEmpty()) {
                    for (ConstraintViolation<OrganizationImportDto> violation : violations) {
                        validationErrors.add("Организация #" + (i + 1) + ": " + violation.getMessage());
                    }
                }
            }

            if (!validationErrors.isEmpty()) {
                throw new IllegalArgumentException("Ошибки валидации: " + String.join("; ", validationErrors));
            }

            // Проверка уникальности внутри импортируемого файла
            Set<String> namesInImport = new HashSet<>();
            Set<String> zipCodesInImport = new HashSet<>();
            Set<Double> ratingsInImport = new HashSet<>();
            
            for (int i = 0; i < dtos.size(); i++) {
                OrganizationImportDto dto = dtos.get(i);
                
                // Проверка уникальности имени внутри файла
                if (namesInImport.contains(dto.getName())) {
                    throw new IllegalArgumentException("Организация #" + (i + 1) + 
                        ": дублирующееся имя '" + dto.getName() + "' в файле импорта");
                }
                namesInImport.add(dto.getName());

                // Копим рейтинги для последующей проверки против БД
                if (dto.getRating() != null) {
                    ratingsInImport.add(dto.getRating());
                }

                // Проверка уникальности zipCode внутри файла (только внутри файла, не в БД)
                if (dto.getPostalAddress() != null) {
                    String zipCode = dto.getPostalAddress().getZipCode();
                    if (zipCodesInImport.contains(zipCode)) {
                        throw new IllegalArgumentException("Организация #" + (i + 1) + 
                            ": дублирующийся zipCode '" + zipCode + "' в файле импорта");
                    }
                    zipCodesInImport.add(zipCode);
                }

                if (dto.getOfficialAddress() != null) {
                    String zipCode = dto.getOfficialAddress().getZipCode();
                    if (zipCodesInImport.contains(zipCode)) {
                        throw new IllegalArgumentException("Организация #" + (i + 1) + 
                            ": дублирующийся zipCode '" + zipCode + "' в файле импорта");
                    }
                    zipCodesInImport.add(zipCode);
                }
            }

            // Проверка уникальности по БД: имя и рейтинг не должны уже существовать
            for (String name : namesInImport) {
                if (organizationRepository.existsByName(name)) {
                    throw new IllegalArgumentException(
                        "Организация с именем '" + name + "' уже существует в системе");
                }
            }
            for (Double rating : ratingsInImport) {
                if (!organizationRepository.findByRating(rating).isEmpty()) {
                    throw new IllegalArgumentException(
                        "Организация с рейтингом '" + rating + "' уже существует в системе");
                }
            }

            // Создание организаций
            int count = 0;
            for (OrganizationImportDto dto : dtos) {
                Organization organization = convertToEntity(dto);
                organizationRepository.save(organization);
                count++;
                notificationService.notifyOrganizationCreated(organization.getId());
            }

            organizationRepository.flush();

            history.setStatus("SUCCESS");
            history.setImportedCount(count);
            importHistoryRepository.save(history);
            importHistoryRepository.flush();

            return history;

        } catch (Exception e) {
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
            
            // Сохраняем историю в отдельной транзакции
            try {
                saveFailedHistory(history);
            } catch (Exception ex) {
                // Игнорируем ошибки сохранения истории
            }
            
            throw new RuntimeException("Импорт не выполнен: " + e.getMessage(), e);
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    protected void saveFailedHistory(ImportHistory history) {
        importHistoryRepository.save(history);
        importHistoryRepository.flush();
    }

    @Transactional(readOnly = true)
    public List<ImportHistory> getImportHistory() {
        return importHistoryRepository.findAll();
    }

    /**
     * Импорт организаций с сохранением ссылки на файл в MinIO.
     * Используется для распределенной транзакции.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public ImportHistory importOrganizationsWithFile(List<OrganizationImportDto> dtos, 
                                                      String username, 
                                                      String fileObjectName) {
        ImportHistory history = new ImportHistory();
        history.setUsername(username);
        history.setTimestamp(java.time.LocalDateTime.now());
        history.setFileObjectName(fileObjectName);

        try {
            // Валидация всех объектов
            List<String> validationErrors = new ArrayList<>();
            for (int i = 0; i < dtos.size(); i++) {
                OrganizationImportDto dto = dtos.get(i);
                Set<ConstraintViolation<OrganizationImportDto>> violations = validator.validate(dto);
                if (!violations.isEmpty()) {
                    for (ConstraintViolation<OrganizationImportDto> violation : violations) {
                        validationErrors.add("Организация #" + (i + 1) + ": " + violation.getMessage());
                    }
                }
            }

            if (!validationErrors.isEmpty()) {
                throw new IllegalArgumentException("Ошибки валидации: " + String.join("; ", validationErrors));
            }

            // Проверка уникальности внутри импортируемого файла
            Set<String> namesInImport = new HashSet<>();
            Set<String> zipCodesInImport = new HashSet<>();
            Set<Double> ratingsInImport = new HashSet<>();
            
            for (int i = 0; i < dtos.size(); i++) {
                OrganizationImportDto dto = dtos.get(i);
                
                if (namesInImport.contains(dto.getName())) {
                    throw new IllegalArgumentException("Организация #" + (i + 1) + 
                        ": дублирующееся имя '" + dto.getName() + "' в файле импорта");
                }
                namesInImport.add(dto.getName());

                if (dto.getRating() != null) {
                    ratingsInImport.add(dto.getRating());
                }

                if (dto.getPostalAddress() != null) {
                    String zipCode = dto.getPostalAddress().getZipCode();
                    if (zipCodesInImport.contains(zipCode)) {
                        throw new IllegalArgumentException("Организация #" + (i + 1) + 
                            ": дублирующийся zipCode '" + zipCode + "' в файле импорта");
                    }
                    zipCodesInImport.add(zipCode);
                }

                if (dto.getOfficialAddress() != null) {
                    String zipCode = dto.getOfficialAddress().getZipCode();
                    if (zipCodesInImport.contains(zipCode)) {
                        throw new IllegalArgumentException("Организация #" + (i + 1) + 
                            ": дублирующийся zipCode '" + zipCode + "' в файле импорта");
                    }
                    zipCodesInImport.add(zipCode);
                }
            }

            // Проверка уникальности по БД
            for (String name : namesInImport) {
                if (organizationRepository.existsByName(name)) {
                    throw new IllegalArgumentException(
                        "Организация с именем '" + name + "' уже существует в системе");
                }
            }
            for (Double rating : ratingsInImport) {
                if (!organizationRepository.findByRating(rating).isEmpty()) {
                    throw new IllegalArgumentException(
                        "Организация с рейтингом '" + rating + "' уже существует в системе");
                }
            }

            // Создание организаций
            int count = 0;
            for (OrganizationImportDto dto : dtos) {
                Organization organization = convertToEntity(dto);
                organizationRepository.save(organization);
                count++;
                notificationService.notifyOrganizationCreated(organization.getId());
            }

            organizationRepository.flush();

            history.setStatus("SUCCESS");
            history.setImportedCount(count);
            importHistoryRepository.save(history);
            importHistoryRepository.flush();

            return history;

        } catch (Exception e) {
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
            
            try {
                saveFailedHistoryWithFile(history);
            } catch (Exception ex) {
                // Игнорируем ошибки сохранения истории
            }
            
            throw new RuntimeException("Импорт не выполнен: " + e.getMessage(), e);
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    protected void saveFailedHistoryWithFile(ImportHistory history) {
        importHistoryRepository.save(history);
        importHistoryRepository.flush();
    }

    private Organization convertToEntity(OrganizationImportDto dto) {
        Organization organization = new Organization();
        organization.setName(dto.getName());
        organization.setAnnualTurnover(dto.getAnnualTurnover());
        organization.setEmployeesCount(dto.getEmployeesCount());
        organization.setRating(dto.getRating());
        organization.setCreationDate(LocalDate.now());
        
        try {
            organization.setType(OrganizationType.valueOf(dto.getType()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неверный тип организации: " + dto.getType());
        }

        // Coordinates
        if (dto.getCoordinates() != null) {
            Coordinates coordinates = new Coordinates();
            coordinates.setX(dto.getCoordinates().getX());
            coordinates.setY(dto.getCoordinates().getY());
            
            // Проверяем существование координат
            List<Coordinates> existingCoords = coordinatesRepository.findByXAndY(
                coordinates.getX(), coordinates.getY());
            if (!existingCoords.isEmpty()) {
                organization.setCoordinates(existingCoords.get(0));
            } else {
                organization.setCoordinates(coordinates);
            }
        }

        // Postal Address
        if (dto.getPostalAddress() != null) {
            Address postalAddress = new Address();
            postalAddress.setStreet(dto.getPostalAddress().getStreet());
            postalAddress.setZipCode(dto.getPostalAddress().getZipCode());
            organization.setPostalAddress(postalAddress);
        }

        // Official Address
        if (dto.getOfficialAddress() != null) {
            Address officialAddress = new Address();
            officialAddress.setStreet(dto.getOfficialAddress().getStreet());
            officialAddress.setZipCode(dto.getOfficialAddress().getZipCode());
            organization.setOfficialAddress(officialAddress);
        }

        return organization;
    }
}
