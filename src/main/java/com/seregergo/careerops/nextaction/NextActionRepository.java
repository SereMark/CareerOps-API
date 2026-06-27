package com.seregergo.careerops.nextaction;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NextActionRepository extends JpaRepository<NextAction, UUID> {

	@Override
	@EntityGraph(attributePaths = {"application", "application.jobPosting", "application.jobPosting.company"})
	Optional<NextAction> findById(UUID id);

	@EntityGraph(attributePaths = {"application", "application.jobPosting", "application.jobPosting.company"})
	List<NextAction> findAllByApplicationIdOrderByDueDateAsc(UUID applicationId);

	@EntityGraph(attributePaths = {"application", "application.jobPosting", "application.jobPosting.company"})
	List<NextAction> findAllByCompletedAtIsNullOrderByDueDateAsc();

	@EntityGraph(attributePaths = {"application", "application.jobPosting", "application.jobPosting.company"})
	List<NextAction> findAllByCompletedAtIsNotNullOrderByDueDateDesc();

	@EntityGraph(attributePaths = {"application", "application.jobPosting", "application.jobPosting.company"})
	List<NextAction> findAllByCompletedAtIsNullAndDueDateLessThanEqualOrderByDueDateAsc(
			LocalDate dueDate
	);

	long countByCompletedAtIsNull();

	long countByCompletedAtIsNullAndDueDateBefore(LocalDate today);
}
