package com.seregergo.careerops.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "logging.level.org.hibernate.engine.jdbc.spi.SqlExceptionHelper=OFF")
@Testcontainers
class CompanyPostgreSqlIntegrationTest {

	@Container
	@ServiceConnection
	private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

	@Autowired
	private CompanyService companyService;

	@Autowired
	private CompanyRepository companyRepository;

	@BeforeEach
	void clearDatabase() {
		companyRepository.deleteAll();
	}

	@Test
	void flywaySchemaSupportsCompleteCompanyLifecycle() {
		CompanyResponse created = companyService.create(
				new CompanyRequest("  Zebra Labs ", "https://zebra.example", "Initial notes")
		);
		companyService.create(new CompanyRequest("Acme", null, null));

		assertThat(created.id()).isNotNull();
		assertThat(created.createdAt()).isNotNull();
		assertThat(companyService.list(false))
				.extracting(CompanyResponse::name)
				.containsExactly("Acme", "Zebra Labs");

		CompanyResponse updated = companyService.update(
				created.id(),
				new CompanyRequest("Zebra Technologies", "", "Updated notes")
		);
		assertThat(updated.websiteUrl()).isNull();
		assertThat(updated.notes()).isEqualTo("Updated notes");

		assertThat(companyService.archive(created.id()).archived()).isTrue();
		assertThat(companyService.list(false))
				.extracting(CompanyResponse::name)
				.containsExactly("Acme");
		assertThat(companyService.list(true))
				.extracting(CompanyResponse::name)
				.containsExactly("Acme", "Zebra Technologies");

		assertThat(companyService.restore(created.id()).archived()).isFalse();
		assertThat(companyService.get(created.id()).name()).isEqualTo("Zebra Technologies");
	}

	@Test
	void databaseConstraintProtectsCaseInsensitiveNormalizedNameUniqueness() {
		companyRepository.saveAndFlush(new Company("Acme", "acme", null, null));

		assertThatThrownBy(() ->
				companyRepository.saveAndFlush(new Company("ACME", "acme", null, null))
		).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void serviceMapsDuplicateNameToDomainConflict() {
		companyService.create(new CompanyRequest("Acme", null, null));

		assertThatThrownBy(() -> companyService.create(new CompanyRequest("  ACME ", null, null)))
				.isInstanceOf(DuplicateCompanyNameException.class);
	}
}
