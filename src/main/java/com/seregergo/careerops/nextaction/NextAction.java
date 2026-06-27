package com.seregergo.careerops.nextaction;

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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "next_actions")
public class NextAction {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "application_id", nullable = false)
	private JobApplication application;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private NextActionType type;

	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate;

	@Column(name = "completed_at")
	private Instant completedAt;

	@Column(length = 2000)
	private String notes;

	@Column(name = "created_at", nullable = false, updatable = false)
	@CreationTimestamp
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	@UpdateTimestamp
	private Instant updatedAt;

	protected NextAction() {
	}

	NextAction(
			JobApplication application,
			NextActionType type,
			LocalDate dueDate,
			String notes
	) {
		this.application = application;
		this.type = type;
		this.dueDate = dueDate;
		this.notes = notes;
	}

	void replaceDetails(
			JobApplication application,
			NextActionType type,
			LocalDate dueDate,
			String notes
	) {
		this.application = application;
		this.type = type;
		this.dueDate = dueDate;
		this.notes = notes;
	}

	void complete(Instant completedAt) {
		if (this.completedAt == null) {
			this.completedAt = completedAt;
		}
	}

	public UUID getId() {
		return id;
	}

	public JobApplication getApplication() {
		return application;
	}

	public NextActionType getType() {
		return type;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public Instant getCompletedAt() {
		return completedAt;
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
