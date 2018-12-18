package com.aurea.faster.prpopulator.processors;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.aurea.faster.prpopulator.dto.JIRATicketInfo;
import com.aurea.faster.prpopulator.dto.PullRequestDTO;
import com.aurea.faster.prpopulator.exception.BusinessException;
import com.aurea.faster.prpopulator.service.GoogleSheetsService;
import com.aurea.faster.prpopulator.service.JIRAService;
import com.aurea.faster.prpopulator.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractProcessor implements Processor {

	@Value("${pr-populator.jira.jql-filter-query:}")
	private String jql;

	@Autowired
	private JIRAService jiraService;

	@Autowired
	private GoogleSheetsService googleSheetService;

	public String getJql() {
		return jql;
	}

	public void setJql(String jql) {
		this.jql = jql;
	}

	public JIRAService getJiraService() {
		return jiraService;
	}

	public void setJiraService(JIRAService jiraService) {
		this.jiraService = jiraService;
	}

	public GoogleSheetsService getGoogleSheetService() {
		return googleSheetService;
	}

	public void setGoogleSheetService(GoogleSheetsService googleSheetService) {
		this.googleSheetService = googleSheetService;
	}

	public void process() throws BusinessException {
		Set<PullRequestDTO> pullRequestDTOList = getEligiblePullRequestDTO();
		LOGGER.debug("Pull Request Data that will be written to Google Sheets has size {}", pullRequestDTOList.size());
		writePullRequestDataToSheet(pullRequestDTOList);
	}

	public void writePullRequestDataToSheet(Set<PullRequestDTO> dtoList) throws BusinessException {
		googleSheetService.writePullRequestDataToSheet(dtoList);
	}

	public List<JIRATicketInfo> getEligibleJIRATickets() throws BusinessException {
		String quarterParam = "'" + "Q" + Constants.CURRENT_QUARTER + " " + Constants.CURRENT_YEAR + "'";
		List<JIRATicketInfo> tickets = jiraService
				.getAllTicketsWithSpecifiedJQL(jql.replaceAll("<quarter>", quarterParam));
		if (!tickets.isEmpty()) {
			LOGGER.debug("Total JIRA Tickets fetched using JQL {}", tickets.size());
			List<PullRequestDTO> prDTOList = googleSheetService.listPullRequests(Constants.CURRENT_QUARTER,
					Constants.CURRENT_YEAR);
			List<String> jiraKeys = prDTOList.stream().map(PullRequestDTO::getJiraId).collect(Collectors.toList());
			LOGGER.debug("Fetched PR's info {}", prDTOList);
			tickets = tickets.stream().filter(ticket -> !jiraKeys.contains(ticket.getKey()))
					.collect(Collectors.toList());
			LOGGER.debug("Tickets After filtering size {}", tickets.size());
		}
		return tickets;
	}
}
