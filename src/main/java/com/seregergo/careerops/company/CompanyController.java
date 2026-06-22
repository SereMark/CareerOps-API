package com.seregergo.careerops.company;

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
@RequestMapping("/api/companies")
public class CompanyController {

	private final CompanyService companyService;

	public CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}

	@PostMapping
	public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
		CompanyResponse response = companyService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	public List<CompanyResponse> list(
			@RequestParam(defaultValue = "false") boolean includeArchived
	) {
		return companyService.list(includeArchived);
	}

	@GetMapping("/{id}")
	public CompanyResponse get(@PathVariable UUID id) {
		return companyService.get(id);
	}

	@PutMapping("/{id}")
	public CompanyResponse update(
			@PathVariable UUID id,
			@Valid @RequestBody CompanyRequest request
	) {
		return companyService.update(id, request);
	}

	@PostMapping("/{id}/archive")
	public CompanyResponse archive(@PathVariable UUID id) {
		return companyService.archive(id);
	}

	@PostMapping("/{id}/restore")
	public CompanyResponse restore(@PathVariable UUID id) {
		return companyService.restore(id);
	}
}
