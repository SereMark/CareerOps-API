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

	@Enumerated(EnumType.STRING)
	@Column(name = "target_lane", nullable = false, length = 40)
	private TargetLane targetLane;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Seniority seniority;

	@Column(name = "salary_min_gross_huf")
	private Integer salaryMinGrossHuf;

	@Column(name = "salary_max_gross_huf")
	private Integer salaryMaxGrossHuf;

	@Column(name = "role_fit_score", nullable = false)
	private int roleFitScore;

	@Column(name = "mentoring_score", nullable = false)
	private int mentoringScore;

	@Column(name = "salary_score", nullable = false)
	private int salaryScore;

	@Column(name = "engineering_practices_score", nullable = false)
	private int engineeringPracticesScore;

	@Column(name = "learning_signal_score", nullable = false)
	private int learningSignalScore;

	@Column(name = "hybrid_fit_score", nullable = false)
	private int hybridFitScore;

	@Column(name = "hard_veto_reason", length = 500)
	private String hardVetoReason;

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
			TargetLane targetLane,
			Seniority seniority,
			Integer salaryMinGrossHuf,
			Integer salaryMaxGrossHuf,
			int roleFitScore,
			int mentoringScore,
			int salaryScore,
			int engineeringPracticesScore,
			int learningSignalScore,
			int hybridFitScore,
			String hardVetoReason,
			String notes
	) {
		this.company = company;
		this.title = title;
		this.sourceUrl = sourceUrl;
		this.location = location;
		this.workMode = workMode;
		this.targetLane = targetLane;
		this.seniority = seniority;
		this.salaryMinGrossHuf = salaryMinGrossHuf;
		this.salaryMaxGrossHuf = salaryMaxGrossHuf;
		this.roleFitScore = roleFitScore;
		this.mentoringScore = mentoringScore;
		this.salaryScore = salaryScore;
		this.engineeringPracticesScore = engineeringPracticesScore;
		this.learningSignalScore = learningSignalScore;
		this.hybridFitScore = hybridFitScore;
		this.hardVetoReason = hardVetoReason;
		this.notes = notes;
	}

	void replaceDetails(
			Company company,
			String title,
			String sourceUrl,
			String location,
			WorkMode workMode,
			TargetLane targetLane,
			Seniority seniority,
			Integer salaryMinGrossHuf,
			Integer salaryMaxGrossHuf,
			int roleFitScore,
			int mentoringScore,
			int salaryScore,
			int engineeringPracticesScore,
			int learningSignalScore,
			int hybridFitScore,
			String hardVetoReason,
			String notes
	) {
		this.company = company;
		this.title = title;
		this.sourceUrl = sourceUrl;
		this.location = location;
		this.workMode = workMode;
		this.targetLane = targetLane;
		this.seniority = seniority;
		this.salaryMinGrossHuf = salaryMinGrossHuf;
		this.salaryMaxGrossHuf = salaryMaxGrossHuf;
		this.roleFitScore = roleFitScore;
		this.mentoringScore = mentoringScore;
		this.salaryScore = salaryScore;
		this.engineeringPracticesScore = engineeringPracticesScore;
		this.learningSignalScore = learningSignalScore;
		this.hybridFitScore = hybridFitScore;
		this.hardVetoReason = hardVetoReason;
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

	public TargetLane getTargetLane() {
		return targetLane;
	}

	public Seniority getSeniority() {
		return seniority;
	}

	public Integer getSalaryMinGrossHuf() {
		return salaryMinGrossHuf;
	}

	public Integer getSalaryMaxGrossHuf() {
		return salaryMaxGrossHuf;
	}

	public int getRoleFitScore() {
		return roleFitScore;
	}

	public int getMentoringScore() {
		return mentoringScore;
	}

	public int getSalaryScore() {
		return salaryScore;
	}

	public int getEngineeringPracticesScore() {
		return engineeringPracticesScore;
	}

	public int getLearningSignalScore() {
		return learningSignalScore;
	}

	public int getHybridFitScore() {
		return hybridFitScore;
	}

	public String getHardVetoReason() {
		return hardVetoReason;
	}

	public int getTriageScore() {
		return TriageScoring.totalScore(
				roleFitScore,
				mentoringScore,
				salaryScore,
				engineeringPracticesScore,
				learningSignalScore,
				hybridFitScore
		);
	}

	public TriagePriority getTriagePriority() {
		return TriageScoring.priority(getTriageScore(), hardVetoReason);
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
