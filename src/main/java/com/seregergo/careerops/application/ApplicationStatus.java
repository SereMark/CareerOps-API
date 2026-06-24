package com.seregergo.careerops.application;

public enum ApplicationStatus {
	SAVED,
	APPLIED,
	SCREENING,
	INTERVIEW,
	OFFER,
	ACCEPTED,
	REJECTED,
	WITHDRAWN;

	public boolean canTransitionTo(ApplicationStatus target) {
		return switch (this) {
			case SAVED -> target == APPLIED || target == WITHDRAWN;
			case APPLIED -> target == SCREENING || target == REJECTED || target == WITHDRAWN;
			case SCREENING -> target == INTERVIEW || target == REJECTED || target == WITHDRAWN;
			case INTERVIEW -> target == OFFER || target == REJECTED || target == WITHDRAWN;
			case OFFER -> target == ACCEPTED || target == REJECTED || target == WITHDRAWN;
			case ACCEPTED, REJECTED, WITHDRAWN -> false;
		};
	}
}
