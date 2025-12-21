package org.itmo.lab3.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class UniqueRatingValidator implements ConstraintValidator<UniqueRating, Double> {

    @Override
    public void initialize(UniqueRating constraintAnnotation) {
    }

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        return true;
    }
}
