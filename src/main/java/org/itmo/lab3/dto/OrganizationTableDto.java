package org.itmo.lab3.dto;

import java.time.LocalDate;
import org.itmo.lab3.model.Organization;
import org.itmo.lab3.model.OrganizationType;

public class OrganizationTableDto {
    private final Long id;
    private final String name;
    private final Integer coordX;
    private final Float coordY;
    private final LocalDate creationDate;
    private final Long annualTurnover;
    private final Integer employeesCount;
    private final Double rating;
    private final OrganizationType type;

    public OrganizationTableDto(Long id,
                                String name,
                                Integer coordX,
                                Float coordY,
                                LocalDate creationDate,
                                Long annualTurnover,
                                Integer employeesCount,
                                Double rating,
                                OrganizationType type) {
        this.id = id;
        this.name = name;
        this.coordX = coordX;
        this.coordY = coordY;
        this.creationDate = creationDate;
        this.annualTurnover = annualTurnover;
        this.employeesCount = employeesCount;
        this.rating = rating;
        this.type = type;
    }

    public static OrganizationTableDto fromEntity(Organization organization) {
        return new OrganizationTableDto(
                organization.getId(),
                organization.getName(),
                organization.getCoordinates() != null ? organization.getCoordinates().getX() : null,
                organization.getCoordinates() != null ? organization.getCoordinates().getY() : null,
                organization.getCreationDate(),
                organization.getAnnualTurnover(),
                organization.getEmployeesCount(),
                organization.getRating(),
                organization.getType()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getCoordX() {
        return coordX;
    }

    public Float getCoordY() {
        return coordY;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public Long getAnnualTurnover() {
        return annualTurnover;
    }

    public Integer getEmployeesCount() {
        return employeesCount;
    }

    public Double getRating() {
        return rating;
    }

    public OrganizationType getType() {
        return type;
    }

    public boolean isHasCoordinates() {
        return coordX != null && coordY != null;
    }
}

