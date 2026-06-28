package com.seregergo.careerops.interview;

import com.seregergo.careerops.application.JobApplication;
import com.seregergo.careerops.application.JobApplicationNotFoundException;
import com.seregergo.careerops.application.JobApplicationRepository;
import com.seregergo.careerops.common.TextNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InterviewRoundService {

	private final InterviewRoundRepository interviewRoundRepository;
	private final JobApplicationRepository applicationRepository;

	public InterviewRoundService(
			InterviewRoundRepository interviewRoundRepository,
			JobApplicationRepository applicationRepository
	) {
		this.interviewRoundRepository = interviewRoundRepository;
		this.applicationRepository = applicationRepository;
	}

	@Transactional
	public InterviewRoundResponse create(InterviewRoundRequest request) {
		InterviewRound round = createRound(findApplication(request.applicationId()), request);
		return InterviewRoundResponse.from(interviewRoundRepository.saveAndFlush(round));
	}

	@Transactional(readOnly = true)
	public List<InterviewRoundResponse> list(UUID applicationId) {
		List<InterviewRound> rounds = applicationId == null
				? interviewRoundRepository.findAllByOrderByScheduledAtAsc()
				: interviewRoundRepository.findAllByApplicationIdOrderByScheduledAtAsc(applicationId);
		return rounds.stream().map(InterviewRoundResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public InterviewRoundResponse get(UUID id) {
		return InterviewRoundResponse.from(findRound(id));
	}

	@Transactional
	public InterviewRoundResponse update(UUID id, InterviewRoundRequest request) {
		InterviewRound round = findRound(id);
		round.replaceDetails(
				findApplication(request.applicationId()),
				request.roundType(),
				request.scheduledAt(),
				request.format(),
				outcomeOrScheduled(request.outcome()),
				TextNormalizer.trimToNull(request.contactName()),
				TextNormalizer.trimToNull(request.prepNotes()),
				TextNormalizer.trimToNull(request.questionsAsked()),
				request.followUpSentAt()
		);
		return InterviewRoundResponse.from(interviewRoundRepository.saveAndFlush(round));
	}

	private InterviewRound createRound(JobApplication application, InterviewRoundRequest request) {
		return new InterviewRound(
				application,
				request.roundType(),
				request.scheduledAt(),
				request.format(),
				outcomeOrScheduled(request.outcome()),
				TextNormalizer.trimToNull(request.contactName()),
				TextNormalizer.trimToNull(request.prepNotes()),
				TextNormalizer.trimToNull(request.questionsAsked()),
				request.followUpSentAt()
		);
	}

	private JobApplication findApplication(UUID id) {
		return applicationRepository.findById(id)
				.orElseThrow(() -> new JobApplicationNotFoundException(id));
	}

	private InterviewRound findRound(UUID id) {
		return interviewRoundRepository.findById(id)
				.orElseThrow(() -> new InterviewRoundNotFoundException(id));
	}

	private static InterviewOutcome outcomeOrScheduled(InterviewOutcome outcome) {
		return outcome == null ? InterviewOutcome.SCHEDULED : outcome;
	}
}
