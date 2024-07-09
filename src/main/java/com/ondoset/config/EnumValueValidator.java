package com.ondoset.config;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValueValidator implements ConstraintValidator<Enum, String> {
	private Enum annotation;

	@Override
	public void initialize(Enum annotation)
	{
		this.annotation = annotation;
	}

	@Override
	public boolean isValid(String valueForValidation, ConstraintValidatorContext constraintValidatorContext) {

		boolean result = false;

		if (this.annotation.nullable() && valueForValidation == null) {
			return true;
		}

		Object[] enumValues = this.annotation.enumClass().getEnumConstants();

		if (valueForValidation != null && enumValues != null) {
			for (Object enumValue : enumValues) {
				if (valueForValidation.equalsIgnoreCase(enumValue.toString())) {
					result = true;
					break;
				}
			}
		}

		return result;
	}
}
