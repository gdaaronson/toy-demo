package com.toydemo.transaction.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.toydemo.transaction.TransactionUtils;

import java.math.BigDecimal;

public class AmountInCentsValidator implements ConstraintValidator<AmountInCents, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return TransactionUtils.roundToCents(value).compareTo(BigDecimal.ZERO) > 0;
    }
}
