package com.seregergo.careerops.application;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobApplicationController.class)
class JobApplicationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JobApplicationService applicationService;

	@Test
	void createReturnsApplicationAndLocation() throws Exception {
		UUID id = UUID.randomUUID();
		UUID postingId = UUID.randomUUID();
		when(applicationService.create(any(JobApplicationRequest.class)))
				.thenReturn(response(id, postingId, ApplicationStatus.SAVED));

		mockMvc.perform(post("/api/job-applications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"jobPostingId": "%s"}
								""".formatted(postingId)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/job-applications/" + id))
				.andExpect(jsonPath("$.status").value("SAVED"));
	}

	@Test
	void listPassesOptionalStatusFilter() throws Exception {
		when(applicationService.list(ApplicationStatus.INTERVIEW))
				.thenReturn(List.of(response(
						UUID.randomUUID(),
						UUID.randomUUID(),
						ApplicationStatus.INTERVIEW
				)));

		mockMvc.perform(get("/api/job-applications").queryParam("status", "INTERVIEW"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status").value("INTERVIEW"));

		verify(applicationService).list(ApplicationStatus.INTERVIEW);
	}

	@Test
	void transitionReturnsTheUpdatedApplication() throws Exception {
		UUID id = UUID.randomUUID();
		UUID postingId = UUID.randomUUID();
		when(applicationService.transition(eq(id), any(StatusTransitionRequest.class)))
				.thenReturn(response(id, postingId, ApplicationStatus.APPLIED));

		mockMvc.perform(post("/api/job-applications/{id}/status", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "targetStatus": "APPLIED",
								  "note": "Submitted"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPLIED"));
	}

	@Test
	void historyReturnsEventsInServiceOrder() throws Exception {
		UUID id = UUID.randomUUID();
		Instant now = Instant.parse("2026-06-25T12:00:00Z");
		when(applicationService.history(id)).thenReturn(List.of(
				new ApplicationStatusEventResponse(
						UUID.randomUUID(),
						null,
						ApplicationStatus.SAVED,
						"Application added",
						now
				),
				new ApplicationStatusEventResponse(
						UUID.randomUUID(),
						ApplicationStatus.SAVED,
						ApplicationStatus.APPLIED,
						"Submitted",
						now.plusSeconds(1)
				)
		));

		mockMvc.perform(get("/api/job-applications/{id}/history", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].newStatus").value("SAVED"))
				.andExpect(jsonPath("$[1].newStatus").value("APPLIED"));
	}

	@Test
	void invalidTransitionReturnsAStableConflict() throws Exception {
		UUID id = UUID.randomUUID();
		when(applicationService.transition(eq(id), any(StatusTransitionRequest.class)))
				.thenThrow(new InvalidApplicationStatusTransitionException(
						ApplicationStatus.SAVED,
						ApplicationStatus.OFFER
				));

		mockMvc.perform(post("/api/job-applications/{id}/status", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"targetStatus": "OFFER"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.errorCode").value("INVALID_STATUS_TRANSITION"));
	}

	@Test
	void concurrentTransitionReturnsAStableConflict() throws Exception {
		UUID id = UUID.randomUUID();
		when(applicationService.transition(eq(id), any(StatusTransitionRequest.class)))
				.thenThrow(new ApplicationStatusConflictException());

		mockMvc.perform(post("/api/job-applications/{id}/status", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"targetStatus": "APPLIED"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.errorCode").value("APPLICATION_STATUS_CONFLICT"));
	}

	private static JobApplicationResponse response(
			UUID id,
			UUID postingId,
			ApplicationStatus status
	) {
		Instant now = Instant.parse("2026-06-25T12:00:00Z");
		return new JobApplicationResponse(
				id,
				postingId,
				"Java Developer",
				UUID.randomUUID(),
				"Acme",
				status,
				null,
				now,
				now
		);
	}
}
