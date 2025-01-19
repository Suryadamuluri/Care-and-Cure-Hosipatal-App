package com.hms.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LocalTimeValidator.class)
public @interface ValidLocalTime {
    String message() default "Start Time should be between 9.00 a.m - 18.00 p.m";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
