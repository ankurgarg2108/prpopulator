package com.aurea.faster.prpopulator.processors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.aurea.faster.prpopulator.dto.PullRequestDTO;
import com.aurea.faster.prpopulator.exception.BusinessException;
import com.aurea.faster.prpopulator.report.ReportWriter;
import com.aurea.faster.prpopulator.service.GitHubService;
import com.aurea.faster.prpopulator.service.GoogleSheetsService;
import com.aurea.faster.prpopulator.service.JIRAService;

public class JIRAGithubModeProcessorTest extends BaseTest {

	@InjectMocks
	JIRAGitHubModeProcessor processor;

	@Mock
	private JIRAService jiraService;

	@Mock
	private GitHubService gitHubService;

	@Mock
	private GoogleSheetsService googleSheetService;

	@Mock
	private JIRAModeProcessor jiraProcessor;

	@Mock
	private GithubModeProcessor gitProcessor;

	@Mock
	ReportWriter writer;

	@BeforeEach
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(jiraService.getAllTicketsWithSpecifiedJQL(any(String.class))).thenReturn(setupListJIRATicketInfo());
		when(googleSheetService.listPullRequests(any(Integer.class), any(Integer.class)))
				.thenReturn(setupListPullRequestDTO());
		when(googleSheetService.readProductToURLSheet()).thenReturn(getRepoProductMap());
		gitProcessor.setJql("jql");
		gitProcessor.setTeamURL("teamurl");
		gitProcessor.setOrgName("OrgName");
		when(gitHubService.findTeamUsingOrgNameAndTeamName(any(String.class), any(String.class)))
				.thenReturn(getTeamInfoDTO());
		when(gitHubService.findReposForTeamUsingFetchRepoURL(any(String.class))).thenReturn(getRepoInfoDTO());
		when(gitHubService.fetchMergedPRNewerThanDepth(any(String.class))).thenReturn(getPullRequestInfoDTOList());
		processor.setDateFormat("hh-mm-ss");
		processor.setFileNamePrefix("prefix");
		when(jiraProcessor.getEligiblePullRequestDTO()).thenReturn(new HashSet<>(setupListPullRequestDTO()));
		when(gitProcessor.getEligiblePullRequestDTO()).thenReturn(new HashSet<>(setupListPullRequestDTO()));
	}

	@Test
	public void givenJIRAGitHubProcessorWhenGetEligiblePullRequestDTOShouldReturnCorrectPullRequestDTO()
			throws BusinessException {
		// arrange
		when(gitProcessor.getEligiblePullRequestDTO()).thenReturn(new HashSet<>());
		// act
		Set<PullRequestDTO> set = processor.getEligiblePullRequestDTO();
		// assert
		assertEquals(6, set.size());
		Mockito.verify(writer, Mockito.times(1)).writeDiscrepancyReport(any(Set.class), any(Set.class),
				any(String.class), any(String.class));
	}

	@Test
	public void givenBothProcessorsReturnSameSetWhenGetEligiblePullRequestDTOShouldNotInvokeReportWriter()
			throws BusinessException {
		// act
		Set<PullRequestDTO> set = processor.getEligiblePullRequestDTO();
		// assert
		assertEquals(6, set.size());
		Mockito.verify(writer, Mockito.times(0)).writeDiscrepancyReport(any(Set.class), any(Set.class),
				any(String.class), any(String.class));
	}
}
