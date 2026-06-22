package com.seregergo.careerops.company;

public class DuplicateCompanyNameException extends RuntimeException {

	public DuplicateCompanyNameException(String name) {
		super("A company named '" + name + "' already exists");
	}
}
