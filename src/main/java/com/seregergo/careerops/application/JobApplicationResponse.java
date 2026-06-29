package com.seregergo.careerops.application;

import com.seregergo.careerops.jobposting.JobPosting;

import java.time.Instant;
import java.util.UUID;

public record JobApplicationResponse(
		UUID id,
		UUID jobPostingId,
		String jobTitle,
		UUID companyId,
		String companyName,
		ApplicationStatus status,
		String notes,
		Instant createdAt,
		Instant updatedAt
) {

	public static JobApplicationResponse from(JobApplication application) {
		JobPosting jobPosting = application.getJobPosting();
		return new JobApplicationResponse(
				application.getId(),
				jobPosting.getId(),
				jobPosting.getTitle(),
				jobPosting.getCompany().getId(),
				jobPosting.getCompany().getName(),
				application.getStatus(),
				application.getNotes(),
				application.getCreatedAt(),
				application.getUpdatedAt()
		);
	}
}
