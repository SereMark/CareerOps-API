package com.seregergo.careerops.interview;

import com.seregergo.careerops.application.JobApplication;

import java.time.Instant;
import java.util.UUID;

public record InterviewRoundResponse(
		UUID id,
		UUID applicationId,
		String jobTitle,
		String companyName,
		InterviewRoundType roundType,
		Instant scheduledAt,
		InterviewFormat format,
		InterviewOutcome outcome,
		String contactName,
		String prepNotes,
		String questionsAsked,
		Instant followUpSentAt,
		Instant createdAt,
		Instant updatedAt
) {

	static InterviewRoundResponse from(InterviewRound round) {
		JobApplication application = round.getApplication();
		return new InterviewRoundResponse(
				round.getId(),
				application.getId(),
				application.getJobPosting().getTitle(),
				application.getJobPosting().getCompany().getName(),
				round.getRoundType(),
				round.getScheduledAt(),
				round.getFormat(),
				round.getOutcome(),
				round.getContactName(),
				round.getPrepNotes(),
				round.getQuestionsAsked(),
				round.getFollowUpSentAt(),
				round.getCreatedAt(),
				round.getUpdatedAt()
		);
	}
}
