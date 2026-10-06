package com.aditya.solarmanagement.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MobileNumberValidatorTest {
	private final MobileNumberValidator validator = new MobileNumberValidator();

	@Test
	void acceptsInternationalAndFormattedNumbers() {
		assertTrue(validator.isValid("+14155550132", null));
		assertTrue(validator.isValid("+1 (415) 555-0132", null));
	}

	@Test
	void acceptsMissingOptionalNumber() {
		assertTrue(validator.isValid(null, null));
		assertTrue(validator.isValid("   ", null));
	}

	@Test
	void rejectsInvalidOrOutOfRangeNumbers() {
		assertFalse(validator.isValid("123-ABC-7890", null));
		assertFalse(validator.isValid("12345", null));
		assertFalse(validator.isValid("1234567890123456", null));
	}
}