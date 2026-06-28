package com.seregergo.careerops.offer;

import com.seregergo.careerops.application.JobApplication;
import com.seregergo.careerops.application.JobApplicationNotFoundException;
import com.seregergo.careerops.application.JobApplicationRepository;
import com.seregergo.careerops.common.DatabaseConstraint;
import com.seregergo.careerops.common.TextNormalizer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OfferService {

	private static final String UNIQUE_APPLICATION_CONSTRAINT = "uk_offers_application";

	private final OfferRepository offerRepository;
	private final JobApplicationRepository applicationRepository;

	public OfferService(
			OfferRepository offerRepository,
			JobApplicationRepository applicationRepository
	) {
		this.offerRepository = offerRepository;
		this.applicationRepository = applicationRepository;
	}

	@Transactional
	public OfferResponse create(OfferRequest request) {
		if (offerRepository.existsByApplicationId(request.applicationId())) {
			throw new DuplicateOfferException(request.applicationId());
		}
		Offer offer = createOffer(findApplication(request.applicationId()), request);
		try {
			return OfferResponse.from(offerRepository.saveAndFlush(offer));
		} catch (DataIntegrityViolationException exception) {
			if (DatabaseConstraint.causedBy(exception, UNIQUE_APPLICATION_CONSTRAINT)) {
				throw new DuplicateOfferException(request.applicationId());
			}
			throw exception;
		}
	}

	@Transactional(readOnly = true)
	public List<OfferResponse> list(UUID applicationId) {
		if (applicationId != null) {
			Optional<Offer> offer = offerRepository.findByApplicationId(applicationId);
			return offer.stream().map(OfferResponse::from).toList();
		}
		return offerRepository.findAllByOrderByUpdatedAtDesc().stream()
				.map(OfferResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public OfferResponse get(UUID id) {
		return OfferResponse.from(findOffer(id));
	}

	@Transactional
	public OfferResponse update(UUID id, OfferRequest request) {
		Offer offer = findOffer(id);
		offer.replaceDetails(
				findApplication(request.applicationId()),
				request.grossMonthlyHuf(),
				TextNormalizer.trimToNull(request.benefits()),
				TextNormalizer.trimToNull(request.hybridPolicy()),
				TextNormalizer.trimToNull(request.reviewPromise()),
				request.expiresAt(),
				decisionOrPending(request.decision()),
				TextNormalizer.trimToNull(request.decisionNotes())
		);
		try {
			return OfferResponse.from(offerRepository.saveAndFlush(offer));
		} catch (DataIntegrityViolationException exception) {
			if (DatabaseConstraint.causedBy(exception, UNIQUE_APPLICATION_CONSTRAINT)) {
				throw new DuplicateOfferException(request.applicationId());
			}
			throw exception;
		}
	}

	private Offer createOffer(JobApplication application, OfferRequest request) {
		return new Offer(
				application,
				request.grossMonthlyHuf(),
				TextNormalizer.trimToNull(request.benefits()),
				TextNormalizer.trimToNull(request.hybridPolicy()),
				TextNormalizer.trimToNull(request.reviewPromise()),
				request.expiresAt(),
				decisionOrPending(request.decision()),
				TextNormalizer.trimToNull(request.decisionNotes())
		);
	}

	private JobApplication findApplication(UUID id) {
		return applicationRepository.findById(id)
				.orElseThrow(() -> new JobApplicationNotFoundException(id));
	}

	private Offer findOffer(UUID id) {
		return offerRepository.findById(id)
				.orElseThrow(() -> new OfferNotFoundException(id));
	}

	private static OfferDecision decisionOrPending(OfferDecision decision) {
		return decision == null ? OfferDecision.PENDING : decision;
	}
}
