package com.seregergo.careerops.offer;

import com.seregergo.careerops.application.JobApplication;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "offers")
public class Offer {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "application_id", nullable = false, unique = true)
	private JobApplication application;

	@Column(name = "gross_monthly_huf", nullable = false)
	private int grossMonthlyHuf;

	@Column(length = 5000)
	private String benefits;

	@Column(name = "hybrid_policy", length = 1000)
	private String hybridPolicy;

	@Column(name = "review_promise", length = 1000)
	private String reviewPromise;

	@Column(name = "expires_at")
	private Instant expiresAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private OfferDecision decision;

	@Column(name = "decision_notes", length = 5000)
	private String decisionNotes;

	@Column(name = "created_at", nullable = false, updatable = false)
	@CreationTimestamp
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	@UpdateTimestamp
	private Instant updatedAt;

	protected Offer() {
	}

	Offer(
			JobApplication application,
			int grossMonthlyHuf,
			String benefits,
			String hybridPolicy,
			String reviewPromise,
			Instant expiresAt,
			OfferDecision decision,
			String decisionNotes
	) {
		this.application = application;
		this.grossMonthlyHuf = grossMonthlyHuf;
		this.benefits = benefits;
		this.hybridPolicy = hybridPolicy;
		this.reviewPromise = reviewPromise;
		this.expiresAt = expiresAt;
		this.decision = decision;
		this.decisionNotes = decisionNotes;
	}

	void replaceDetails(
			JobApplication application,
			int grossMonthlyHuf,
			String benefits,
			String hybridPolicy,
			String reviewPromise,
			Instant expiresAt,
			OfferDecision decision,
			String decisionNotes
	) {
		this.application = application;
		this.grossMonthlyHuf = grossMonthlyHuf;
		this.benefits = benefits;
		this.hybridPolicy = hybridPolicy;
		this.reviewPromise = reviewPromise;
		this.expiresAt = expiresAt;
		this.decision = decision;
		this.decisionNotes = decisionNotes;
	}

	public UUID getId() {
		return id;
	}

	public JobApplication getApplication() {
		return application;
	}

	public int getGrossMonthlyHuf() {
		return grossMonthlyHuf;
	}

	public String getBenefits() {
		return benefits;
	}

	public String getHybridPolicy() {
		return hybridPolicy;
	}

	public String getReviewPromise() {
		return reviewPromise;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public OfferDecision getDecision() {
		return decision;
	}

	public String getDecisionNotes() {
		return decisionNotes;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
