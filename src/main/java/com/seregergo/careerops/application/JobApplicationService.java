package com.seregergo.careerops.application;

import com.seregergo.careerops.common.DatabaseConstraint;
import com.seregergo.careerops.common.TextNormalizer;
import com.seregergo.careerops.jobposting.JobPosting;
import com.seregergo.careerops.jobposting.JobPostingNotFoundException;
import com.seregergo.careerops.jobposting.JobPostingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class JobApplicationService {

	private static final String UNIQUE_POSTING_CONSTRAINT = "uk_job_applications_posting";
	private static final String UNIQUE_EVENT_TARGET_CONSTRAINT =
			"uk_application_status_events_target";

	private final JobApplicationRepository applicationRepository;
	private final ApplicationStatusEventRepository eventRepository;
	private final JobPostingRepository jobPostingRepository;

	public JobApplicationService(
			JobApplicationRepository applicationRepository,
			ApplicationStatusEventRepository eventRepository,
			JobPostingRepository jobPostingRepository
	) {
		this.applicationRepository = applicationRepository;
		this.eventRepository = eventRepository;
		this.jobPostingRepository = jobPostingRepository;
	}

	@Transactional
	public JobApplicationResponse create(JobApplicationRequest request) {
		if (applicationRepository.existsByJobPostingId(request.jobPostingId())) {
			throw new DuplicateJobApplicationException(request.jobPostingId());
		}

		JobPosting jobPosting = jobPostingRepository.findById(request.jobPostingId())
				.orElseThrow(() -> new JobPostingNotFoundException(request.jobPostingId()));
		JobApplication application = new JobApplication(
				jobPosting,
				TextNormalizer.trimToNull(request.notes())
		);

		try {
			applicationRepository.saveAndFlush(application);
			eventRepository.saveAndFlush(new ApplicationStatusEvent(
					application,
					null,
					ApplicationStatus.SAVED,
					"Application added"
			));
		} catch (DataIntegrityViolationException exception) {
			if (DatabaseConstraint.causedBy(exception, UNIQUE_POSTING_CONSTRAINT)) {
				throw new DuplicateJobApplicationException(request.jobPostingId());
			}
			throw exception;
		}

		return JobApplicationResponse.from(application);
	}

	@Transactional(readOnly = true)
	public List<JobApplicationResponse> list(ApplicationStatus status) {
		List<JobApplication> applications = status == null
				? applicationRepository.findAllByOrderByUpdatedAtDesc()
				: applicationRepository.findAllByStatusOrderByUpdatedAtDesc(status);

		return applications.stream()
				.map(JobApplicationResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public JobApplicationResponse get(UUID id) {
		return JobApplicationResponse.from(findApplication(id));
	}

	@Transactional
	public JobApplicationResponse transition(UUID id, StatusTransitionRequest request) {
		JobApplication application = findApplication(id);
		ApplicationStatus currentStatus = application.transitionTo(request.targetStatus());
		try {
			applicationRepository.saveAndFlush(application);
			eventRepository.saveAndFlush(new ApplicationStatusEvent(
					application,
					currentStatus,
					request.targetStatus(),
					TextNormalizer.trimToNull(request.note())
			));
		} catch (ObjectOptimisticLockingFailureException exception) {
			throw new ApplicationStatusConflictException();
		} catch (DataIntegrityViolationException exception) {
			if (DatabaseConstraint.causedBy(exception, UNIQUE_EVENT_TARGET_CONSTRAINT)) {
				throw new ApplicationStatusConflictException();
			}
			throw exception;
		}
		return JobApplicationResponse.from(application);
	}

	@Transactional(readOnly = true)
	public List<ApplicationStatusEventResponse> history(UUID id) {
		findApplication(id);
		return eventRepository.findAllByApplicationIdOrderByOccurredAtAscIdAsc(id).stream()
				.map(ApplicationStatusEventResponse::from)
				.toList();
	}

	private JobApplication findApplication(UUID id) {
		return applicationRepository.findById(id)
				.orElseThrow(() -> new JobApplicationNotFoundException(id));
	}
}
