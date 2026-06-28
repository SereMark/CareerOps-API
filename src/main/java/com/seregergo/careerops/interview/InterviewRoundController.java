package com.seregergo.careerops.interview;

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
@RequestMapping("/api/interview-rounds")
public class InterviewRoundController {

	private final InterviewRoundService interviewRoundService;

	public InterviewRoundController(InterviewRoundService interviewRoundService) {
		this.interviewRoundService = interviewRoundService;
	}

	@PostMapping
	public ResponseEntity<InterviewRoundResponse> create(
			@Valid @RequestBody InterviewRoundRequest request
	) {
		InterviewRoundResponse response = interviewRoundService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	public List<InterviewRoundResponse> list(@RequestParam(required = false) UUID applicationId) {
		return interviewRoundService.list(applicationId);
	}

	@GetMapping("/{id}")
	public InterviewRoundResponse get(@PathVariable UUID id) {
		return interviewRoundService.get(id);
	}

	@PutMapping("/{id}")
	public InterviewRoundResponse update(
			@PathVariable UUID id,
			@Valid @RequestBody InterviewRoundRequest request
	) {
		return interviewRoundService.update(id, request);
	}
}
