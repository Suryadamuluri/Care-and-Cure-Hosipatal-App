package com.hms.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// This annotation will be used to validate the future date
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureDateValidator.class)  // No need to put in array
public @interface ValidFutureDate {

    // Default message for validation failure
    String message() default "Appointment date cannot be more than 1 year in the future.";

    // Allow custom validation groups
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
