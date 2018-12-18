package com.aurea.faster.prpopulator.utils;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.aurea.faster.prpopulator.dto.ParsePullRequestResponseDTO;
import com.aurea.faster.prpopulator.dto.ParseRepositoryResponseDTO;
import com.aurea.faster.prpopulator.dto.ParseTeamResponseDTO;
import com.aurea.faster.prpopulator.processors.BaseTest;

public class GitHubResponseParserUtilTest extends BaseTest {

	@Test
	public void givenDateLessThanXDaysWhenIsPassedDateAfterCurrentDateMinusXDaysThenReturnFalse() {
		// act and assert
		assertEquals(false, GitHubResponseParserUtil
				.isPassedDateAfterCurrentDateMinusXDays(Instant.now().minus(12, ChronoUnit.DAYS).toString(), 10));
	}

	@Test
	public void givenResponseAndTeamNameWhenFindTeamInResponseThenVerifyReturnValue() throws Exception {
		// arrange
		final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("link",
				"<https://api.github.com/repositories/71002246/pulls?status=all&per_page=10&page=2>; rel=\"next\", <https://api.github.com/repositories/71002246/pulls?status=all&per_page=10&page=3>; rel=\"last\"");
		ResponseEntity r = new ResponseEntity(LIST_TEAM_RESPONSE, headers, HttpStatus.ACCEPTED);
		ParseTeamResponseDTO dto = new ParseTeamResponseDTO();
		String teamName = "aurea-crm-reviewers";
		// act
		GitHubResponseParserUtil.findTeamInResponse(r, teamName, dto);
		assertEquals(false, dto.isLastPage());
		// assert
		assertEquals(true, dto.getTeamInfoDTO().isPresent());
		assertEquals(teamName, dto.getTeamInfoDTO().get().getTeamName());
	}

	@Test
	public void givenResponseWhenPopulateReposThenVerifyReturnValue() throws Exception {
		// arrange
		final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		ResponseEntity r = new ResponseEntity(LIST_REPOS_RESPONSE, headers, HttpStatus.ACCEPTED);
		ParseRepositoryResponseDTO dto = new ParseRepositoryResponseDTO();
		// act
		GitHubResponseParserUtil.populateRepos(r, dto);
		// assert
		assertEquals(true, dto.isLastPage());
		assertEquals(2, dto.getListRepos().size());
	}

	@Test
	public void givenResponseWhenPopulatePRsThenVerifyReturnValue() throws Exception {
		// arrange
		final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		ResponseEntity r = new ResponseEntity(LIST_PRS_RESPONSE, headers, HttpStatus.ACCEPTED);
		ParsePullRequestResponseDTO dto = new ParsePullRequestResponseDTO();
		// act
		GitHubResponseParserUtil.populatePRs(r, dto, 10000);
		// assert
		assertEquals(true, dto.isLastPage());
		assertEquals(1, dto.getPrInfo().size());
	}
}
