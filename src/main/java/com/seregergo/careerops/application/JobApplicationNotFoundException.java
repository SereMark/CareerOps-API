package com.seregergo.careerops.application;

import java.util.UUID;

public class JobApplicationNotFoundException extends RuntimeException {

	public JobApplicationNotFoundException(UUID id) {
		super("Job application with ID " + id + " was not found");
	}
}
