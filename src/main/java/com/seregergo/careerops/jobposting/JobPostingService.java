package com.seregergo.careerops.jobposting;

import com.seregergo.careerops.common.TextNormalizer;
import com.seregergo.careerops.company.Company;
import com.seregergo.careerops.company.CompanyNotFoundException;
import com.seregergo.careerops.company.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class JobPostingService {

	private final JobPostingRepository jobPostingRepository;
	private final CompanyRepository companyRepository;

	public JobPostingService(
			JobPostingRepository jobPostingRepository,
			CompanyRepository companyRepository
	) {
		this.jobPostingRepository = jobPostingRepository;
		this.companyRepository = companyRepository;
	}

	@Transactional
	public JobPostingResponse create(JobPostingRequest request) {
		Company company = findCompany(request.companyId());
		JobPostingInput input = normalize(request);
		JobPosting jobPosting = new JobPosting(
				company,
				input.title(),
				input.sourceUrl(),
				input.location(),
				input.workMode(),
				input.notes()
		);
		return JobPostingResponse.from(jobPostingRepository.saveAndFlush(jobPosting));
	}

	@Transactional(readOnly = true)
	public List<JobPostingResponse> list(UUID companyId) {
		List<JobPosting> jobPostings = companyId == null
				? jobPostingRepository.findAllByOrderByCreatedAtDesc()
				: jobPostingRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId);

		return jobPostings.stream()
				.map(JobPostingResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public JobPostingResponse get(UUID id) {
		return JobPostingResponse.from(findJobPosting(id));
	}

	@Transactional
	public JobPostingResponse update(UUID id, JobPostingRequest request) {
		JobPosting jobPosting = findJobPosting(id);
		Company company = findCompany(request.companyId());
		JobPostingInput input = normalize(request);
		jobPosting.replaceDetails(
				company,
				input.title(),
				input.sourceUrl(),
				input.location(),
				input.workMode(),
				input.notes()
		);
		return JobPostingResponse.from(jobPostingRepository.saveAndFlush(jobPosting));
	}

	private JobPosting findJobPosting(UUID id) {
		return jobPostingRepository.findById(id)
				.orElseThrow(() -> new JobPostingNotFoundException(id));
	}

	private Company findCompany(UUID id) {
		return companyRepository.findById(id)
				.orElseThrow(() -> new CompanyNotFoundException(id));
	}

	private static JobPostingInput normalize(JobPostingRequest request) {
		return new JobPostingInput(
				request.title().strip(),
				TextNormalizer.trimToNull(request.sourceUrl()),
				TextNormalizer.trimToNull(request.location()),
				request.workMode(),
				TextNormalizer.trimToNull(request.notes())
		);
	}

	private record JobPostingInput(
			String title,
			String sourceUrl,
			String location,
			WorkMode workMode,
			String notes
	) {
	}
}
