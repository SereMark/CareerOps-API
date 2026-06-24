package com.seregergo.careerops.application;

import com.seregergo.careerops.common.DatabaseConstraint;
import com.seregergo.careerops.common.TextNormalizer;
import com.seregergo.careerops.jobposting.JobPosting;
import com.seregergo.careerops.jobposting.JobPostingNotFoundException;
import com.seregergo.careerops.jobposting.JobPostingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class JobApplicationService {

	private static final String UNIQUE_POSTING_CONSTRAINT = "uk_job_applications_posting";
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



	private JobApplication findApplication(UUID id) {
		return applicationRepository.findById(id)
				.orElseThrow(() -> new JobApplicationNotFoundException(id));
	}
}
