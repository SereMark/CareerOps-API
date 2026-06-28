package com.seregergo.careerops.interview;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interview_rounds")
public class InterviewRound {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "application_id", nullable = false)
	private JobApplication application;

	@Enumerated(EnumType.STRING)
	@Column(name = "round_type", nullable = false, length = 40)
	private InterviewRoundType roundType;

	@Column(name = "scheduled_at", nullable = false)
	private Instant scheduledAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private InterviewFormat format;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private InterviewOutcome outcome;

	@Column(name = "contact_name", length = 200)
	private String contactName;

	@Column(name = "prep_notes", length = 5000)
	private String prepNotes;

	@Column(name = "questions_asked", length = 5000)
	private String questionsAsked;

	@Column(name = "follow_up_sent_at")
	private Instant followUpSentAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	@CreationTimestamp
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	@UpdateTimestamp
	private Instant updatedAt;

	protected InterviewRound() {
	}

	InterviewRound(
			JobApplication application,
			InterviewRoundType roundType,
			Instant scheduledAt,
			InterviewFormat format,
			InterviewOutcome outcome,
			String contactName,
			String prepNotes,
			String questionsAsked,
			Instant followUpSentAt
	) {
		this.application = application;
		this.roundType = roundType;
		this.scheduledAt = scheduledAt;
		this.format = format;
		this.outcome = outcome;
		this.contactName = contactName;
		this.prepNotes = prepNotes;
		this.questionsAsked = questionsAsked;
		this.followUpSentAt = followUpSentAt;
	}

	void replaceDetails(
			JobApplication application,
			InterviewRoundType roundType,
			Instant scheduledAt,
			InterviewFormat format,
			InterviewOutcome outcome,
			String contactName,
			String prepNotes,
			String questionsAsked,
			Instant followUpSentAt
	) {
		this.application = application;
		this.roundType = roundType;
		this.scheduledAt = scheduledAt;
		this.format = format;
		this.outcome = outcome;
		this.contactName = contactName;
		this.prepNotes = prepNotes;
		this.questionsAsked = questionsAsked;
		this.followUpSentAt = followUpSentAt;
	}

	public UUID getId() {
		return id;
	}

	public JobApplication getApplication() {
		return application;
	}

	public InterviewRoundType getRoundType() {
		return roundType;
	}

	public Instant getScheduledAt() {
		return scheduledAt;
	}

	public InterviewFormat getFormat() {
		return format;
	}

	public InterviewOutcome getOutcome() {
		return outcome;
	}

	public String getContactName() {
		return contactName;
	}

	public String getPrepNotes() {
		return prepNotes;
	}

	public String getQuestionsAsked() {
		return questionsAsked;
	}

	public Instant getFollowUpSentAt() {
		return followUpSentAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
