package org.itmo.lab3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "addresses")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Улица не может быть null")
    @Size(max = 180, message = "Длина строки не должна быть больше 180")
    @Column(name = "street", nullable = false, length = 180)
    private String street;

    @NotNull(message = "Почтовый индекс не может быть null")
    @Size(min = 7, message = "Длина строки должна быть не меньше 7")
    @Column(name = "zip_code", nullable = false, length = 50)
    private String zipCode;

    public Address() {
    }

    public Address(String street, String zipCode) {
        this.street = street;
        this.zipCode = zipCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        if (street != null ? !street.equals(address.street) : address.street != null) return false;
        return zipCode != null ? zipCode.equals(address.zipCode) : address.zipCode == null;
    }

    @Override
    public int hashCode() {
        int result = street != null ? street.hashCode() : 0;
        result = 31 * result + (zipCode != null ? zipCode.hashCode() : 0);
        return result;
    }
}

