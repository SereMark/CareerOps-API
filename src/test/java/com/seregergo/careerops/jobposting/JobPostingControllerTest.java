package com.seregergo.careerops.jobposting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobPostingController.class)
class JobPostingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JobPostingService jobPostingService;

	@Test
	void createReturnsPostingAndLocation() throws Exception {
		UUID id = UUID.randomUUID();
		UUID companyId = UUID.randomUUID();
		when(jobPostingService.create(any(JobPostingRequest.class)))
				.thenReturn(response(id, companyId));

		mockMvc.perform(post("/api/job-postings")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "companyId": "%s",
								  "title": "Java Developer",
								  "workMode": "HYBRID"
								}
								""".formatted(companyId)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/job-postings/" + id))
				.andExpect(jsonPath("$.title").value("Java Developer"))
				.andExpect(jsonPath("$.workMode").value("HYBRID"));
	}

	@Test
	void listPassesOptionalCompanyFilter() throws Exception {
		UUID companyId = UUID.randomUUID();
		when(jobPostingService.list(companyId))
				.thenReturn(List.of(response(UUID.randomUUID(), companyId)));

		mockMvc.perform(get("/api/job-postings").queryParam("companyId", companyId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].companyId").value(companyId.toString()));

		verify(jobPostingService).list(companyId);
	}

	@Test
	void updateReturnsReplacement() throws Exception {
		UUID id = UUID.randomUUID();
		UUID companyId = UUID.randomUUID();
		when(jobPostingService.update(eq(id), any(JobPostingRequest.class)))
				.thenReturn(response(id, companyId));

		mockMvc.perform(put("/api/job-postings/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "companyId": "%s",
								  "title": "Java Developer",
								  "workMode": "HYBRID"
								}
								""".formatted(companyId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id.toString()));
	}

	@Test
	void invalidPostingReturnsFieldErrors() throws Exception {
		mockMvc.perform(post("/api/job-postings")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": " ",
								  "sourceUrl": "file:///tmp/job",
								  "workMode": null
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.fieldErrors.companyId[0]").value("Company is required"))
				.andExpect(jsonPath("$.fieldErrors.title[0]").value("Title is required"))
				.andExpect(jsonPath("$.fieldErrors.sourceUrl[0]")
						.value("Source URL must be a valid HTTP or HTTPS URL"))
				.andExpect(jsonPath("$.fieldErrors.workMode[0]").value("Work mode is required"));
	}

	private static JobPostingResponse response(UUID id, UUID companyId) {
		Instant now = Instant.parse("2026-06-25T12:00:00Z");
		return new JobPostingResponse(
				id,
				companyId,
				"Acme",
				"Java Developer",
				"https://jobs.example/1",
				"Budapest",
				WorkMode.HYBRID,
				TargetLane.JAVA_BACKEND,
				Seniority.JUNIOR,
				910000,
				1120000,
				25,
				20,
				18,
				8,
				8,
				4,
				83,
				TriagePriority.PRIORITIZE,
				null,
				null,
				now,
				now
		);
	}
}
