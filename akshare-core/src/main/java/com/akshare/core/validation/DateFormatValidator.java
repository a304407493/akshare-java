package com.akshare.core.validation;

import com.akshare.core.util.DateUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for DateFormat annotation
 */
public class DateFormatValidator implements ConstraintValidator<DateFormat, String> {

    private String pattern;

    @Override
    public void initialize(DateFormat constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // Null values are handled by @NotNull
        }
        return DateUtils.isValidFormat(value, pattern);
    }
}
