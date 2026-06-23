package com.seregergo.careerops.jobposting;

import com.seregergo.careerops.company.Company;
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
@Table(name = "job_postings")
public class JobPosting {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(name = "source_url", length = 2048)
	private String sourceUrl;

	@Column(length = 200)
	private String location;

	@Enumerated(EnumType.STRING)
	@Column(name = "work_mode", nullable = false, length = 20)
	private WorkMode workMode;

	@Column(length = 5000)
	private String notes;

	@Column(name = "created_at", nullable = false, updatable = false)
	@CreationTimestamp
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	@UpdateTimestamp
	private Instant updatedAt;

	protected JobPosting() {
	}

	JobPosting(
			Company company,
			String title,
			String sourceUrl,
			String location,
			WorkMode workMode,
			String notes
	) {
		this.company = company;
		this.title = title;
		this.sourceUrl = sourceUrl;
		this.location = location;
		this.workMode = workMode;
		this.notes = notes;
	}

	void replaceDetails(
			Company company,
			String title,
			String sourceUrl,
			String location,
			WorkMode workMode,
			String notes
	) {
		this.company = company;
		this.title = title;
		this.sourceUrl = sourceUrl;
		this.location = location;
		this.workMode = workMode;
		this.notes = notes;
	}

	public UUID getId() {
		return id;
	}

	public Company getCompany() {
		return company;
	}

	public String getTitle() {
		return title;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public String getLocation() {
		return location;
	}

	public WorkMode getWorkMode() {
		return workMode;
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
