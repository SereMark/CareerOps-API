package com.seregergo.careerops.jobposting;

final class TriageScoring {

	private TriageScoring() {
	}

	static int totalScore(
			int roleFitScore,
			int mentoringScore,
			int salaryScore,
			int engineeringPracticesScore,
			int learningSignalScore,
			int hybridFitScore
	) {
		return roleFitScore
				+ mentoringScore
				+ salaryScore
				+ engineeringPracticesScore
				+ learningSignalScore
				+ hybridFitScore;
	}

	static TriagePriority priority(int score, String hardVetoReason) {
		if (hardVetoReason != null && !hardVetoReason.isBlank()) {
			return TriagePriority.SKIP;
		}
		if (score >= 75) {
			return TriagePriority.PRIORITIZE;
		}
		if (score >= 60) {
			return TriagePriority.APPLY;
		}
		if (score >= 45) {
			return TriagePriority.MAYBE;
		}
		return TriagePriority.SKIP;
	}
}
