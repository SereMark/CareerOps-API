package com.seregergo.careerops.company;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CompanyService {

	private static final String UNIQUE_NAME_CONSTRAINT = "uk_companies_normalized_name";

	private final CompanyRepository companyRepository;

	public CompanyService(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}

	@Transactional
	public CompanyResponse create(CompanyRequest request) {
		NormalizedCompanyInput input = normalize(request);
		if (companyRepository.existsByNormalizedName(input.normalizedName())) {
			throw new DuplicateCompanyNameException(input.name());
		}

		Company company = new Company(input.name(), input.normalizedName(), input.websiteUrl(), input.notes());
		return CompanyResponse.from(saveWithUniqueNameProtection(company, input.name()));
	}

	@Transactional(readOnly = true)
	public List<CompanyResponse> list(boolean includeArchived) {
		List<Company> companies = includeArchived
				? companyRepository.findAllByOrderByNormalizedNameAsc()
				: companyRepository.findAllByArchivedFalseOrderByNormalizedNameAsc();

		return companies.stream()
				.map(CompanyResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public CompanyResponse get(UUID id) {
		return CompanyResponse.from(findCompany(id));
	}

	@Transactional
	public CompanyResponse update(UUID id, CompanyRequest request) {
		Company company = findCompany(id);
		NormalizedCompanyInput input = normalize(request);

		if (companyRepository.existsByNormalizedNameAndIdNot(input.normalizedName(), id)) {
			throw new DuplicateCompanyNameException(input.name());
		}

		company.replaceDetails(input.name(), input.normalizedName(), input.websiteUrl(), input.notes());
		return CompanyResponse.from(saveWithUniqueNameProtection(company, input.name()));
	}

	@Transactional
	public CompanyResponse archive(UUID id) {
		Company company = findCompany(id);
		if (!company.isArchived()) {
			company.archive();
			companyRepository.saveAndFlush(company);
		}
		return CompanyResponse.from(company);
	}

	@Transactional
	public CompanyResponse restore(UUID id) {
		Company company = findCompany(id);
		if (company.isArchived()) {
			company.restore();
			companyRepository.saveAndFlush(company);
		}
		return CompanyResponse.from(company);
	}

	private Company findCompany(UUID id) {
		return companyRepository.findById(id)
				.orElseThrow(() -> new CompanyNotFoundException(id));
	}

	private Company saveWithUniqueNameProtection(Company company, String displayName) {
		try {
			return companyRepository.saveAndFlush(company);
		} catch (DataIntegrityViolationException exception) {
			// A concurrent request can pass the earlier check, so the database remains the final guard.
			if (violatesUniqueNameConstraint(exception)) {
				throw new DuplicateCompanyNameException(displayName);
			}
			throw exception;
		}
	}

	private static boolean violatesUniqueNameConstraint(Throwable exception) {
		Throwable cause = exception;
		while (cause != null) {
			if (cause instanceof ConstraintViolationException constraintViolation
					&& UNIQUE_NAME_CONSTRAINT.equals(constraintViolation.getConstraintName())) {
				return true;
			}
			cause = cause.getCause();
		}
		return false;
	}

	private static NormalizedCompanyInput normalize(CompanyRequest request) {
		String name = request.name().strip();
		String normalizedName = name.toLowerCase(Locale.ROOT);
		return new NormalizedCompanyInput(
				name,
				normalizedName,
				blankToNull(request.websiteUrl()),
				blankToNull(request.notes())
		);
	}

	private static String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String stripped = value.strip();
		return stripped.isEmpty() ? null : stripped;
	}

	private record NormalizedCompanyInput(String name, String normalizedName, String websiteUrl, String notes) {
	}
}
