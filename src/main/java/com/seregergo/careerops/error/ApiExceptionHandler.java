package com.seregergo.careerops.error;

import com.seregergo.careerops.application.ApplicationStatusConflictException;
import com.seregergo.careerops.application.DuplicateJobApplicationException;
import com.seregergo.careerops.application.InvalidApplicationStatusTransitionException;
import com.seregergo.careerops.application.JobApplicationNotFoundException;
import com.seregergo.careerops.company.CompanyNotFoundException;
import com.seregergo.careerops.company.DuplicateCompanyNameException;
import com.seregergo.careerops.interview.InterviewRoundNotFoundException;
import com.seregergo.careerops.jobposting.JobPostingNotFoundException;
import com.seregergo.careerops.nextaction.NextActionNotFoundException;
import com.seregergo.careerops.offer.DuplicateOfferException;
import com.seregergo.careerops.offer.OfferNotFoundException;
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

	@ExceptionHandler(JobApplicationNotFoundException.class)
	ResponseEntity<ProblemDetail> handleJobApplicationNotFound(
			JobApplicationNotFoundException exception
	) {
		return problem(ApiError.JOB_APPLICATION_NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(DuplicateJobApplicationException.class)
	ResponseEntity<ProblemDetail> handleDuplicateApplication(
			DuplicateJobApplicationException exception
	) {
		return problem(ApiError.JOB_APPLICATION_CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(InvalidApplicationStatusTransitionException.class)
	ResponseEntity<ProblemDetail> handleInvalidStatusTransition(
			InvalidApplicationStatusTransitionException exception
	) {
		return problem(ApiError.INVALID_STATUS_TRANSITION, exception.getMessage());
	}

	@ExceptionHandler(ApplicationStatusConflictException.class)
	ResponseEntity<ProblemDetail> handleApplicationStatusConflict(
			ApplicationStatusConflictException exception
	) {
		return problem(ApiError.APPLICATION_STATUS_CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(NextActionNotFoundException.class)
	ResponseEntity<ProblemDetail> handleNextActionNotFound(NextActionNotFoundException exception) {
		return problem(ApiError.NEXT_ACTION_NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(InterviewRoundNotFoundException.class)
	ResponseEntity<ProblemDetail> handleInterviewRoundNotFound(
			InterviewRoundNotFoundException exception
	) {
		return problem(ApiError.INTERVIEW_ROUND_NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(OfferNotFoundException.class)
	ResponseEntity<ProblemDetail> handleOfferNotFound(OfferNotFoundException exception) {
		return problem(ApiError.OFFER_NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(DuplicateOfferException.class)
	ResponseEntity<ProblemDetail> handleDuplicateOffer(DuplicateOfferException exception) {
		return problem(ApiError.OFFER_CONFLICT, exception.getMessage());
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
