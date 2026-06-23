package com.seregergo.careerops.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.hibernate.exception.ConstraintViolationException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

	@Mock
	private CompanyRepository companyRepository;

	private CompanyService companyService;

	@BeforeEach
	void setUp() {
		companyService = new CompanyService(companyRepository);
	}

	@Test
	void createNormalizesInputAndBlankOptionalFields() {
		when(companyRepository.existsByNormalizedName("acme")).thenReturn(false);
		when(companyRepository.saveAndFlush(any(Company.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		CompanyResponse response = companyService.create(new CompanyRequest("  Acme  ", "  ", "\t"));

		assertThat(response.name()).isEqualTo("Acme");
		assertThat(response.websiteUrl()).isNull();
		assertThat(response.notes()).isNull();
		verify(companyRepository).existsByNormalizedName("acme");
	}

	@Test
	void createRejectsDuplicateNormalizedName() {
		when(companyRepository.existsByNormalizedName("acme")).thenReturn(true);

		assertThatThrownBy(() -> companyService.create(new CompanyRequest(" ACME ", null, null)))
				.isInstanceOf(DuplicateCompanyNameException.class)
				.hasMessageContaining("ACME");

		verify(companyRepository, never()).saveAndFlush(any());
	}

	@Test
	void createMapsUniqueConstraintRaceToDomainConflict() {
		when(companyRepository.existsByNormalizedName("acme")).thenReturn(false);
		when(companyRepository.saveAndFlush(any(Company.class)))
				.thenThrow(integrityViolation("uk_companies_normalized_name"));

		assertThatThrownBy(() -> companyService.create(new CompanyRequest("Acme", null, null)))
				.isInstanceOf(DuplicateCompanyNameException.class);
	}

	@Test
	void createDoesNotMaskUnrelatedIntegrityViolations() {
		DataIntegrityViolationException violation = integrityViolation("another_constraint");
		when(companyRepository.existsByNormalizedName("acme")).thenReturn(false);
		when(companyRepository.saveAndFlush(any(Company.class))).thenThrow(violation);

		assertThatThrownBy(() -> companyService.create(new CompanyRequest("Acme", null, null)))
				.isSameAs(violation);
	}

	@Test
	void updateReplacesEditableFieldsAndExcludesCurrentCompanyFromDuplicateCheck() {
		UUID id = UUID.randomUUID();
		Company company = new Company("Old name", "old name", "https://old.example", "Old notes");
		when(companyRepository.findById(id)).thenReturn(Optional.of(company));
		when(companyRepository.existsByNormalizedNameAndIdNot("new name", id)).thenReturn(false);
		when(companyRepository.saveAndFlush(company)).thenReturn(company);

		CompanyResponse response = companyService.update(
				id,
				new CompanyRequest(" New Name ", " https://new.example ", " New notes ")
		);

		assertThat(response.name()).isEqualTo("New Name");
		assertThat(response.websiteUrl()).isEqualTo("https://new.example");
		assertThat(response.notes()).isEqualTo("New notes");
		verify(companyRepository).existsByNormalizedNameAndIdNot("new name", id);
	}

	@Test
	void archiveAndRestoreAreIdempotent() {
		UUID id = UUID.randomUUID();
		Company company = new Company("Acme", "acme", null, null);
		when(companyRepository.findById(id)).thenReturn(Optional.of(company));
		when(companyRepository.saveAndFlush(company)).thenReturn(company);

		assertThat(companyService.archive(id).archived()).isTrue();
		assertThat(companyService.archive(id).archived()).isTrue();
		assertThat(companyService.restore(id).archived()).isFalse();
		assertThat(companyService.restore(id).archived()).isFalse();

		verify(companyRepository, times(2)).saveAndFlush(company);
	}

	@Test
	void listUsesActiveOrCompleteRepositoryQuery() {
		Company active = new Company("Acme", "acme", null, null);
		Company archived = new Company("Beta", "beta", null, null);
		archived.archive();
		when(companyRepository.findAllByArchivedFalseOrderByNormalizedNameAsc()).thenReturn(List.of(active));
		when(companyRepository.findAllByOrderByNormalizedNameAsc()).thenReturn(List.of(active, archived));

		assertThat(companyService.list(false)).extracting(CompanyResponse::name).containsExactly("Acme");
		assertThat(companyService.list(true)).extracting(CompanyResponse::name).containsExactly("Acme", "Beta");
	}

	@Test
	void getRejectsUnknownCompany() {
		UUID id = UUID.randomUUID();
		when(companyRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> companyService.get(id))
				.isInstanceOf(CompanyNotFoundException.class)
				.hasMessageContaining(id.toString());
	}

	private static DataIntegrityViolationException integrityViolation(String constraintName) {
		ConstraintViolationException cause = new ConstraintViolationException(
				"Constraint violation",
				new SQLException("Database rejected the write"),
				constraintName
		);
		return new DataIntegrityViolationException("Write failed", cause);
	}
}
