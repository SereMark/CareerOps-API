package com.seregergo.careerops.dashboard;

import com.seregergo.careerops.application.ApplicationStatus;
import com.seregergo.careerops.application.JobApplicationRepository;
import com.seregergo.careerops.application.JobApplicationResponse;
import com.seregergo.careerops.interview.InterviewOutcome;
import com.seregergo.careerops.interview.InterviewRoundRepository;
import com.seregergo.careerops.nextaction.NextActionRepository;
import com.seregergo.careerops.offer.OfferDecision;
import com.seregergo.careerops.offer.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

	private static final List<ApplicationStatus> TERMINAL_STATUSES = List.of(
			ApplicationStatus.ACCEPTED,
			ApplicationStatus.REJECTED,
			ApplicationStatus.WITHDRAWN
	);

	private final JobApplicationRepository applicationRepository;
	private final NextActionRepository nextActionRepository;
	private final InterviewRoundRepository interviewRoundRepository;
	private final OfferRepository offerRepository;
	private final Clock clock;

	public DashboardService(
			JobApplicationRepository applicationRepository,
			NextActionRepository nextActionRepository,
			InterviewRoundRepository interviewRoundRepository,
			OfferRepository offerRepository,
			Clock clock
	) {
		this.applicationRepository = applicationRepository;
		this.nextActionRepository = nextActionRepository;
		this.interviewRoundRepository = interviewRoundRepository;
		this.offerRepository = offerRepository;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public DashboardResponse getDashboard() {
		Instant now = clock.instant();
		LocalDate today = LocalDate.now(clock);
		Instant staleBefore = now.minus(7, ChronoUnit.DAYS);
		Instant nextWeek = now.plus(7, ChronoUnit.DAYS);

		return new DashboardResponse(
				applicationsByStatus(),
				applicationRepository.countByStatusNotIn(TERMINAL_STATUSES),
				nextActionRepository.countByCompletedAtIsNull(),
				nextActionRepository.countByCompletedAtIsNullAndDueDateBefore(today),
				interviewRoundRepository.countByScheduledAtBetweenAndOutcome(
						now,
						nextWeek,
						InterviewOutcome.SCHEDULED
				),
				offerRepository.countByDecision(OfferDecision.PENDING),
				applicationRepository
						.findAllByStatusNotInAndUpdatedAtBeforeOrderByUpdatedAtAsc(
								TERMINAL_STATUSES,
								staleBefore
						)
						.stream()
						.map(JobApplicationResponse::from)
						.toList()
		);
	}

	private Map<ApplicationStatus, Long> applicationsByStatus() {
		Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
		Arrays.stream(ApplicationStatus.values())
				.forEach(status -> counts.put(status, applicationRepository.countByStatus(status)));
		return counts;
	}
}
