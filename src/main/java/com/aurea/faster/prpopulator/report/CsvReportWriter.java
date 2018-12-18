package com.aurea.faster.prpopulator.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import com.aurea.faster.prpopulator.dto.PullRequestDTO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CsvReportWriter implements ReportWriter {

	private static final String DISCREPANCY_REPORT_HEADER = "<Issue_ID>, <PR_ID>, <GitHub/JIRA (where data found)>";
	private static final String LINE_SEPERATOR = System.lineSeparator();

	@Override
	public void writeDiscrepancyReport(Set<PullRequestDTO> jiraPRList, Set<PullRequestDTO> gitHubPRList,
			String filePath, String fileName) {

		try (FileWriter fileWriter = new FileWriter(filePath + File.separator + fileName)) {
			fileWriter.append(DISCREPANCY_REPORT_HEADER);
			fileWriter.append(LINE_SEPERATOR);
			CollectionUtils.subtract(jiraPRList, gitHubPRList).forEach(dto -> printDTOToCSV(dto, "JIRA", fileWriter));
			CollectionUtils.subtract(gitHubPRList, jiraPRList).forEach(dto -> printDTOToCSV(dto, "GITHUB", fileWriter));
			fileWriter.flush();
		} catch (IOException | RuntimeException e) {
			LOGGER.error("Exception Occurred while printing diff report", e);
		}

	}

	private void printDTOToCSV(PullRequestDTO dto, String mode, FileWriter fileWriter) {
		try {
			fileWriter.append(dto.getJiraId());
			fileWriter.append(',');
			fileWriter.append(String.valueOf(dto.getPrId()));
			fileWriter.append(',');
			fileWriter.append(mode);
			fileWriter.append(',');
			fileWriter.append(LINE_SEPERATOR);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
