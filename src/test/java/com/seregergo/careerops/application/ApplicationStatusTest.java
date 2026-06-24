package com.seregergo.careerops.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationStatusTest {

	@Test
	void supportsTheExpectedHiringPath() {
		assertThat(ApplicationStatus.SAVED.canTransitionTo(ApplicationStatus.APPLIED)).isTrue();
		assertThat(ApplicationStatus.APPLIED.canTransitionTo(ApplicationStatus.SCREENING)).isTrue();
		assertThat(ApplicationStatus.SCREENING.canTransitionTo(ApplicationStatus.INTERVIEW)).isTrue();
		assertThat(ApplicationStatus.INTERVIEW.canTransitionTo(ApplicationStatus.OFFER)).isTrue();
		assertThat(ApplicationStatus.OFFER.canTransitionTo(ApplicationStatus.ACCEPTED)).isTrue();
	}

	@Test
	void terminalStatusesCannotBeReopened() {
		for (ApplicationStatus terminal : new ApplicationStatus[]{
				ApplicationStatus.ACCEPTED,
				ApplicationStatus.REJECTED,
				ApplicationStatus.WITHDRAWN
		}) {
			for (ApplicationStatus target : ApplicationStatus.values()) {
				assertThat(terminal.canTransitionTo(target)).isFalse();
			}
		}
	}

	@Test
	void statusesCannotBeSkippedOrRepeated() {
		assertThat(ApplicationStatus.SAVED.canTransitionTo(ApplicationStatus.INTERVIEW)).isFalse();
		assertThat(ApplicationStatus.APPLIED.canTransitionTo(ApplicationStatus.APPLIED)).isFalse();
		assertThat(ApplicationStatus.INTERVIEW.canTransitionTo(ApplicationStatus.SCREENING)).isFalse();
	}
}
