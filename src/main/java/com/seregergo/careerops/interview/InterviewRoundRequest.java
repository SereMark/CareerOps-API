package com.seregergo.careerops.interview;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record InterviewRoundRequest(
		@NotNull(message = "Application is required")
		UUID applicationId,

		@NotNull(message = "Round type is required")
		InterviewRoundType roundType,

		@NotNull(message = "Scheduled time is required")
		Instant scheduledAt,

		@NotNull(message = "Interview format is required")
		InterviewFormat format,

		InterviewOutcome outcome,

		@Size(max = 200, message = "Contact name must not exceed 200 characters")
		String contactName,

		@Size(max = 5000, message = "Prep notes must not exceed 5000 characters")
		String prepNotes,

		@Size(max = 5000, message = "Questions asked must not exceed 5000 characters")
		String questionsAsked,

		Instant followUpSentAt
) {
}
