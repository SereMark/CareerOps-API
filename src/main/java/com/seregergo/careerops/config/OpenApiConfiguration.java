package com.seregergo.careerops.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

	@Bean
	OpenAPI careerOpsOpenApi() {
		return new OpenAPI().info(new Info()
				.title("CareerOps API")
				.version("v1")
				.description(
						"Java/Spring backend for tracking a job search: companies, "
								+ "job postings, applications, next actions, interview rounds, offers, "
								+ "and a dashboard."
				));
	}
}
