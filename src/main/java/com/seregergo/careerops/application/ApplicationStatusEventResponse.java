package com.seregergo.careerops.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationStatusEventResponse(
		UUID id,
		ApplicationStatus previousStatus,
		ApplicationStatus newStatus,
		String note,
		Instant occurredAt
) {

	static ApplicationStatusEventResponse from(ApplicationStatusEvent event) {
		return new ApplicationStatusEventResponse(
				event.getId(),
				event.getPreviousStatus(),
				event.getNewStatus(),
				event.getNote(),
				event.getOccurredAt()
		);
	}
}
