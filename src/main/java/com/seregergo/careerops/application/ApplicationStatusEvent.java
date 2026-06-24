package com.seregergo.careerops.application;

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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_status_events")
public class ApplicationStatusEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "application_id", nullable = false)
	private JobApplication application;

	@Enumerated(EnumType.STRING)
	@Column(name = "previous_status", length = 30)
	private ApplicationStatus previousStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "new_status", nullable = false, length = 30)
	private ApplicationStatus newStatus;

	@Column(length = 2000)
	private String note;

	@Column(name = "occurred_at", nullable = false, updatable = false)
	@CreationTimestamp
	private Instant occurredAt;

	protected ApplicationStatusEvent() {
	}

	ApplicationStatusEvent(
			JobApplication application,
			ApplicationStatus previousStatus,
			ApplicationStatus newStatus,
			String note
	) {
		this.application = application;
		this.previousStatus = previousStatus;
		this.newStatus = newStatus;
		this.note = note;
	}

	public UUID getId() {
		return id;
	}

	public ApplicationStatus getPreviousStatus() {
		return previousStatus;
	}

	public ApplicationStatus getNewStatus() {
		return newStatus;
	}

	public String getNote() {
		return note;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}
}
