package com.seregergo.careerops.jobposting;

import java.util.UUID;

public class JobPostingNotFoundException extends RuntimeException {

	public JobPostingNotFoundException(UUID id) {
		super("Job posting with ID " + id + " was not found");
	}
}
