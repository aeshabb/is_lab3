package org.itmo.lab3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class OrganizationImportDto {

    @NotNull(message = "Название не может быть null")
    @NotBlank(message = "Строка не может быть пустой")
    @org.itmo.lab3.validation.UniqueOrganizationName
    private String name;

    @NotNull(message = "Координаты не могут быть null")
    @Valid
    private CoordinatesDto coordinates;

    private AddressDto officialAddress;

    @NotNull(message = "Годовой оборот не может быть null")
    @Positive(message = "Значение поля должно быть больше 0")
    private Long annualTurnover;

    @Positive(message = "Значение поля должно быть больше 0")
    private Integer employeesCount;

    @NotNull(message = "Рейтинг не может быть null")
    @Positive(message = "Значение поля должно быть больше 0")
    @org.itmo.lab3.validation.UniqueRating
    private Double rating;

    @NotNull(message = "Тип организации не может быть null")
    private String type;

    @NotNull(message = "Почтовый адрес не может быть null")
    @Valid
    private AddressDto postalAddress;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CoordinatesDto getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(CoordinatesDto coordinates) {
        this.coordinates = coordinates;
    }

    public AddressDto getOfficialAddress() {
        return officialAddress;
    }

    public void setOfficialAddress(AddressDto officialAddress) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AddressDto getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(AddressDto postalAddress) {
        this.postalAddress = postalAddress;
    }

    // Nested DTOs
    public static class CoordinatesDto {
        @NotNull(message = "Координата X не может быть null")
        @Min(value = -331, message = "Значение поля должно быть больше -331")
        private Integer x;

        @NotNull(message = "Координата Y не может быть null")
        private Float y;

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }

        public Float getY() {
            return y;
        }

        public void setY(Float y) {
            this.y = y;
        }
    }

    public static class AddressDto {
        @NotNull(message = "Улица не может быть null")
        @Size(max = 180, message = "Длина строки не должна быть больше 180")
        private String street;

        @NotNull(message = "Почтовый индекс не может быть null")
        @Size(min = 7, message = "Длина строки должна быть не меньше 7")
        @JsonProperty("zipCode")
        private String zipCode;

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
    }
}
