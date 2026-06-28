package com.seregergo.careerops.interview;

import java.util.UUID;

public class InterviewRoundNotFoundException extends RuntimeException {

	public InterviewRoundNotFoundException(UUID id) {
		super("Interview round not found: " + id);
	}
}
