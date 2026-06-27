package com.seregergo.careerops.nextaction;

import java.util.UUID;

public class NextActionNotFoundException extends RuntimeException {

	public NextActionNotFoundException(UUID id) {
		super("Next action not found: " + id);
	}
}
