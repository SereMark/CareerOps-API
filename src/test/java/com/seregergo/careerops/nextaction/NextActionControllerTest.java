package com.seregergo.careerops.nextaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
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

@WebMvcTest(NextActionController.class)
class NextActionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NextActionService nextActionService;

	@Test
	void createReturnsActionAndLocation() throws Exception {
		UUID id = UUID.randomUUID();
		UUID applicationId = UUID.randomUUID();
		when(nextActionService.create(any(NextActionRequest.class)))
				.thenReturn(response(id, applicationId, null));

		mockMvc.perform(post("/api/next-actions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "applicationId": "%s",
								  "type": "FOLLOW_UP",
								  "dueDate": "2026-07-03"
								}
								""".formatted(applicationId)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/next-actions/" + id))
				.andExpect(jsonPath("$.type").value("FOLLOW_UP"))
				.andExpect(jsonPath("$.dueDate").value("2026-07-03"));
	}

	@Test
	void listPassesFiltersToService() throws Exception {
		UUID applicationId = UUID.randomUUID();
		LocalDate dueBefore = LocalDate.parse("2026-07-05");
		when(nextActionService.list(applicationId, false, dueBefore))
				.thenReturn(List.of(response(UUID.randomUUID(), applicationId, null)));

		mockMvc.perform(get("/api/next-actions")
						.queryParam("applicationId", applicationId.toString())
						.queryParam("completed", "false")
						.queryParam("dueBefore", dueBefore.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].applicationId").value(applicationId.toString()));

		verify(nextActionService).list(applicationId, false, dueBefore);
	}

	@Test
	void completeReturnsCompletedAction() throws Exception {
		UUID id = UUID.randomUUID();
		UUID applicationId = UUID.randomUUID();
		Instant completedAt = Instant.parse("2026-07-02T10:00:00Z");
		when(nextActionService.complete(id)).thenReturn(response(id, applicationId, completedAt));

		mockMvc.perform(post("/api/next-actions/{id}/complete", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.completedAt").value(completedAt.toString()));
	}

	@Test
	void missingActionReturnsStableProblem() throws Exception {
		UUID id = UUID.randomUUID();
		when(nextActionService.complete(eq(id))).thenThrow(new NextActionNotFoundException(id));

		mockMvc.perform(post("/api/next-actions/{id}/complete", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errorCode").value("NEXT_ACTION_NOT_FOUND"));
	}

	private static NextActionResponse response(UUID id, UUID applicationId, Instant completedAt) {
		Instant now = Instant.parse("2026-07-01T12:00:00Z");
		return new NextActionResponse(
				id,
				applicationId,
				"Junior Java Backend Developer",
				"Acme",
				NextActionType.FOLLOW_UP,
				LocalDate.parse("2026-07-03"),
				completedAt,
				null,
				now,
				now
		);
	}
}
