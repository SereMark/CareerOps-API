package com.seregergo.careerops.offer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OfferController.class)
class OfferControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OfferService offerService;

	@Test
	void createReturnsOfferAndLocation() throws Exception {
		UUID id = UUID.randomUUID();
		UUID applicationId = UUID.randomUUID();
		when(offerService.create(any(OfferRequest.class))).thenReturn(response(id, applicationId));

		mockMvc.perform(post("/api/offers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "applicationId": "%s",
								  "grossMonthlyHuf": 980000,
								  "decision": "PENDING"
								}
								""".formatted(applicationId)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/offers/" + id))
				.andExpect(jsonPath("$.grossMonthlyHuf").value(980000))
				.andExpect(jsonPath("$.decision").value("PENDING"));
	}

	@Test
	void duplicateOfferReturnsStableConflict() throws Exception {
		UUID applicationId = UUID.randomUUID();
		when(offerService.create(any(OfferRequest.class)))
				.thenThrow(new DuplicateOfferException(applicationId));

		mockMvc.perform(post("/api/offers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "applicationId": "%s",
								  "grossMonthlyHuf": 980000
								}
								""".formatted(applicationId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.errorCode").value("OFFER_CONFLICT"));
	}

	@Test
	void missingSalaryReturnsFieldError() throws Exception {
		UUID applicationId = UUID.randomUUID();

		mockMvc.perform(post("/api/offers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "applicationId": "%s"
								}
								""".formatted(applicationId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.fieldErrors.grossMonthlyHuf[0]")
						.value("Gross monthly salary is required"));
	}

	@Test
	void missingOfferReturnsStableProblem() throws Exception {
		UUID id = UUID.randomUUID();
		when(offerService.get(eq(id))).thenThrow(new OfferNotFoundException(id));

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.get("/api/offers/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errorCode").value("OFFER_NOT_FOUND"));
	}

	private static OfferResponse response(UUID id, UUID applicationId) {
		Instant now = Instant.parse("2026-07-01T12:00:00Z");
		return new OfferResponse(
				id,
				applicationId,
				"Junior Java Backend Developer",
				"Acme",
				980000,
				"Cafeteria",
				"Hybrid Budapest",
				null,
				null,
				OfferDecision.PENDING,
				null,
				now,
				now
		);
	}
}
