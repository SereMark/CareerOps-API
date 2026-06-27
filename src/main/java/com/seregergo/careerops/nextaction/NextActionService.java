package com.seregergo.careerops.nextaction;

import com.seregergo.careerops.application.JobApplication;
import com.seregergo.careerops.application.JobApplicationNotFoundException;
import com.seregergo.careerops.application.JobApplicationRepository;
import com.seregergo.careerops.common.TextNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class NextActionService {

	private final NextActionRepository nextActionRepository;
	private final JobApplicationRepository applicationRepository;
	private final Clock clock;

	public NextActionService(
			NextActionRepository nextActionRepository,
			JobApplicationRepository applicationRepository,
			Clock clock
	) {
		this.nextActionRepository = nextActionRepository;
		this.applicationRepository = applicationRepository;
		this.clock = clock;
	}

	@Transactional
	public NextActionResponse create(NextActionRequest request) {
		JobApplication application = findApplication(request.applicationId());
		NextAction action = new NextAction(
				application,
				request.type(),
				request.dueDate(),
				TextNormalizer.trimToNull(request.notes())
		);
		return NextActionResponse.from(nextActionRepository.saveAndFlush(action));
	}

	@Transactional(readOnly = true)
	public List<NextActionResponse> list(UUID applicationId, Boolean completed, LocalDate dueBefore) {
		List<NextAction> actions;
		if (applicationId != null) {
			actions = nextActionRepository.findAllByApplicationIdOrderByDueDateAsc(applicationId);
		} else if (Boolean.TRUE.equals(completed)) {
			actions = nextActionRepository.findAllByCompletedAtIsNotNullOrderByDueDateDesc();
		} else if (dueBefore != null) {
			actions = nextActionRepository
					.findAllByCompletedAtIsNullAndDueDateLessThanEqualOrderByDueDateAsc(dueBefore);
		} else {
			actions = nextActionRepository.findAllByCompletedAtIsNullOrderByDueDateAsc();
		}
		return actions.stream().map(NextActionResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public NextActionResponse get(UUID id) {
		return NextActionResponse.from(findAction(id));
	}

	@Transactional
	public NextActionResponse update(UUID id, NextActionRequest request) {
		NextAction action = findAction(id);
		action.replaceDetails(
				findApplication(request.applicationId()),
				request.type(),
				request.dueDate(),
				TextNormalizer.trimToNull(request.notes())
		);
		return NextActionResponse.from(nextActionRepository.saveAndFlush(action));
	}

	@Transactional
	public NextActionResponse complete(UUID id) {
		NextAction action = findAction(id);
		action.complete(clock.instant());
		return NextActionResponse.from(nextActionRepository.saveAndFlush(action));
	}

	private JobApplication findApplication(UUID id) {
		return applicationRepository.findById(id)
				.orElseThrow(() -> new JobApplicationNotFoundException(id));
	}

	private NextAction findAction(UUID id) {
		return nextActionRepository.findById(id)
				.orElseThrow(() -> new NextActionNotFoundException(id));
	}
}
