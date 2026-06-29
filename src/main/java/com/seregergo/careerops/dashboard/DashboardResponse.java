package com.seregergo.careerops.dashboard;

import com.seregergo.careerops.application.ApplicationStatus;
import com.seregergo.careerops.application.JobApplicationResponse;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
		Map<ApplicationStatus, Long> applicationsByStatus,
		long activeApplications,
		long openNextActions,
		long overdueNextActions,
		long interviewsNextSevenDays,
		long pendingOffers,
		List<JobApplicationResponse> staleApplications
) {
}
