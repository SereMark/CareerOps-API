package com.seregergo.careerops.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.net.URISyntaxException;

public class HttpUrlValidator implements ConstraintValidator<HttpUrl, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true;
		}

		try {
			URI uri = new URI(value.strip());
			String scheme = uri.getScheme();
			return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
					&& uri.getHost() != null;
		} catch (URISyntaxException exception) {
			return false;
		}
	}
}
