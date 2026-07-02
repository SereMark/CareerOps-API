package com.seregergo.careerops.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seregergo.careerops.company.CompanyRequest;
import com.seregergo.careerops.company.CompanyResponse;
import com.seregergo.careerops.company.CompanyService;
import com.seregergo.careerops.jobposting.JobPostingRequest;
import com.seregergo.careerops.jobposting.JobPostingResponse;
import com.seregergo.careerops.jobposting.JobPostingService;
import com.seregergo.careerops.jobposting.WorkMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "logging.level.org.hibernate.engine.jdbc.spi.SqlExceptionHelper=OFF")
@AutoConfigureMockMvc
@Testcontainers
class CareerOpsPostgreSqlIT {

	@Container
	@ServiceConnection
	private static final PostgreSQLContainer<?> POSTGRES =
			new PostgreSQLContainer<>("postgres:17-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private JobPostingService jobPostingService;

	@Autowired
	private JobApplicationService applicationService;

	@Autowired
	private JobApplicationRepository applicationRepository;

	@Autowired
	private ApplicationStatusEventRepository eventRepository;

	@BeforeEach
	void clearDatabase() {
		jdbcTemplate.execute("TRUNCATE TABLE companies CASCADE");
	}

	@Test
	void companyLifecycleRunsAgainstTheFlywaySchema() {
		CompanyResponse created = companyService.create(
				new CompanyRequest("  Zebra Labs ", "https://zebra.example", "Initial notes")
		);
		companyService.create(new CompanyRequest("Acme", null, null));

		assertThat(created.id()).isNotNull();
		assertThat(created.createdAt()).isNotNull();
		assertThat(companyService.list(false))
				.extracting(CompanyResponse::name)
				.containsExactly("Acme", "Zebra Labs");

		companyService.update(
				created.id(),
				new CompanyRequest("Zebra Technologies", "", "Updated notes")
		);
		assertThat(companyService.archive(created.id()).archived()).isTrue();
		assertThat(companyService.list(false))
				.extracting(CompanyResponse::name)
				.containsExactly("Acme");
		assertThat(companyService.restore(created.id()).archived()).isFalse();
	}

	@Test
	void completeHttpWorkflowPersistsRelationshipsAndAuditHistory() throws Exception {
		UUID companyId = responseId(mockMvc.perform(post("/api/companies")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name": "Acme"}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString());

		UUID postingId = responseId(mockMvc.perform(post("/api/job-postings")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "companyId": "%s",
								  "title": "Java Backend Developer",
								  "sourceUrl": "https://jobs.example/java",
								  "location": "Budapest",
								  "workMode": "HYBRID",
								  "targetLane": "JAVA_BACKEND",
								  "seniority": "JUNIOR",
								  "salaryMinGrossHuf": 910000,
								  "salaryMaxGrossHuf": 1120000,
								  "roleFitScore": 28,
								  "mentoringScore": 20,
								  "salaryScore": 18,
								  "engineeringPracticesScore": 8,
								  "learningSignalScore": 8,
								  "hybridFitScore": 5
								}
								""".formatted(companyId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.companyName").value("Acme"))
				.andExpect(jsonPath("$.triageScore").value(87))
				.andExpect(jsonPath("$.triagePriority").value("PRIORITIZE"))
				.andReturn()
				.getResponse()
				.getContentAsString());

		UUID applicationId = responseId(mockMvc.perform(post("/api/job-applications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "jobPostingId": "%s",
								  "notes": "Strong match"
								}
								""".formatted(postingId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("SAVED"))
				.andReturn()
				.getResponse()
				.getContentAsString());

		mockMvc.perform(post("/api/job-applications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"jobPostingId": "%s"}
								""".formatted(postingId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.errorCode").value("JOB_APPLICATION_CONFLICT"));

		transition(applicationId, ApplicationStatus.APPLIED, "Submitted on company site");
		transition(applicationId, ApplicationStatus.SCREENING, "Recruiter call booked");

		UUID nextActionId = responseId(mockMvc.perform(post("/api/next-actions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "applicationId": "%s",
								  "type": "PREPARE_SCREENING",
								  "dueDate": "2026-07-03",
								  "notes": "Review CV and company notes"
								}
								""".formatted(applicationId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.type").value("PREPARE_SCREENING"))
				.andReturn()
				.getResponse()
				.getContentAsString());

		mockMvc.perform(post("/api/next-actions/{id}/complete", nextActionId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.completedAt").exists());

		mockMvc.perform(post("/api/interview-rounds")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "applicationId": "%s",
								  "roundType": "HR_SCREEN",
								  "scheduledAt": "2026-07-04T09:00:00Z",
								  "format": "VIDEO",
								  "contactName": "Recruiter"
								}
								""".formatted(applicationId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.outcome").value("SCHEDULED"));

		mockMvc.perform(post("/api/offers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "applicationId": "%s",
								  "grossMonthlyHuf": 980000,
								  "hybridPolicy": "Budapest hybrid",
								  "decision": "PENDING"
								}
								""".formatted(applicationId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.decision").value("PENDING"));

		mockMvc.perform(get("/api/job-applications/{id}/history", applicationId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[0].newStatus").value("SAVED"))
				.andExpect(jsonPath("$[1].newStatus").value("APPLIED"))
				.andExpect(jsonPath("$[2].newStatus").value("SCREENING"));

		assertThat(applicationService.get(applicationId).companyName()).isEqualTo("Acme");
		assertThat(applicationService.get(applicationId).status())
				.isEqualTo(ApplicationStatus.SCREENING);

		mockMvc.perform(get("/api/dashboard"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.applicationsByStatus.SCREENING").value(1))
				.andExpect(jsonPath("$.pendingOffers").value(1));
	}

	@Test
	void auditFailureRollsBackTheStatusChange() {
		CompanyResponse company = companyService.create(new CompanyRequest("Acme", null, null));
		JobPostingResponse posting = jobPostingService.create(new JobPostingRequest(
				company.id(),
				"Java Developer",
				null,
				"Budapest",
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
		));
		JobApplicationResponse created = applicationService.create(
				new JobApplicationRequest(posting.id(), null)
		);
		JobApplication application = applicationRepository.findById(created.id()).orElseThrow();

		eventRepository.saveAndFlush(new ApplicationStatusEvent(
				application,
				ApplicationStatus.SAVED,
				ApplicationStatus.APPLIED,
				"Conflicting event inserted by the test"
		));

		assertThatThrownBy(() -> applicationService.transition(
				created.id(),
				new StatusTransitionRequest(ApplicationStatus.APPLIED, "Submitted")
		)).isInstanceOf(ApplicationStatusConflictException.class);

		assertThat(applicationService.get(created.id()).status()).isEqualTo(ApplicationStatus.SAVED);
	}

	@Test
	void optimisticLockingRejectsAStaleApplicationUpdate() {
		CompanyResponse company = companyService.create(new CompanyRequest("Acme", null, null));
		JobPostingResponse posting = jobPostingService.create(new JobPostingRequest(
				company.id(),
				"Java Developer",
				null,
				"Budapest",
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
		));
		JobApplicationResponse created = applicationService.create(
				new JobApplicationRequest(posting.id(), null)
		);

		JobApplication firstCopy = applicationRepository.findById(created.id()).orElseThrow();
		JobApplication staleCopy = applicationRepository.findById(created.id()).orElseThrow();
		firstCopy.transitionTo(ApplicationStatus.APPLIED);
		staleCopy.transitionTo(ApplicationStatus.WITHDRAWN);

		applicationRepository.saveAndFlush(firstCopy);

		assertThatThrownBy(() -> applicationRepository.saveAndFlush(staleCopy))
				.isInstanceOf(ObjectOptimisticLockingFailureException.class);
		assertThat(applicationService.get(created.id()).status()).isEqualTo(ApplicationStatus.APPLIED);
	}

	@Test
	void openApiDocumentIncludesAllResources() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("CareerOps API"))
				.andExpect(jsonPath("$.paths['/api/companies']").exists())
				.andExpect(jsonPath("$.paths['/api/job-postings']").exists())
				.andExpect(jsonPath("$.paths['/api/job-applications']").exists())
				.andExpect(jsonPath("$.paths['/api/next-actions']").exists())
				.andExpect(jsonPath("$.paths['/api/interview-rounds']").exists())
				.andExpect(jsonPath("$.paths['/api/offers']").exists())
				.andExpect(jsonPath("$.paths['/api/dashboard']").exists());
	}

	private void transition(UUID applicationId, ApplicationStatus status, String note) throws Exception {
		mockMvc.perform(post("/api/job-applications/{id}/status", applicationId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "targetStatus": "%s",
								  "note": "%s"
								}
								""".formatted(status, note)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(status.name()));
	}

	private UUID responseId(String content) throws Exception {
		JsonNode response = objectMapper.readTree(content);
		return UUID.fromString(response.get("id").asText());
	}
}
