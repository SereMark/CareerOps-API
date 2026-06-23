package com.seregergo.careerops.jobposting;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobPostingRepository extends JpaRepository<JobPosting, UUID> {

	@Override
	@EntityGraph(attributePaths = "company")
	Optional<JobPosting> findById(UUID id);

	@EntityGraph(attributePaths = "company")
	List<JobPosting> findAllByOrderByCreatedAtDesc();

	@EntityGraph(attributePaths = "company")
	List<JobPosting> findAllByCompanyIdOrderByCreatedAtDesc(UUID companyId);
}
