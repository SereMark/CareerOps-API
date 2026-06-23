package com.seregergo.careerops.company;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CompanyService companyService;

	@Test
	void createReturnsCreatedCompanyAndLocation() throws Exception {
		UUID id = UUID.randomUUID();
		CompanyResponse response = response(id, "Acme", false);
		when(companyService.create(any(CompanyRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/companies")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Acme",
								  "websiteUrl": "https://acme.example",
								  "notes": "Priority target"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/companies/" + id))
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.name").value("Acme"))
				.andExpect(jsonPath("$.archived").value(false));
	}

	@Test
	void listUsesRequestedArchiveFilter() throws Exception {
		when(companyService.list(false)).thenReturn(List.of());
		when(companyService.list(true)).thenReturn(List.of(response(UUID.randomUUID(), "Acme", false)));

		mockMvc.perform(get("/api/companies"))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));

		mockMvc.perform(get("/api/companies").queryParam("includeArchived", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Acme"));

		verify(companyService).list(false);
		verify(companyService).list(true);
	}

	@Test
	void updateReturnsReplacementResult() throws Exception {
		UUID id = UUID.randomUUID();
		when(companyService.update(eq(id), any(CompanyRequest.class)))
				.thenReturn(response(id, "Updated", false));

		mockMvc.perform(put("/api/companies/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name": "Updated"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated"));
	}

	@Test
	void archiveAndRestoreReturnCurrentState() throws Exception {
		UUID id = UUID.randomUUID();
		when(companyService.archive(id)).thenReturn(response(id, "Acme", true));
		when(companyService.restore(id)).thenReturn(response(id, "Acme", false));

		mockMvc.perform(post("/api/companies/{id}/archive", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.archived").value(true));

		mockMvc.perform(post("/api/companies/{id}/restore", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.archived").value(false));
	}

	@Test
	void invalidRequestReturnsProblemDetailWithFieldErrors() throws Exception {
		mockMvc.perform(post("/api/companies")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": " ",
								  "websiteUrl": "ftp://invalid.example"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.type").value("urn:careerops:problem:validation-failed"))
				.andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.fieldErrors.name[0]").value("Name is required"))
				.andExpect(jsonPath("$.fieldErrors.websiteUrl[0]")
						.value("Website URL must be a valid HTTP or HTTPS URL"));
	}

	@Test
	void missingCompanyReturnsStableNotFoundProblem() throws Exception {
		UUID id = UUID.randomUUID();
		when(companyService.get(id)).thenThrow(new CompanyNotFoundException(id));

		mockMvc.perform(get("/api/companies/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.errorCode").value("COMPANY_NOT_FOUND"));
	}

	@Test
	void duplicateNameReturnsStableConflictProblem() throws Exception {
		when(companyService.create(any(CompanyRequest.class)))
				.thenThrow(new DuplicateCompanyNameException("Acme"));

		mockMvc.perform(post("/api/companies")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name": "Acme"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.errorCode").value("COMPANY_NAME_CONFLICT"));
	}

	@Test
	void malformedUuidReturnsInvalidParameterProblem() throws Exception {
		mockMvc.perform(get("/api/companies/not-a-uuid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("INVALID_PARAMETER"));
	}

	@Test
	void malformedJsonReturnsMalformedRequestProblem() throws Exception {
		mockMvc.perform(post("/api/companies")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("MALFORMED_REQUEST"));
	}

	private static CompanyResponse response(UUID id, String name, boolean archived) {
		Instant now = Instant.parse("2026-06-25T12:00:00Z");
		return new CompanyResponse(id, name, "https://example.com", "Notes", archived, now, now);
	}
}
