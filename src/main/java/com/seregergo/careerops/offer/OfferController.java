package com.seregergo.careerops.offer;

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
@RequestMapping("/api/offers")
public class OfferController {

	private final OfferService offerService;

	public OfferController(OfferService offerService) {
		this.offerService = offerService;
	}

	@PostMapping
	public ResponseEntity<OfferResponse> create(@Valid @RequestBody OfferRequest request) {
		OfferResponse response = offerService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	public List<OfferResponse> list(@RequestParam(required = false) UUID applicationId) {
		return offerService.list(applicationId);
	}

	@GetMapping("/{id}")
	public OfferResponse get(@PathVariable UUID id) {
		return offerService.get(id);
	}

	@PutMapping("/{id}")
	public OfferResponse update(
			@PathVariable UUID id,
			@Valid @RequestBody OfferRequest request
	) {
		return offerService.update(id, request);
	}
}
