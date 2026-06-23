package com.seregergo.careerops.jobposting;

import java.time.Instant;
import java.util.UUID;

public record JobPostingResponse(
		UUID id,
		UUID companyId,
		String companyName,
		String title,
		String sourceUrl,
		String location,
		WorkMode workMode,
		String notes,
		Instant createdAt,
		Instant updatedAt
) {

	static JobPostingResponse from(JobPosting jobPosting) {
		return new JobPostingResponse(
				jobPosting.getId(),
				jobPosting.getCompany().getId(),
				jobPosting.getCompany().getName(),
				jobPosting.getTitle(),
				jobPosting.getSourceUrl(),
				jobPosting.getLocation(),
				jobPosting.getWorkMode(),
				jobPosting.getNotes(),
				jobPosting.getCreatedAt(),
				jobPosting.getUpdatedAt()
		);
	}
}
