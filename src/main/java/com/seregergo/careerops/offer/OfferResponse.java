package com.seregergo.careerops.offer;

import com.seregergo.careerops.application.JobApplication;

import java.time.Instant;
import java.util.UUID;

public record OfferResponse(
		UUID id,
		UUID applicationId,
		String jobTitle,
		String companyName,
		int grossMonthlyHuf,
		String benefits,
		String hybridPolicy,
		String reviewPromise,
		Instant expiresAt,
		OfferDecision decision,
		String decisionNotes,
		Instant createdAt,
		Instant updatedAt
) {

	static OfferResponse from(Offer offer) {
		JobApplication application = offer.getApplication();
		return new OfferResponse(
				offer.getId(),
				application.getId(),
				application.getJobPosting().getTitle(),
				application.getJobPosting().getCompany().getName(),
				offer.getGrossMonthlyHuf(),
				offer.getBenefits(),
				offer.getHybridPolicy(),
				offer.getReviewPromise(),
				offer.getExpiresAt(),
				offer.getDecision(),
				offer.getDecisionNotes(),
				offer.getCreatedAt(),
				offer.getUpdatedAt()
		);
	}
}
