package com.aurea.faster.prpopulator.processors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.aurea.faster.prpopulator.dto.PullRequestDTO;
import com.aurea.faster.prpopulator.exception.BusinessException;
import com.aurea.faster.prpopulator.report.ReportWriter;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JIRAGitHubModeProcessor extends AbstractProcessor {

	@Autowired
	JIRAModeProcessor jiraProcessor;

	@Autowired
	GithubModeProcessor gitHubProcessor;

	@Autowired
	ReportWriter reportWriter;

	@Value("${pr-populator.diff-file.path}")
	private String filePath;

	@Value("${pr-populator.diff-file.filename.prefix}")
	private String fileNamePrefix;

	@Value("${pr-populator.diff-file.filename.date-format}")
	private String dateFormat;

	public JIRAModeProcessor getJiraProcessor() {
		return jiraProcessor;
	}

	public void setJiraProcessor(JIRAModeProcessor jiraProcessor) {
		this.jiraProcessor = jiraProcessor;
	}

	public GithubModeProcessor getGitHubProcessor() {
		return gitHubProcessor;
	}

	public void setGitHubProcessor(GithubModeProcessor gitHubProcessor) {
		this.gitHubProcessor = gitHubProcessor;
	}

	public ReportWriter getReportWriter() {
		return reportWriter;
	}

	public void setReportWriter(ReportWriter reportWriter) {
		this.reportWriter = reportWriter;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileNamePrefix() {
		return fileNamePrefix;
	}

	public void setFileNamePrefix(String fileNamePrefix) {
		this.fileNamePrefix = fileNamePrefix;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	@Override
	public Set<PullRequestDTO> getEligiblePullRequestDTO() throws BusinessException {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
		String formatDateTime = now.format(formatter);
		Set<PullRequestDTO> pullRequestList = new HashSet<>();
		Set<PullRequestDTO> jiraPRList = jiraProcessor.getEligiblePullRequestDTO();
		Set<PullRequestDTO> gitHubPRList = gitHubProcessor.getEligiblePullRequestDTO();
		pullRequestList.addAll(jiraPRList);
		pullRequestList.addAll(gitHubPRList);
		LOGGER.debug(
				"Total Pull Request that would be printed in google sheets {} with JIRA mode contributing {} and github mode contributing {}",
				pullRequestList.size(), jiraPRList.size(), gitHubPRList.size());
		if (CollectionUtils.subtract(jiraPRList, gitHubPRList).isEmpty()
				&& CollectionUtils.subtract(gitHubPRList, jiraPRList).isEmpty()) {
			LOGGER.info("No discrepancies found in JIRA and Github");
		} else {
			String fileName = fileNamePrefix + formatDateTime + ".csv";
			if (StringUtils.isEmpty(filePath))
				filePath = ".";
			reportWriter.writeDiscrepancyReport(jiraPRList, gitHubPRList, filePath, fileName);
		}
		return pullRequestList;

	}

}
