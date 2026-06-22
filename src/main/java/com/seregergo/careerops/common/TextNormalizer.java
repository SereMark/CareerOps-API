package com.seregergo.careerops.common;

public final class TextNormalizer {

	private TextNormalizer() {
	}

	public static String trimToNull(String value) {
		if (value == null) {
			return null;
		}

		String trimmed = value.strip();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
