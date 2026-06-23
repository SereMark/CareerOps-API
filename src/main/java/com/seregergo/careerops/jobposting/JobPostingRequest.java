package com.seregergo.careerops.jobposting;

import com.seregergo.careerops.validation.HttpUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record JobPostingRequest(
		@NotNull(message = "Company is required")
		UUID companyId,

		@NotBlank(message = "Title is required")
		@Size(max = 200, message = "Title must not exceed 200 characters")
		String title,

		@Size(max = 2048, message = "Source URL must not exceed 2048 characters")
		@HttpUrl(message = "Source URL must be a valid HTTP or HTTPS URL")
		String sourceUrl,

		@Size(max = 200, message = "Location must not exceed 200 characters")
		String location,

		@NotNull(message = "Work mode is required")
		WorkMode workMode,

		@Size(max = 5000, message = "Notes must not exceed 5000 characters")
		String notes
) {
}
