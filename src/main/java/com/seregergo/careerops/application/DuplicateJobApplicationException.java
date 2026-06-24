package com.seregergo.careerops.application;

import java.util.UUID;

public class DuplicateJobApplicationException extends RuntimeException {

	public DuplicateJobApplicationException(UUID jobPostingId) {
		super("An application already exists for job posting " + jobPostingId);
	}
}
