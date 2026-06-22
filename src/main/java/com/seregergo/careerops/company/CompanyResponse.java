package com.seregergo.careerops.company;

import java.time.Instant;
import java.util.UUID;

public record CompanyResponse(
		UUID id,
		String name,
		String websiteUrl,
		String notes,
		boolean archived,
		Instant createdAt,
		Instant updatedAt
) {

	static CompanyResponse from(Company company) {
		return new CompanyResponse(
				company.getId(),
				company.getName(),
				company.getWebsiteUrl(),
				company.getNotes(),
				company.isArchived(),
				company.getCreatedAt(),
				company.getUpdatedAt()
		);
	}
}
