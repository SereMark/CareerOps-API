package com.seregergo.careerops.offer;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {

	boolean existsByApplicationId(UUID applicationId);

	@Override
	@EntityGraph(attributePaths = {"application", "application.jobPosting", "application.jobPosting.company"})
	Optional<Offer> findById(UUID id);

	@EntityGraph(attributePaths = {"application", "application.jobPosting", "application.jobPosting.company"})
	Optional<Offer> findByApplicationId(UUID applicationId);

	@EntityGraph(attributePaths = {"application", "application.jobPosting", "application.jobPosting.company"})
	List<Offer> findAllByOrderByUpdatedAtDesc();

	long countByDecision(OfferDecision decision);
}
