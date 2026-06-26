package com.seregergo.careerops.application;

import com.seregergo.careerops.jobposting.JobPosting;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_applications")
public class JobApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "job_posting_id", nullable = false, unique = true)
	private JobPosting jobPosting;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ApplicationStatus status;

	@Column(length = 5000)
	private String notes;

	@Version
	@Column(nullable = false)
	private long version;

	@Column(name = "created_at", nullable = false, updatable = false)
	@CreationTimestamp
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	@UpdateTimestamp
	private Instant updatedAt;

	protected JobApplication() {
	}

	JobApplication(JobPosting jobPosting, String notes) {
		this.jobPosting = jobPosting;
		this.status = ApplicationStatus.SAVED;
		this.notes = notes;
	}

	ApplicationStatus transitionTo(ApplicationStatus targetStatus) {
		ApplicationStatus previousStatus = status;
		if (!previousStatus.canTransitionTo(targetStatus)) {
			throw new InvalidApplicationStatusTransitionException(previousStatus, targetStatus);
		}
		status = targetStatus;
		return previousStatus;
	}

	public UUID getId() {
		return id;
	}

	public JobPosting getJobPosting() {
		return jobPosting;
	}

	public ApplicationStatus getStatus() {
		return status;
	}

	public String getNotes() {
		return notes;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
