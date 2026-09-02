package com.ams.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotPastDateValidator.class)
public @interface NotPastDate {
    String message() default "Date must not be in the past";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
