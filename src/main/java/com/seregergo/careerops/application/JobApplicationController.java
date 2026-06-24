package com.seregergo.careerops.application;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-applications")
public class JobApplicationController {

	private final JobApplicationService applicationService;

	public JobApplicationController(JobApplicationService applicationService) {
		this.applicationService = applicationService;
	}

	@PostMapping
	public ResponseEntity<JobApplicationResponse> create(
			@Valid @RequestBody JobApplicationRequest request
	) {
		JobApplicationResponse response = applicationService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	public List<JobApplicationResponse> list(
			@RequestParam(required = false) ApplicationStatus status
	) {
		return applicationService.list(status);
	}

	@GetMapping("/{id}")
	public JobApplicationResponse get(@PathVariable UUID id) {
		return applicationService.get(id);
	}


}
