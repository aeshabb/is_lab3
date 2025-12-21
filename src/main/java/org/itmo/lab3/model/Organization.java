package org.itmo.lab3.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.itmo.lab3.validation.UniqueRating;

@Entity
@Table(name = "organizations")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_seq")
    @SequenceGenerator(name = "org_seq", sequenceName = "organizations_id_seq", allocationSize = 1)
    @Positive(message = "Значение поля должно быть больше 0")
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull(message = "Название не может быть null")
    @NotBlank(message = "Строка не может быть пустой")
    @org.itmo.lab3.validation.UniqueOrganizationName
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "Координаты не могут быть null")
    @Valid
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "coordinates_id", nullable = false)
    private Coordinates coordinates;

    @NotNull(message = "Дата создания не может быть null")
    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "official_address_id")
    private Address officialAddress;

    @NotNull(message = "Годовой оборот не может быть null")
    @Positive(message = "Значение поля должно быть больше 0")
    @Column(name = "annual_turnover", nullable = false)
    private Long annualTurnover;

    @Positive(message = "Значение поля должно быть больше 0")
    @Column(name = "employees_count", nullable = false)
    private Integer employeesCount;

    @NotNull(message = "Рейтинг не может быть null")
    @Positive(message = "Значение поля должно быть больше 0")
    @UniqueRating
    @Column(name = "rating", nullable = false)
    private Double rating;

    @NotNull(message = "Тип организации не может быть null")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OrganizationType type;

    @NotNull(message = "Почтовый адрес не может быть null")
    @Valid
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "postal_address_id", nullable = false)
    private Address postalAddress;

    @PrePersist
    protected void onCreate() {
        if (creationDate == null) {
            creationDate = LocalDate.now();
        }
    }

    public Organization() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public Address getOfficialAddress() {
        return officialAddress;
    }

    public void setOfficialAddress(Address officialAddress) {
        this.officialAddress = officialAddress;
    }

    public Long getAnnualTurnover() {
        return annualTurnover;
    }

    public void setAnnualTurnover(Long annualTurnover) {
        this.annualTurnover = annualTurnover;
    }

    public Integer getEmployeesCount() {
        return employeesCount;
    }

    public void setEmployeesCount(Integer employeesCount) {
        this.employeesCount = employeesCount;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public OrganizationType getType() {
        return type;
    }

    public void setType(OrganizationType type) {
        this.type = type;
    }

    public Address getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(Address postalAddress) {
        this.postalAddress = postalAddress;
    }
}

