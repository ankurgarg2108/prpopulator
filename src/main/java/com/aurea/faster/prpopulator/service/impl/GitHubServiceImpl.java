package com.aurea.faster.prpopulator.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.aurea.faster.prpopulator.dto.ParsePullRequestResponseDTO;
import com.aurea.faster.prpopulator.dto.ParseRepositoryResponseDTO;
import com.aurea.faster.prpopulator.dto.ParseTeamResponseDTO;
import com.aurea.faster.prpopulator.dto.PullRequestInfoDTO;
import com.aurea.faster.prpopulator.dto.RepoInfoDTO;
import com.aurea.faster.prpopulator.dto.TeamInfoDTO;
import com.aurea.faster.prpopulator.exception.BusinessException;
import com.aurea.faster.prpopulator.processors.GithubModeProcessor;
import com.aurea.faster.prpopulator.service.GitHubService;
import com.aurea.faster.prpopulator.utils.GitHubResponseParserUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GitHubServiceImpl implements GitHubService {
	private static final String IO_EXCEPTION_OCCCURRED = "IO Exception Occcurred";

	@Value("${pr-populator.git-hub.api-request-limit}")
	private Integer maxNumberOfHitsAllowedPerRun;

	@Value("${pr-populator.git-hub.org-name}")
	private String orgName;

	@Value("${pr-populator.git-hub.api-base-path}")
	private String apiBasePath;

	@Value("${pr-populator.git-hub.depth-scan}")
	private int depthScan;

	@Value("${pr-populator.git-hub.second-between-retries}")
	private int secondBetweenRetries;

	@Value("${pr-populator.git-hub.max-retries}")
	private int maxRetries;

	@Autowired
	@Qualifier("restTemplateGithub")
	RestTemplate restTemplate;

	public void setMaxNumberOfHitsAllowedPerRun(Integer maxNumberOfHitsAllowedPerRun) {
		this.maxNumberOfHitsAllowedPerRun = maxNumberOfHitsAllowedPerRun;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public void setApiBasePath(String apiBasePath) {
		this.apiBasePath = apiBasePath;
	}

	public void setDepthScan(int depthScan) {
		this.depthScan = depthScan;
	}

	public void setSecondBetweenRetries(int secondBetweenRetries) {
		this.secondBetweenRetries = secondBetweenRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public Optional<TeamInfoDTO> findTeamUsingOrgNameAndTeamName(String orgName, String teamName)
			throws BusinessException {
		StringBuilder query = new StringBuilder(apiBasePath);
		query.append("/orgs/").append(orgName).append("/teams?per_page=100");
		Optional<TeamInfoDTO> team = Optional.empty();
		ResultDTO result = callAPI(query.toString(), 1);
		ParseTeamResponseDTO parseResponseDTO = new ParseTeamResponseDTO();
		parseResponseDTO.setTeamInfoDTO(team);
		while (result.getResponse().isPresent()) {
			try {
				parseResponseDTO = GitHubResponseParserUtil.findTeamInResponse(result.getResponse().get(), teamName,
						parseResponseDTO);
			} catch (IOException e) {
				throw new BusinessException(IO_EXCEPTION_OCCCURRED, e);
			}
			if (parseResponseDTO.isLastPage() || parseResponseDTO.getTeamInfoDTO().isPresent()) {
				break;
			} else {
				result = callAPI(parseResponseDTO.getNextPageLink(), 1);
			}
		}
		return parseResponseDTO.getTeamInfoDTO();
	}

	@Override
	public List<PullRequestInfoDTO> fetchMergedPRNewerThanDepth(String pullUrl) throws BusinessException {
		StringBuilder query = new StringBuilder(pullUrl);
		query.append("?state=all&per_page=100");
		ResultDTO result = callAPI(query.toString(), 1);
		ParsePullRequestResponseDTO parseResponseDTO = new ParsePullRequestResponseDTO();
		while (result.getResponse().isPresent()) {
			// parse all pr from the response
			try {
				parseResponseDTO = GitHubResponseParserUtil.populatePRs(result.getResponse().get(), parseResponseDTO,
						depthScan);
			} catch (IOException e) {
				throw new BusinessException(IO_EXCEPTION_OCCCURRED, e);
			}
			if (parseResponseDTO.isLastPage()) {
				break;
			} else {
				result = callAPI(parseResponseDTO.getNextPageLink(), 1);
			}
		}
		LOGGER.debug("PR Size Fetched{}", parseResponseDTO.getPrInfo().size());
		return parseResponseDTO.getPrInfo();
	}

	public ResultDTO callAPI(String query, int numberOfTries) {
		ResultDTO result = new ResultDTO();
		if (GithubModeProcessor.estimatedHits.incrementAndGet() <= maxNumberOfHitsAllowedPerRun) {
			LOGGER.debug("Rest API for hitting github {} Counter of calls {} times ", query,
					GithubModeProcessor.estimatedHits.get());
			GithubModeProcessor.executedHits.incrementAndGet();
			ResponseEntity<String> response;
			try {
				response = restTemplate.getForEntity(query, String.class);
				result.setResponse(Optional.of(response));
			} catch (RuntimeException excp) {
				LOGGER.error("Exception Occurred {} while hitting URI {}", excp, query);
				LOGGER.debug("Waiting for GitHub API throttling to let us in...");
				try {
					TimeUnit.SECONDS.sleep(secondBetweenRetries);
				} catch (InterruptedException e) {
					LOGGER.error("While sleeping someone interuupted the thread");
					Thread.currentThread().interrupt();
				}
				if (numberOfTries <= maxRetries) {
					result = callAPI(query, numberOfTries + 1);
				} else {
					LOGGER.info("Tried max retries but still github is not letting us in");
					result.setResponse(Optional.empty());
				}
			}
		} else {
			LOGGER.error("Rest API not called for hitting github {} as counter {} is above threshold {} ", query,
					GithubModeProcessor.estimatedHits.get(), maxNumberOfHitsAllowedPerRun);
			result.setResponse(Optional.empty());
			result.setMaxHitsDone(true);
		}
		return result;

	}

	@Override
	public List<RepoInfoDTO> findReposForTeamUsingFetchRepoURL(String fetchReposURL) throws BusinessException {
		fetchReposURL = fetchReposURL + "?per_page=100";
		ResultDTO result = callAPI(fetchReposURL, 1);
		ParseRepositoryResponseDTO parseResponseDTO = new ParseRepositoryResponseDTO();
		while (result.getResponse().isPresent()) {
			try {
				parseResponseDTO = GitHubResponseParserUtil.populateRepos(result.getResponse().get(), parseResponseDTO);
			} catch (IOException e) {
				throw new BusinessException(IO_EXCEPTION_OCCCURRED, e);
			}
			if (parseResponseDTO.isLastPage()) {
				break;
			} else {
				result = callAPI(parseResponseDTO.getNextPageLink(), 1);
			}
		}
		LOGGER.debug("Repo Size Fetched{}", parseResponseDTO.getListRepos().size());
		return parseResponseDTO.getListRepos();
	}

	static class ResultDTO {
		private Optional<ResponseEntity<String>> response;
		private boolean maxHitsDone;

		public Optional<ResponseEntity<String>> getResponse() {
			return response;
		}

		public void setResponse(Optional<ResponseEntity<String>> response) {
			this.response = response;
		}

		public boolean isMaxHitsDone() {
			return maxHitsDone;
		}

		public void setMaxHitsDone(boolean maxHitsDone) {
			this.maxHitsDone = maxHitsDone;
		}

	}

}
