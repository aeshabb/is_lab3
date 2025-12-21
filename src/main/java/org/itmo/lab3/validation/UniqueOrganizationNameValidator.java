package org.itmo.lab3.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueOrganizationNameValidator implements ConstraintValidator<UniqueOrganizationName, String> {

    @Override
    public void initialize(UniqueOrganizationName constraintAnnotation) {
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        return true;
    }
}
