package com.aurea.faster.prpopulator.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.aurea.faster.prpopulator.dto.PullRequestInfoDTO;
import com.aurea.faster.prpopulator.dto.RepoInfoDTO;
import com.aurea.faster.prpopulator.dto.TeamInfoDTO;
import com.aurea.faster.prpopulator.exception.BusinessException;
import com.aurea.faster.prpopulator.processors.BaseTest;
import com.aurea.faster.prpopulator.service.impl.GitHubServiceImpl.ResultDTO;

public class GitHubServiceImplTest extends BaseTest {

	@InjectMocks
	GitHubServiceImpl gitHubService;

	@Mock
	RestTemplate template;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		gitHubService.setApiBasePath("api.com");
		gitHubService.setDepthScan(10);
		gitHubService.setMaxRetries(5);
		gitHubService.setOrgName("org");
		gitHubService.setSecondBetweenRetries(5);
		gitHubService.setMaxNumberOfHitsAllowedPerRun(100);
	}

	@Test
	public void givenCorrectOrgNameTeamNameWhenFindTeamUsingOrgNameAndTeamNameThenReturnTeamInfoDTO() throws Exception {
		// arrange
		final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("link",
				"<https://api.github.com/repositories/71002246/pulls?status=all&per_page=10&page=2>; rel=\"next\", <https://api.github.com/repositories/71002246/pulls?status=all&per_page=10&page=3>; rel=\"last\"");
		ResponseEntity r = new ResponseEntity(LIST_TEAM_RESPONSE, headers, HttpStatus.ACCEPTED);
		Mockito.when(template.getForEntity(Mockito.any(String.class), Mockito.any(Class.class))).thenReturn(r);
		// act
		Optional<TeamInfoDTO> teamInfo = gitHubService.findTeamUsingOrgNameAndTeamName("org", "aurea-crm-reviewers");
		// assert
		assertEquals(true, teamInfo.isPresent());
		assertEquals("aurea-crm-reviewers", teamInfo.get().getTeamName());
	}

	@Test
	public void givenFetchRepoURLWhenFindReposForTeamUsingFetchRepoURLThenReturnReposForThatTeam() throws Exception {
		// arrange
		final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		ResponseEntity r = new ResponseEntity(LIST_REPOS_RESPONSE, headers, HttpStatus.ACCEPTED);
		Mockito.when(template.getForEntity(Mockito.any(String.class), Mockito.any(Class.class))).thenReturn(r);
		// act
		List<RepoInfoDTO> repoInfo = gitHubService.findReposForTeamUsingFetchRepoURL("anyUrl");
		// assert
		assertEquals(2, repoInfo.size());
		assertEquals("Dnn.Evoq.Social", repoInfo.get(0).getRepoName());
		assertEquals("Dnn.Evoq.Content", repoInfo.get(1).getRepoName());

	}

	@Test
	public void givenBadResponseFromGithubWhenFindReposForTeamUsingFetchRepoURLThenExceptionShouldBeThrown()
			throws Exception {
		// arrange
		final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		ResponseEntity r = new ResponseEntity("Corrupted", headers, HttpStatus.ACCEPTED);
		Mockito.when(template.getForEntity(Mockito.any(String.class), Mockito.any(Class.class))).thenReturn(r);
		// act
		Assertions.assertThrows(BusinessException.class,
				() -> gitHubService.findReposForTeamUsingFetchRepoURL("anyUrl"));
		// assert
	}

	@Test
	public void givenHttpClientErrorExceptionFromGithubAPIWhenFindReposForTeamUsingFetchRepoURLThenExceptionShouldBeThrown()
			throws Exception {
		// arrange
		final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		ResponseEntity r = new ResponseEntity("Corrupted", headers, HttpStatus.ACCEPTED);
		Mockito.when(template.getForEntity(Mockito.any(String.class), Mockito.any(Class.class))).thenReturn(r);
		// act
		Assertions.assertThrows(BusinessException.class,
				() -> gitHubService.findReposForTeamUsingFetchRepoURL("anyUrl"));
		// assert
	}

	@Test
	public void givenWhenThresholdIsZeroWhenCallAPIThenVerifyEmptyResponse() throws Exception {
		// arrange
		Mockito.when(template.getForEntity(Mockito.any(String.class), Mockito.any(Class.class)))
				.thenThrow(HttpClientErrorException.class);
		// act
		ResultDTO result = gitHubService.callAPI("query", 6);
		// assert
		assertEquals(false, result.getResponse().isPresent());
	}

	@Test
	public void givenWhenGitHubAPIThrowsHttpClientErrorExceptionWhenCallAPIThenVerifyEmptyResponse() throws Exception {
		// arrange
		gitHubService.setMaxNumberOfHitsAllowedPerRun(0);
		// act
		ResultDTO result = gitHubService.callAPI("query", 1);
		// assert
		assertEquals(false, result.getResponse().isPresent());
		assertEquals(true, result.isMaxHitsDone());
	}

	@Test
	public void givenPullURLForRepoWhenFetchMergedPRNewerThanDepthThenReturnPRsForTheRepo() throws Exception {
		// arrange
		final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		ResponseEntity r = new ResponseEntity(LIST_PRS_RESPONSE, headers, HttpStatus.ACCEPTED);
		Mockito.when(template.getForEntity(Mockito.any(String.class), Mockito.any(Class.class))).thenReturn(r);
		// act
		List<PullRequestInfoDTO> prDTO = gitHubService.fetchMergedPRNewerThanDepth("anyUrl");
		// assert
		assertEquals(1, prDTO.size());
		assertEquals("1620", prDTO.get(0).getNumber());
	}
}
