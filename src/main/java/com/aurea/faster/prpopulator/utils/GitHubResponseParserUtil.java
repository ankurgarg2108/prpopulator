package com.aurea.faster.prpopulator.utils;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import com.aurea.faster.prpopulator.dto.ParsePullRequestResponseDTO;
import com.aurea.faster.prpopulator.dto.ParseRepositoryResponseDTO;
import com.aurea.faster.prpopulator.dto.ParseResponseDTO;
import com.aurea.faster.prpopulator.dto.ParseTeamResponseDTO;
import com.aurea.faster.prpopulator.dto.PullRequestInfoDTO;
import com.aurea.faster.prpopulator.dto.RepoInfoDTO;
import com.aurea.faster.prpopulator.dto.TeamInfoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class GitHubResponseParserUtil {
	private GitHubResponseParserUtil() {
		throw new UnsupportedOperationException("Cannot be instatiated");
	}

	public static ParseTeamResponseDTO findTeamInResponse(ResponseEntity<String> responseEntity, String teamName,
			ParseTeamResponseDTO parseResponseDTO) throws IOException {
		setPaginationInfo(responseEntity, parseResponseDTO);
		// parse Body to find our team
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(responseEntity.getBody());
		root.forEach(node -> {
			if (node.get("name").asText().equalsIgnoreCase(teamName)) {
				TeamInfoDTO dto = new TeamInfoDTO();
				dto.setTeamName(teamName);
				dto.setId(node.get("id").asText());
				dto.setFetchReposURL(node.get("repositories_url").asText());
				parseResponseDTO.setTeamInfoDTO(Optional.of(dto));
			}
		});

		LOGGER.debug("Updated ParseResponseDTO is {}", parseResponseDTO);
		return parseResponseDTO;
	}

	public static ParseRepositoryResponseDTO populateRepos(ResponseEntity<String> responseEntity,
			ParseRepositoryResponseDTO parseResponseDTO) throws IOException {
		setPaginationInfo(responseEntity, parseResponseDTO);
		// parse Body to find our team
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(responseEntity.getBody());
		root.forEach(node -> {
			RepoInfoDTO repInfoDTO = new RepoInfoDTO();
			repInfoDTO.setRepoName(node.get("name").asText());
			repInfoDTO.setId(node.get("id").asText());
			repInfoDTO.setRepoUrl(node.get("html_url").asText());
			String pullurl = node.get("pulls_url").asText();
			repInfoDTO.setPullRequestURL(pullurl.replaceAll("\\{/number\\}", ""));
			parseResponseDTO.getListRepos().add(repInfoDTO);
		});

		LOGGER.debug("Updated ParseResponseDTO is {}", parseResponseDTO);
		return parseResponseDTO;
	}

	public static ParsePullRequestResponseDTO populatePRs(ResponseEntity<String> responseEntity,
			ParsePullRequestResponseDTO parseResponseDTO, int depthDays) throws IOException {
		setPaginationInfo(responseEntity, parseResponseDTO);
		// parse Body to find our team
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(responseEntity.getBody());
		Iterator<JsonNode> iterator = root.iterator();
		while (iterator.hasNext()) {
			JsonNode node = iterator.next();
			// check whether created at is less than depth mentioned
			String createdAt = node.get("created_at").asText();
			if (isPassedDateAfterCurrentDateMinusXDays(createdAt, depthDays)) {
				String mergedAt = node.get("merged_at").asText();
				if (null != mergedAt && !"null".equalsIgnoreCase(mergedAt)) {
					PullRequestInfoDTO dto = new PullRequestInfoDTO();
					dto.setCreatedAt(createdAt);
					dto.setTitle(node.get("title").asText());
					dto.setFirstComment(node.get("body").asText());
					dto.setNumber(node.get("number").asText());
					dto.setPrDetailFetchURL(node.get("url").asText());
					dto.setHtmlURL(node.get("html_url").asText());
					parseResponseDTO.getPrInfo().add(dto);
				}
			} else {
				parseResponseDTO.setLastPage(true);
				break;
			}
		}
		LOGGER.debug("Updated ParseResponseDTO is {}", parseResponseDTO);
		return parseResponseDTO;
	}

	private static void setPaginationInfo(ResponseEntity<String> responseEntity, ParseResponseDTO parseResponseDTO) {
		LOGGER.debug("Link Header {}", responseEntity.getHeaders().get("link"));
		if (!CollectionUtils.isEmpty(responseEntity.getHeaders().get("link"))) {
			String linkHeader = responseEntity.getHeaders().get("link").get(0);
			Optional<String> nextLink = Arrays.stream(linkHeader.split(",")).filter(t -> t.contains("rel=\"next\""))
					.findFirst();
			if (nextLink.isPresent()) {
				parseResponseDTO.setNextPageLink(nextLink.get().trim().replaceAll("(<)(.*)(>;.*)", "$2"));
			} else {
				parseResponseDTO.setLastPage(true);
			}
		} else {
			parseResponseDTO.setLastPage(true);
		}
	}

	public static boolean isPassedDateAfterCurrentDateMinusXDays(String dateToBeChecked, int depthDays) {
		Instant instant = Instant.parse(dateToBeChecked);
		Instant maxDateAllowed = Instant.now().minus(depthDays, ChronoUnit.DAYS);
		return instant.isAfter(maxDateAllowed);
	}

	public static Set<String> searchAllStringsInAString(String title, Set<String> issues) {
		Set<String> foundSet = new HashSet<>();
		for (String issue : issues) {
			int index = title.indexOf(issue);
			if (index != -1 && ((index + issue.length() >= title.length()) || ((index + issue.length()) < title.length()
					&& !Character.isDigit(title.charAt(index + issue.length()))))) {
				foundSet.add(issue);
			}
		}
		return foundSet;
	}
}
