package com.seregergo.careerops.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusTransitionRequest(
		@NotNull(message = "Target status is required")
		ApplicationStatus targetStatus,

		@Size(max = 2000, message = "Transition note must not exceed 2000 characters")
		String note
) {
}
