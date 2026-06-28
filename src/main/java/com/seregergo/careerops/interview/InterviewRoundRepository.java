package com.seregergo.careerops.interview;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterviewRoundRepository extends JpaRepository<InterviewRound, UUID> {

	@Override
	@EntityGraph(attributePaths = {"application", "application.jobPosting", "application.jobPosting.company"})
	Optional<InterviewRound> findById(UUID id);

	@EntityGraph(attributePaths = {"application", "application.jobPosting", "application.jobPosting.company"})
	List<InterviewRound> findAllByApplicationIdOrderByScheduledAtAsc(UUID applicationId);

	@EntityGraph(attributePaths = {"application", "application.jobPosting", "application.jobPosting.company"})
	List<InterviewRound> findAllByOrderByScheduledAtAsc();

	long countByScheduledAtBetweenAndOutcome(
			Instant startInclusive,
			Instant endExclusive,
			InterviewOutcome outcome
	);
}
