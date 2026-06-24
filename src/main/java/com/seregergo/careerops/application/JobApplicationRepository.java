package com.seregergo.careerops.application;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

	boolean existsByJobPostingId(UUID jobPostingId);

	@Override
	@EntityGraph(attributePaths = {"jobPosting", "jobPosting.company"})
	Optional<JobApplication> findById(UUID id);

	@EntityGraph(attributePaths = {"jobPosting", "jobPosting.company"})
	List<JobApplication> findAllByOrderByUpdatedAtDesc();

	@EntityGraph(attributePaths = {"jobPosting", "jobPosting.company"})
	List<JobApplication> findAllByStatusOrderByUpdatedAtDesc(ApplicationStatus status);
}
