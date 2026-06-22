package com.seregergo.careerops.company;

import java.util.UUID;

public class CompanyNotFoundException extends RuntimeException {

	public CompanyNotFoundException(UUID id) {
		super("Company with ID " + id + " was not found");
	}
}
