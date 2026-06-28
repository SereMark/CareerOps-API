package com.seregergo.careerops.interview;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterviewRoundController.class)
class InterviewRoundControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private InterviewRoundService interviewRoundService;

	@Test
	void createReturnsRoundAndLocation() throws Exception {
		UUID id = UUID.randomUUID();
		UUID applicationId = UUID.randomUUID();
		when(interviewRoundService.create(any(InterviewRoundRequest.class)))
				.thenReturn(response(id, applicationId));

		mockMvc.perform(post("/api/interview-rounds")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "applicationId": "%s",
								  "roundType": "TECHNICAL_SCREEN",
								  "scheduledAt": "2026-07-04T09:00:00Z",
								  "format": "VIDEO"
								}
								""".formatted(applicationId)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/interview-rounds/" + id))
				.andExpect(jsonPath("$.roundType").value("TECHNICAL_SCREEN"))
				.andExpect(jsonPath("$.outcome").value("SCHEDULED"));
	}

	@Test
	void listPassesApplicationFilter() throws Exception {
		UUID applicationId = UUID.randomUUID();
		when(interviewRoundService.list(applicationId))
				.thenReturn(List.of(response(UUID.randomUUID(), applicationId)));

		mockMvc.perform(get("/api/interview-rounds")
						.queryParam("applicationId", applicationId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].applicationId").value(applicationId.toString()));

		verify(interviewRoundService).list(applicationId);
	}

	private static InterviewRoundResponse response(UUID id, UUID applicationId) {
		Instant now = Instant.parse("2026-07-01T12:00:00Z");
		return new InterviewRoundResponse(
				id,
				applicationId,
				"Junior Java Backend Developer",
				"Acme",
				InterviewRoundType.TECHNICAL_SCREEN,
				Instant.parse("2026-07-04T09:00:00Z"),
				InterviewFormat.VIDEO,
				InterviewOutcome.SCHEDULED,
				"Recruiter",
				null,
				null,
				null,
				now,
				now
		);
	}
}
