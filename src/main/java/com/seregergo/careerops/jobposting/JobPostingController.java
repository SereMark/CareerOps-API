package com.seregergo.careerops.jobposting;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-postings")
public class JobPostingController {

	private final JobPostingService jobPostingService;

	public JobPostingController(JobPostingService jobPostingService) {
		this.jobPostingService = jobPostingService;
	}

	@PostMapping
	public ResponseEntity<JobPostingResponse> create(@Valid @RequestBody JobPostingRequest request) {
		JobPostingResponse response = jobPostingService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	public List<JobPostingResponse> list(@RequestParam(required = false) UUID companyId) {
		return jobPostingService.list(companyId);
	}

	@GetMapping("/{id}")
	public JobPostingResponse get(@PathVariable UUID id) {
		return jobPostingService.get(id);
	}

	@PutMapping("/{id}")
	public JobPostingResponse update(
			@PathVariable UUID id,
			@Valid @RequestBody JobPostingRequest request
	) {
		return jobPostingService.update(id, request);
	}
}
