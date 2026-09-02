package com.ams.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class MinimumAgeValidator implements ConstraintValidator<MinimumAge, LocalDate> {

    private int minimumAge;
    private int maximumAge;

    @Override
    public void initialize(MinimumAge constraintAnnotation) {
        this.minimumAge = constraintAnnotation.value();
        this.maximumAge = constraintAnnotation.maxValue();
    }

    @Override
    public boolean isValid(LocalDate dob, ConstraintValidatorContext context) {
        // @NotNull handles the null case separately; a null value here is considered valid
        // so the two annotations don't produce duplicate/confusing error messages.
        if (dob == null) {
            return true;
        }
        if (dob.isAfter(LocalDate.now())) {
            return false;
        }
        int age = Period.between(dob, LocalDate.now()).getYears();
        return age >= minimumAge && age <= maximumAge;
    }
}
