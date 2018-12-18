package com.aurea.faster.prpopulator.report;

import java.util.Set;

import com.aurea.faster.prpopulator.dto.PullRequestDTO;

public interface ReportWriter {

	void writeDiscrepancyReport(Set<PullRequestDTO> jiraPRList, Set<PullRequestDTO> gitHubPRList, String filePath,
			String fileName);
}
