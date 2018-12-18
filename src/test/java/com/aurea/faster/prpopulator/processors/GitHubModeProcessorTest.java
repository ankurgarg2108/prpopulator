package com.aurea.faster.prpopulator.processors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aurea.faster.prpopulator.dto.PullRequestDTO;
import com.aurea.faster.prpopulator.exception.BusinessException;
import com.aurea.faster.prpopulator.service.GitHubService;
import com.aurea.faster.prpopulator.service.GoogleSheetsService;
import com.aurea.faster.prpopulator.service.JIRAService;

public class GitHubModeProcessorTest extends BaseTest {

	@InjectMocks
	GithubModeProcessor gitHubProcessor;

	@Mock
	private JIRAService jiraService;

	@Mock
	private GitHubService gitHubService;

	@Mock
	private GoogleSheetsService googleSheetService;

	@BeforeEach
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(jiraService.getAllTicketsWithSpecifiedJQL(any(String.class))).thenReturn(setupListJIRATicketInfo());
		when(googleSheetService.listPullRequests(any(Integer.class), any(Integer.class)))
				.thenReturn(setupListPullRequestDTO());
		when(googleSheetService.readProductToURLSheet()).thenReturn(getRepoProductMap());
		gitHubProcessor.setJql("jql");
		gitHubProcessor.setTeamURL("teamurl");
		gitHubProcessor.setOrgName("OrgName");
		when(gitHubService.findTeamUsingOrgNameAndTeamName(any(String.class), any(String.class)))
				.thenReturn(getTeamInfoDTO());
		when(gitHubService.findReposForTeamUsingFetchRepoURL(any(String.class))).thenReturn(getRepoInfoDTO());
		when(gitHubService.fetchMergedPRNewerThanDepth(any(String.class))).thenReturn(getPullRequestInfoDTOList());
	}

	@Test
	public void givenGitHubProcessorWhenGetEligiblePullRequestDTOShouldReturnOnlyMergedPRsNewerThanDepth()
			throws BusinessException {
		// act
		Set<PullRequestDTO> set = gitHubProcessor.getEligiblePullRequestDTO();
		// assert
		assertEquals(2, set.size());
		List<PullRequestDTO> list = new ArrayList<>(set);
		assertEquals(100,
				list.get(0).getJiraId().equalsIgnoreCase("SL2-22331") ? list.get(1).getPrId() : list.get(0).getPrId());
		assertEquals(1000,
				list.get(0).getJiraId().equalsIgnoreCase("SL2-22331") ? list.get(0).getPrId() : list.get(1).getPrId());
	}

}
