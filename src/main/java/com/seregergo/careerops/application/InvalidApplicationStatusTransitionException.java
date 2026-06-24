package com.seregergo.careerops.application;

public class InvalidApplicationStatusTransitionException extends RuntimeException {

	public InvalidApplicationStatusTransitionException(
			ApplicationStatus currentStatus,
			ApplicationStatus targetStatus
	) {
		super("Application status cannot move from " + currentStatus + " to " + targetStatus);
	}
}
