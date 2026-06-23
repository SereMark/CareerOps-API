package com.seregergo.careerops.error;

import com.seregergo.careerops.company.CompanyNotFoundException;
import com.seregergo.careerops.company.DuplicateCompanyNameException;
import com.seregergo.careerops.jobposting.JobPostingNotFoundException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(CompanyNotFoundException.class)
	ResponseEntity<ProblemDetail> handleNotFound(CompanyNotFoundException exception) {
		return problem(ApiError.COMPANY_NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(DuplicateCompanyNameException.class)
	ResponseEntity<ProblemDetail> handleDuplicate(DuplicateCompanyNameException exception) {
		return problem(ApiError.COMPANY_NAME_CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(JobPostingNotFoundException.class)
	ResponseEntity<ProblemDetail> handleJobPostingNotFound(JobPostingNotFoundException exception) {
		return problem(ApiError.JOB_POSTING_NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, List<String>> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.groupingBy(
						FieldError::getField,
						LinkedHashMap::new,
						Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
				));

		ProblemDetail detail = createProblem(
				ApiError.VALIDATION_FAILED,
				"One or more request fields are invalid"
		);
		detail.setProperty("fieldErrors", fieldErrors);
		return ResponseEntity.status(ApiError.VALIDATION_FAILED.status()).body(detail);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		String detail = "Parameter '" + exception.getName() + "' has an invalid value";
		return problem(ApiError.INVALID_PARAMETER, detail);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ProblemDetail> handleUnreadableMessage() {
		return problem(ApiError.MALFORMED_REQUEST, "The request body could not be read");
	}

	private ResponseEntity<ProblemDetail> problem(ApiError error, String detail) {
		return ResponseEntity.status(error.status()).body(createProblem(error, detail));
	}

	private ProblemDetail createProblem(ApiError error, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(error.status(), detail);
		problem.setTitle(error.title());
		problem.setType(error.type());
		problem.setProperty("errorCode", error.name());
		return problem;
	}
}
