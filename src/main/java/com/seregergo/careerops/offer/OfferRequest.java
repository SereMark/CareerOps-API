package com.seregergo.careerops.offer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record OfferRequest(
		@NotNull(message = "Application is required")
		UUID applicationId,

		@NotNull(message = "Gross monthly salary is required")
		@Min(value = 0, message = "Gross monthly salary must not be negative")
		Integer grossMonthlyHuf,

		@Size(max = 5000, message = "Benefits must not exceed 5000 characters")
		String benefits,

		@Size(max = 1000, message = "Hybrid policy must not exceed 1000 characters")
		String hybridPolicy,

		@Size(max = 1000, message = "Review promise must not exceed 1000 characters")
		String reviewPromise,

		Instant expiresAt,

		OfferDecision decision,

		@Size(max = 5000, message = "Decision notes must not exceed 5000 characters")
		String decisionNotes
) {
}
