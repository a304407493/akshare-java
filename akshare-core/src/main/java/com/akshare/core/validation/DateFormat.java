package com.akshare.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotation for validating date format
 */
@Documented
@Constraint(validatedBy = DateFormatValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DateFormat {

    String message() default "Invalid date format";

    String pattern() default "yyyyMMdd";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
