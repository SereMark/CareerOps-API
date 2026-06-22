package com.seregergo.careerops.company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "companies")
public class Company {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(name = "normalized_name", nullable = false, length = 200)
	private String normalizedName;

	@Column(name = "website_url", length = 2048)
	private String websiteUrl;

	@Column(length = 5000)
	private String notes;

	@Column(nullable = false)
	private boolean archived;

	@Column(name = "created_at", nullable = false, updatable = false)
	@CreationTimestamp
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	@UpdateTimestamp
	private Instant updatedAt;

	protected Company() {
	}

	Company(String name, String normalizedName, String websiteUrl, String notes) {
		this.name = name;
		this.normalizedName = normalizedName;
		this.websiteUrl = websiteUrl;
		this.notes = notes;
	}

	public void replaceDetails(String name, String normalizedName, String websiteUrl, String notes) {
		this.name = name;
		this.normalizedName = normalizedName;
		this.websiteUrl = websiteUrl;
		this.notes = notes;
	}

	public void archive() {
		archived = true;
	}

	public void restore() {
		archived = false;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public String getNotes() {
		return notes;
	}

	public boolean isArchived() {
		return archived;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
