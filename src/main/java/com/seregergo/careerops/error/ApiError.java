package com.seregergo.careerops.error;

import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.Locale;

enum ApiError {

	COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "Company not found"),
	COMPANY_NAME_CONFLICT(HttpStatus.CONFLICT, "Company name conflict"),
	JOB_POSTING_NOT_FOUND(HttpStatus.NOT_FOUND, "Job posting not found"),
	JOB_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Job application not found"),
	JOB_APPLICATION_CONFLICT(HttpStatus.CONFLICT, "Job application conflict"),
	APPLICATION_STATUS_CONFLICT(HttpStatus.CONFLICT, "Application status conflict"),
	INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "Invalid application status transition"),
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Request validation failed"),
	INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "Invalid parameter"),
	MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "Malformed request");

	private static final String TYPE_PREFIX = "urn:careerops:problem:";

	private final HttpStatus status;
	private final String title;
	private final URI type;

	ApiError(HttpStatus status, String title) {
		this.status = status;
		this.title = title;
		this.type = URI.create(TYPE_PREFIX + name().toLowerCase(Locale.ROOT).replace('_', '-'));
	}

	HttpStatus status() {
		return status;
	}

	String title() {
		return title;
	}

	URI type() {
		return type;
	}
}
