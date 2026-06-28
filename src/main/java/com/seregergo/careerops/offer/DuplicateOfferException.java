package com.seregergo.careerops.offer;

import java.util.UUID;

public class DuplicateOfferException extends RuntimeException {

	public DuplicateOfferException(UUID applicationId) {
		super("Offer already exists for application: " + applicationId);
	}
}
