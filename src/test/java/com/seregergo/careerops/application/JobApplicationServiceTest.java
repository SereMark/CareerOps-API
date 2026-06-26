package com.seregergo.careerops.application;

import com.seregergo.careerops.company.Company;
import com.seregergo.careerops.jobposting.JobPosting;
import com.seregergo.careerops.jobposting.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

	@Mock
	private JobApplicationRepository applicationRepository;

	@Mock
	private ApplicationStatusEventRepository eventRepository;

	@Mock
	private JobPostingRepository jobPostingRepository;

	@Mock
	private JobPosting jobPosting;

	@Mock
	private Company company;

	private JobApplicationService applicationService;

	@BeforeEach
	void setUp() {
		applicationService = new JobApplicationService(
				applicationRepository,
				eventRepository,
				jobPostingRepository
		);
	}

	@Test
	void createStartsSavedAndRecordsTheInitialEvent() {
		UUID postingId = UUID.randomUUID();
		when(applicationRepository.existsByJobPostingId(postingId)).thenReturn(false);
		when(jobPostingRepository.findById(postingId)).thenReturn(Optional.of(jobPosting));
		stubPostingDetails(postingId);
		when(applicationRepository.saveAndFlush(any(JobApplication.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(eventRepository.saveAndFlush(any(ApplicationStatusEvent.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		JobApplicationResponse response = applicationService.create(
				new JobApplicationRequest(postingId, "  ")
		);

		assertThat(response.status()).isEqualTo(ApplicationStatus.SAVED);
		assertThat(response.notes()).isNull();

		ArgumentCaptor<ApplicationStatusEvent> eventCaptor =
				ArgumentCaptor.forClass(ApplicationStatusEvent.class);
		verify(eventRepository).saveAndFlush(eventCaptor.capture());
		assertThat(eventCaptor.getValue().getPreviousStatus()).isNull();
		assertThat(eventCaptor.getValue().getNewStatus()).isEqualTo(ApplicationStatus.SAVED);
	}

	@Test
	void createRejectsASecondApplicationForTheSamePosting() {
		UUID postingId = UUID.randomUUID();
		when(applicationRepository.existsByJobPostingId(postingId)).thenReturn(true);

		assertThatThrownBy(() -> applicationService.create(
				new JobApplicationRequest(postingId, null)
		)).isInstanceOf(DuplicateJobApplicationException.class);

		verify(jobPostingRepository, never()).findById(any());
	}

	@Test
	void transitionChangesStatusAndRecordsContext() {
		UUID applicationId = UUID.randomUUID();
		JobApplication application = new JobApplication(jobPosting, null);
		stubPostingDetails(UUID.randomUUID());
		when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
		when(applicationRepository.saveAndFlush(application)).thenReturn(application);
		when(eventRepository.saveAndFlush(any(ApplicationStatusEvent.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		applicationService.transition(
				applicationId,
				new StatusTransitionRequest(ApplicationStatus.APPLIED, "  Sent through careers page  ")
		);

		assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
		ArgumentCaptor<ApplicationStatusEvent> eventCaptor =
				ArgumentCaptor.forClass(ApplicationStatusEvent.class);
		verify(eventRepository).saveAndFlush(eventCaptor.capture());
		assertThat(eventCaptor.getValue().getPreviousStatus()).isEqualTo(ApplicationStatus.SAVED);
		assertThat(eventCaptor.getValue().getNewStatus()).isEqualTo(ApplicationStatus.APPLIED);
		assertThat(eventCaptor.getValue().getNote()).isEqualTo("Sent through careers page");
	}

	@Test
	void transitionRejectsAnInvalidJumpWithoutWriting() {
		UUID applicationId = UUID.randomUUID();
		JobApplication application = new JobApplication(jobPosting, null);
		when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

		assertThatThrownBy(() -> applicationService.transition(
				applicationId,
				new StatusTransitionRequest(ApplicationStatus.OFFER, null)
		)).isInstanceOf(InvalidApplicationStatusTransitionException.class);

		verify(applicationRepository, never()).saveAndFlush(application);
		verify(eventRepository, never()).saveAndFlush(any());
	}

	@Test
	void transitionMapsAStaleUpdateToADomainConflict() {
		UUID applicationId = UUID.randomUUID();
		JobApplication application = new JobApplication(jobPosting, null);
		when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
		when(applicationRepository.saveAndFlush(application))
				.thenThrow(new ObjectOptimisticLockingFailureException(
						JobApplication.class,
						applicationId
				));

		assertThatThrownBy(() -> applicationService.transition(
				applicationId,
				new StatusTransitionRequest(ApplicationStatus.APPLIED, null)
		)).isInstanceOf(ApplicationStatusConflictException.class);

		verify(eventRepository, never()).saveAndFlush(any());
	}

	private void stubPostingDetails(UUID postingId) {
		when(jobPosting.getId()).thenReturn(postingId);
		when(jobPosting.getTitle()).thenReturn("Java Developer");
		when(jobPosting.getCompany()).thenReturn(company);
		when(company.getId()).thenReturn(UUID.randomUUID());
		when(company.getName()).thenReturn("Acme");
	}
}
