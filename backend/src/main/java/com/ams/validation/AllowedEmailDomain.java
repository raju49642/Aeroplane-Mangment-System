package com.ams.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllowedEmailDomainValidator.class)
public @interface AllowedEmailDomain {
    String message() default "Email domain is not allowed";
    String domains() default "gmail.com,yahoo.com,outlook.com,hotmail.com,icloud.com,protonmail.com";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
