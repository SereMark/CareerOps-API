package com.seregergo.careerops.jobposting;

import com.seregergo.careerops.company.Company;
import com.seregergo.careerops.company.CompanyNotFoundException;
import com.seregergo.careerops.company.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobPostingServiceTest {

	@Mock
	private JobPostingRepository jobPostingRepository;

	@Mock
	private CompanyRepository companyRepository;

	@Mock
	private Company company;

	private JobPostingService jobPostingService;

	@BeforeEach
	void setUp() {
		jobPostingService = new JobPostingService(jobPostingRepository, companyRepository);
	}

	@Test
	void createNormalizesOptionalText() {
		UUID companyId = UUID.randomUUID();
		when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
		when(company.getId()).thenReturn(companyId);
		when(company.getName()).thenReturn("Acme");
		when(jobPostingRepository.saveAndFlush(any(JobPosting.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		JobPostingResponse response = jobPostingService.create(new JobPostingRequest(
				companyId,
				"  Java Developer  ",
				" https://jobs.example/1 ",
				"  Budapest  ",
				WorkMode.HYBRID,
				TargetLane.JAVA_BACKEND,
				Seniority.JUNIOR,
				910000,
				1120000,
				28,
				20,
				18,
				8,
				8,
				5,
				"  ",
				"  "
		));

		assertThat(response.title()).isEqualTo("Java Developer");
		assertThat(response.sourceUrl()).isEqualTo("https://jobs.example/1");
		assertThat(response.location()).isEqualTo("Budapest");
		assertThat(response.triageScore()).isEqualTo(87);
		assertThat(response.triagePriority()).isEqualTo(TriagePriority.PRIORITIZE);
		assertThat(response.hardVetoReason()).isNull();
		assertThat(response.notes()).isNull();
	}

	@Test
	void createRejectsUnknownCompany() {
		UUID companyId = UUID.randomUUID();
		when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> jobPostingService.create(new JobPostingRequest(
				companyId,
				"Java Developer",
				null,
				null,
				WorkMode.HYBRID,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null
		))).isInstanceOf(CompanyNotFoundException.class);
	}
}
