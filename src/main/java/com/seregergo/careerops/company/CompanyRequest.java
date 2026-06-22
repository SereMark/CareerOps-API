package com.seregergo.careerops.company;

import com.seregergo.careerops.validation.HttpUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
		@NotBlank(message = "Name is required")
		@Size(max = 200, message = "Name must not exceed 200 characters")
		String name,

		@Size(max = 2048, message = "Website URL must not exceed 2048 characters")
		@HttpUrl
		String websiteUrl,

		@Size(max = 5000, message = "Notes must not exceed 5000 characters")
		String notes
) {
}
