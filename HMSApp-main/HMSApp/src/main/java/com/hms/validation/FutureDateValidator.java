package com.hms.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class FutureDateValidator implements ConstraintValidator<ValidFutureDate, LocalDate> {

    @Override
    public void initialize(ValidFutureDate constraintAnnotation) {
    }

    @Override
    public boolean isValid(LocalDate appointmentDate, ConstraintValidatorContext context) {
        if (appointmentDate == null) {
            return true;
        }
        LocalDate today = LocalDate.now();
        LocalDate oneMonthFromToday = today.plusMonths(1);
        return !appointmentDate.isAfter(oneMonthFromToday);
    }
}
