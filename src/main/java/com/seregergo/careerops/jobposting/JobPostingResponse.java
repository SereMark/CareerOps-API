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
		TargetLane targetLane,
		Seniority seniority,
		Integer salaryMinGrossHuf,
		Integer salaryMaxGrossHuf,
		int roleFitScore,
		int mentoringScore,
		int salaryScore,
		int engineeringPracticesScore,
		int learningSignalScore,
		int hybridFitScore,
		int triageScore,
		TriagePriority triagePriority,
		String hardVetoReason,
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
				jobPosting.getTargetLane(),
				jobPosting.getSeniority(),
				jobPosting.getSalaryMinGrossHuf(),
				jobPosting.getSalaryMaxGrossHuf(),
				jobPosting.getRoleFitScore(),
				jobPosting.getMentoringScore(),
				jobPosting.getSalaryScore(),
				jobPosting.getEngineeringPracticesScore(),
				jobPosting.getLearningSignalScore(),
				jobPosting.getHybridFitScore(),
				jobPosting.getTriageScore(),
				jobPosting.getTriagePriority(),
				jobPosting.getHardVetoReason(),
				jobPosting.getNotes(),
				jobPosting.getCreatedAt(),
				jobPosting.getUpdatedAt()
		);
	}
}
