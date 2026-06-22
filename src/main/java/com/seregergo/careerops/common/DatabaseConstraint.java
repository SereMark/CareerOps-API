package com.seregergo.careerops.common;

import org.hibernate.exception.ConstraintViolationException;

public final class DatabaseConstraint {

	private DatabaseConstraint() {
	}

	public static boolean causedBy(Throwable exception, String constraintName) {
		Throwable cause = exception;
		while (cause != null) {
			if (cause instanceof ConstraintViolationException violation
					&& constraintName.equals(violation.getConstraintName())) {
				return true;
			}
			cause = cause.getCause();
		}
		return false;
	}
}
