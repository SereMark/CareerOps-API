package com.seregergo.careerops.nextaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record NextActionRequest(
		@NotNull(message = "Application is required")
		UUID applicationId,

		@NotNull(message = "Action type is required")
		NextActionType type,

		@NotNull(message = "Due date is required")
		LocalDate dueDate,

		@Size(max = 2000, message = "Notes must not exceed 2000 characters")
		String notes
) {
}
