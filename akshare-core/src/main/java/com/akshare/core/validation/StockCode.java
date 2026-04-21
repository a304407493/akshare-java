package com.akshare.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotation for validating stock code format
 */
@Documented
@Constraint(validatedBy = StockCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StockCode {

    String message() default "Invalid stock code format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
