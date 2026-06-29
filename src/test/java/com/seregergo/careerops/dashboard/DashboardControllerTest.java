package com.seregergo.careerops.dashboard;

import com.seregergo.careerops.application.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DashboardService dashboardService;

	@Test
	void dashboardReturnsJobSearchSnapshot() throws Exception {
		Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
		counts.put(ApplicationStatus.SAVED, 2L);
		counts.put(ApplicationStatus.APPLIED, 3L);
		when(dashboardService.getDashboard())
				.thenReturn(new DashboardResponse(counts, 5, 4, 1, 2, 1, List.of()));

		mockMvc.perform(get("/api/dashboard"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.applicationsByStatus.SAVED").value(2))
				.andExpect(jsonPath("$.activeApplications").value(5))
				.andExpect(jsonPath("$.overdueNextActions").value(1))
				.andExpect(jsonPath("$.interviewsNextSevenDays").value(2))
				.andExpect(jsonPath("$.pendingOffers").value(1));
	}
}
