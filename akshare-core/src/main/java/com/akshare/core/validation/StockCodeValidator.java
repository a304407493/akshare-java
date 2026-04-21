package com.akshare.core.validation;

import com.akshare.core.util.StockCodeUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for StockCode annotation
 */
public class StockCodeValidator implements ConstraintValidator<StockCode, String> {

    @Override
    public void initialize(StockCode constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // Null values are handled by @NotNull
        }
        return StockCodeUtils.isValidStockCode(value);
    }
}
