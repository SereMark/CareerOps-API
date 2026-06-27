package com.seregergo.careerops.nextaction;

import com.seregergo.careerops.application.JobApplication;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record NextActionResponse(
		UUID id,
		UUID applicationId,
		String jobTitle,
		String companyName,
		NextActionType type,
		LocalDate dueDate,
		Instant completedAt,
		String notes,
		Instant createdAt,
		Instant updatedAt
) {

	static NextActionResponse from(NextAction action) {
		JobApplication application = action.getApplication();
		return new NextActionResponse(
				action.getId(),
				application.getId(),
				application.getJobPosting().getTitle(),
				application.getJobPosting().getCompany().getName(),
				action.getType(),
				action.getDueDate(),
				action.getCompletedAt(),
				action.getNotes(),
				action.getCreatedAt(),
				action.getUpdatedAt()
		);
	}
}
