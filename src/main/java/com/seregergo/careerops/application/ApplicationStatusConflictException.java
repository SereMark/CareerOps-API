package com.seregergo.careerops.application;

public class ApplicationStatusConflictException extends RuntimeException {

	public ApplicationStatusConflictException() {
		super("The application changed while this request was being processed. Reload it and try again.");
	}
}
