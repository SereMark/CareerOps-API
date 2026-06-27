package com.seregergo.careerops.nextaction;

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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/next-actions")
public class NextActionController {

	private final NextActionService nextActionService;

	public NextActionController(NextActionService nextActionService) {
		this.nextActionService = nextActionService;
	}

	@PostMapping
	public ResponseEntity<NextActionResponse> create(@Valid @RequestBody NextActionRequest request) {
		NextActionResponse response = nextActionService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	public List<NextActionResponse> list(
			@RequestParam(required = false) UUID applicationId,
			@RequestParam(required = false) Boolean completed,
			@RequestParam(required = false) LocalDate dueBefore
	) {
		return nextActionService.list(applicationId, completed, dueBefore);
	}

	@GetMapping("/{id}")
	public NextActionResponse get(@PathVariable UUID id) {
		return nextActionService.get(id);
	}

	@PutMapping("/{id}")
	public NextActionResponse update(
			@PathVariable UUID id,
			@Valid @RequestBody NextActionRequest request
	) {
		return nextActionService.update(id, request);
	}

	@PostMapping("/{id}/complete")
	public NextActionResponse complete(@PathVariable UUID id) {
		return nextActionService.complete(id);
	}
}
