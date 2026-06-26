package com.seregergo.careerops.jobposting;

import com.seregergo.careerops.validation.HttpUrl;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

		TargetLane targetLane,

		Seniority seniority,

		@PositiveOrZero(message = "Minimum salary must not be negative")
		Integer salaryMinGrossHuf,

		@PositiveOrZero(message = "Maximum salary must not be negative")
		Integer salaryMaxGrossHuf,

		@Min(value = 0, message = "Role fit score must be at least 0")
		@Max(value = 30, message = "Role fit score must not exceed 30")
		Integer roleFitScore,

		@Min(value = 0, message = "Mentoring score must be at least 0")
		@Max(value = 25, message = "Mentoring score must not exceed 25")
		Integer mentoringScore,

		@Min(value = 0, message = "Salary score must be at least 0")
		@Max(value = 20, message = "Salary score must not exceed 20")
		Integer salaryScore,

		@Min(value = 0, message = "Engineering practices score must be at least 0")
		@Max(value = 10, message = "Engineering practices score must not exceed 10")
		Integer engineeringPracticesScore,

		@Min(value = 0, message = "Learning signal score must be at least 0")
		@Max(value = 10, message = "Learning signal score must not exceed 10")
		Integer learningSignalScore,

		@Min(value = 0, message = "Hybrid fit score must be at least 0")
		@Max(value = 5, message = "Hybrid fit score must not exceed 5")
		Integer hybridFitScore,

		@Size(max = 500, message = "Hard veto reason must not exceed 500 characters")
		String hardVetoReason,

		@Size(max = 5000, message = "Notes must not exceed 5000 characters")
		String notes
) {

	@AssertTrue(message = "Minimum salary must not be greater than maximum salary")
	public boolean isSalaryRangeValid() {
		return salaryMinGrossHuf == null
				|| salaryMaxGrossHuf == null
				|| salaryMinGrossHuf <= salaryMaxGrossHuf;
	}
}
