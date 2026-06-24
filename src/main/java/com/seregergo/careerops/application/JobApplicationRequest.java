package com.seregergo.careerops.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record JobApplicationRequest(
		@NotNull(message = "Job posting is required")
		UUID jobPostingId,

		@Size(max = 5000, message = "Notes must not exceed 5000 characters")
		String notes
) {
}
