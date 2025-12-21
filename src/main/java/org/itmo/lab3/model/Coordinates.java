package org.itmo.lab3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "coordinates")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Coordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "X не может быть null")
    @Column(name = "x", nullable = false)
    private Integer x;

    @NotNull(message = "Y не может быть null")
    @Max(value = 323, message = "Максимальное значение поля: 323")
    @Column(name = "y", nullable = false)
    private Float y;

    public Coordinates() {
    }

    public Coordinates(Integer x, Float y) {
        this.x = x;
        this.y = y;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

