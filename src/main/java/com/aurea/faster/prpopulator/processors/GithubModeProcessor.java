package com.aurea.faster.prpopulator.processors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.aurea.faster.prpopulator.dto.JIRATicketInfo;
import com.aurea.faster.prpopulator.dto.PullRequestDTO;
import com.aurea.faster.prpopulator.dto.PullRequestInfoDTO;
import com.aurea.faster.prpopulator.dto.RepoInfoDTO;
import com.aurea.faster.prpopulator.dto.TeamInfoDTO;
import com.aurea.faster.prpopulator.exception.BusinessException;
import com.aurea.faster.prpopulator.service.GitHubService;
import com.aurea.faster.prpopulator.utils.Constants;
import com.aurea.faster.prpopulator.utils.GitHubResponseParserUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GithubModeProcessor extends AbstractProcessor {

	public static AtomicInteger estimatedHits = new AtomicInteger();

	public static AtomicInteger executedHits = new AtomicInteger();

	@Autowired
	private GitHubService gitHubService;

	@Value("${pr-populator.git-hub.api-request-limit}")
	private Integer maxNumberOfHitsAllowedPerRun;

	@Value("${pr-populator.git-hub.org-name}")
	private String orgName;

	@Value("${pr-populator.git-hub.team-url}")
	private String teamURL;

	@Value("${pr-populator.git-hub.api-base-path}")
	private String apiBasePath;

	private Map<String, JIRATicketInfo> ticketIDToDTOMap;

	public void setGitHubService(GitHubService gitHubService) {
		this.gitHubService = gitHubService;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public void setTeamURL(String teamURL) {
		this.teamURL = teamURL;
	}

	public void setApiBasePath(String apiBasePath) {
		this.apiBasePath = apiBasePath;
	}

	@Override
	public Set<PullRequestDTO> getEligiblePullRequestDTO() throws BusinessException {
		Set<PullRequestDTO> pullRequestList = new HashSet<>();
		List<JIRATicketInfo> tickets = getEligibleJIRATickets();
		if (tickets.isEmpty())
			return pullRequestList;
		ticketIDToDTOMap = tickets.stream().collect(Collectors.toMap(JIRATicketInfo::getKey, Function.identity()));
		Map<String, Set<String>> repoProductMapping = getGoogleSheetService().readProductToURLSheet();
		List<RepoInfoDTO> mappedRepos = populateMappedRepos(repoProductMapping);
		mappedRepos.stream().forEach(repo -> mapIssuesToRepo(tickets, repo));
		LOGGER.debug("Mapped Repos {}", mappedRepos);
		List<JIRATicketInfo> unMappedTickets = tickets.stream().filter(ticket -> !ticket.isMapped())
				.collect(Collectors.toList());
		Set<String> unmappedTicketIds = unMappedTickets.stream().map(JIRATicketInfo::getKey)
				.collect(Collectors.toSet());
		LOGGER.debug("UnMapped TicketIds {}", unmappedTicketIds);
		final List<RepoInfoDTO> unMappedRepos = new ArrayList<>();
		if (!unMappedTickets.isEmpty()) {
			populateUnMappedReposUsingTeamAPI(mappedRepos, unMappedRepos);
		}
		unMappedTickets.stream().forEach(ticket -> ticket.getReposScannedForPR()
				.addAll(unMappedRepos.stream().map(RepoInfoDTO::getRepoUrl).collect(Collectors.toList())));
		processMappedRepos(pullRequestList, mappedRepos);
		LOGGER.debug("PR From Merged Repos to be sent to Google Sheet Info{}", pullRequestList.size());
		processUnMappedRepos(pullRequestList, unmappedTicketIds, unMappedRepos);
		LOGGER.debug("Total PR's from both Repos to be sent to Google Sheet Info{}", pullRequestList.size());
		// log JIRA Tickets along with scanned repos for it with no merged PR
		logEveryTicketWithNoMergedPR(tickets);
		// log API request made Info
		logAPIRequestUsedInfo();
		return pullRequestList;

	}

	private void logAPIRequestUsedInfo() {
		int totalRequestsEstimated = estimatedHits.get();
		if (totalRequestsEstimated > maxNumberOfHitsAllowedPerRun) {
			LOGGER.info("Estimated Number of hits {} Number of hits actuallay Sent {}", estimatedHits.get(),
					executedHits.get());
		}

	}

	private void processUnMappedRepos(Set<PullRequestDTO> pullRequestList, Set<String> unmappedTicketIds,
			final List<RepoInfoDTO> unMappedRepos) {
		processRepos(unMappedRepos);
		// add all unmapped repos url as to be scanned for unmapped issues
		unMappedRepos.parallelStream().forEach(repo -> processPRInfo(repo, pullRequestList, unmappedTicketIds));
	}

	private void processRepos(final List<RepoInfoDTO> repos) {
		ForkJoinPool customPool = new ForkJoinPool(50);
		try {
			customPool.submit(() -> repos.parallelStream().forEach(this::processRepositoryForPR)).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void logEveryTicketWithNoMergedPR(List<JIRATicketInfo> tickets) {
		tickets.stream().filter(ticket -> ticket.getMergedPRs().isEmpty())
				.forEach(ticket -> LOGGER.info(
						"Ticket With Id {} has no merged PR's and repos that were scanned for this were {}",
						ticket.getKey(), ticket.getReposScannedForPR()));
	}

	private void processMappedRepos(Set<PullRequestDTO> pullRequestList, List<RepoInfoDTO> mappedRepos) {
		processRepos(mappedRepos);
		LOGGER.debug("Mapped Repo Info{}", mappedRepos);
		mappedRepos.parallelStream().forEach(repo -> processPRInfo(repo, pullRequestList, repo.getJiraIssues()));
	}

	private List<RepoInfoDTO> populateMappedRepos(Map<String, Set<String>> repoProductMapping) {
		return repoProductMapping.keySet().stream().map(repo -> {
			RepoInfoDTO repoInfo = new RepoInfoDTO(repo);
			repoInfo.setProducts(repoProductMapping.get(repo));
			repoInfo.setPullRequestURL(
					repo.replaceAll("https://github.com", "https://api.github.com/repos") + "/pulls");
			return repoInfo;
		}).collect(Collectors.toList());
	}

	private void populateUnMappedReposUsingTeamAPI(List<RepoInfoDTO> mappedRepos, List<RepoInfoDTO> unMappedRepos)
			throws BusinessException {
		String teamName = teamURL.replaceAll("https://github.com/orgs/" + orgName + "/teams/", "");
		Optional<TeamInfoDTO> teamInfoDTO = gitHubService.findTeamUsingOrgNameAndTeamName(orgName, teamName);
		if (teamInfoDTO.isPresent()) {
			LOGGER.debug("Team Info Fetched Using API {}", teamInfoDTO);
			// use this team info to get repos list
			TeamInfoDTO dto = teamInfoDTO.get();
			dto.setRepoInfo(gitHubService.findReposForTeamUsingFetchRepoURL(dto.getFetchReposURL()));
			unMappedRepos.addAll(dto.getRepoInfo().stream().filter(repo -> !mappedRepos.contains(repo))
					.collect(Collectors.toList()));
			LOGGER.debug("Unmapped repos size after exclusion{}", unMappedRepos.size());
			unMappedRepos.forEach(repo -> LOGGER.info("Unmapped Repo With URl {}", repo.getRepoUrl()));
		} else {
			throw new BusinessException("Team Information could not be found thus aborting");
		}
	}

	private void processPRInfo(RepoInfoDTO repo, Set<PullRequestDTO> pullRequestList, Set<String> issues) {
		List<PullRequestInfoDTO> prInfoList = repo.getPrList();
		if (!CollectionUtils.isEmpty(prInfoList)) {
			prInfoList.stream().forEach(prInfo -> extractAndAddJIRAInfo(prInfo, issues, pullRequestList));
		}
	}

	private void extractAndAddJIRAInfo(PullRequestInfoDTO prInfo, Set<String> issues,
			Set<PullRequestDTO> pullRequestList) {
		String title = prInfo.getTitle();
		Set<String> jiraMatched = GitHubResponseParserUtil.searchAllStringsInAString(title, issues);
		if (jiraMatched.isEmpty()) {
			String comment = prInfo.getFirstComment();
			jiraMatched = GitHubResponseParserUtil.searchAllStringsInAString(comment, issues);
		}
		if (!jiraMatched.isEmpty()) {
			jiraMatched.stream().forEach(jira -> {
				PullRequestDTO pr = getPullRequestDTO(prInfo, jira);
				ticketIDToDTOMap.get(jira).getMergedPRs().add(pr);
				pullRequestList.add(pr);
			});
		} else {
			LOGGER.debug("No JIRA issue could be associated with PR {} title {} and issues {}", prInfo.getNumber(),
					prInfo.getTitle(), issues);
		}
	}

	private PullRequestDTO getPullRequestDTO(PullRequestInfoDTO prInfo, String jiraId) {
		PullRequestDTO pr = new PullRequestDTO();
		pr.setJiraId(jiraId);
		pr.setQuarter(Constants.CURRENT_QUARTER);
		pr.setYear(Constants.CURRENT_YEAR);
		pr.setUrl(prInfo.getHtmlURL());
		pr.setPrId(Integer.parseInt(prInfo.getNumber()));
		return pr;
	}

	private void processRepositoryForPR(RepoInfoDTO repo) {
		LOGGER.debug("Processing Repository{}", repo.getRepoUrl());
		try {
			repo.getPrList().addAll(gitHubService.fetchMergedPRNewerThanDepth(repo.getPullRequestURL()));
		} catch (BusinessException e) {
			throw new RuntimeException(e);
		}
	}

	private void mapIssuesToRepo(List<JIRATicketInfo> tickets, RepoInfoDTO repo) {
		Set<String> products = repo.getProducts();
		repo.setJiraIssues(tickets.stream().filter(ticket -> {
			if (products.contains(ticket.getProductKey())) {
				ticket.getReposScannedForPR().add(repo.getRepoUrl());
				ticket.setMapped(true);
				return true;
			}
			return false;
		}).map(JIRATicketInfo::getKey).collect(Collectors.toSet()));

	}

}
