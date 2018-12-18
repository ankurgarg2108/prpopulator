package com.aurea.faster.prpopulator.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.aurea.faster.prpopulator.dto.JIRATicketInfo;
import com.aurea.faster.prpopulator.dto.PullRequestDTO;
import com.aurea.faster.prpopulator.exception.BusinessException;
import com.aurea.faster.prpopulator.service.JIRAService;
import com.aurea.faster.prpopulator.utils.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JIRAServiceImpl implements JIRAService {

	private static final String START_AT = "<startAt>";

	public static final String SEARCHAPI = "/search";

	@Value("${pr-populator.jira.instance-url:}")
	private String jiraInstanceURL;

	@Value("${pr-populator.jira.rest-api-path:}")
	private String jiraRestAPI;

	@Autowired
	@Qualifier("restTemplateJIRA")
	private RestTemplate restTemplate;

	public void setJiraInstanceURL(String jiraInstanceURL) {
		this.jiraInstanceURL = jiraInstanceURL;
	}

	public void setJiraRestAPI(String jiraRestAPI) {
		this.jiraRestAPI = jiraRestAPI;
	}

	@Override
	public List<JIRATicketInfo> getAllTicketsWithSpecifiedJQL(String jql) throws BusinessException {
		StringBuilder query = new StringBuilder();
		query.append(jiraInstanceURL).append(jiraRestAPI).append(SEARCHAPI).append("?startAt=").append(START_AT)
				.append("&fields=key,id&").append("&jql=").append(jql);
		PaginationInfo page = new PaginationInfo();
		page.setStartNumber(0);
		page.setHasMoreData(true);
		page.setDataReadSoFar(new ArrayList<JIRATicketInfo>());
		while (page.isHasMoreData())
			callAPI(query.toString(), page);
		return page.getDataReadSoFar();
	}

	public void callAPI(String query, PaginationInfo page) throws BusinessException {
		try {
			LOGGER.debug("Hitting JIRA API {}", query.replaceAll(START_AT, String.valueOf(page.getStartNumber())));
			ResponseEntity<String> response = restTemplate
					.getForEntity(query.replaceAll(START_AT, String.valueOf(page.getStartNumber())), String.class);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response.getBody());
			LOGGER.debug(root.get("startAt").asText());
			page.setMaxResults(Integer.valueOf(root.get("maxResults").asText()));
			page.setStartNumber(Integer.valueOf(root.get("startAt").asText()) + page.getMaxResults());
			page.setHasMoreData(page.getStartNumber() < Integer.valueOf(root.get("total").asText()));
			page.getDataReadSoFar().addAll(readTickets(root.get("issues")));
			LOGGER.debug("PaginationInfo {}", page.getDataReadSoFar().size());
		} catch (HttpClientErrorException e) {
			throw new BusinessException(e.getResponseBodyAsString(), e);
		} catch (RestClientException | IOException e) {
			throw new BusinessException("Querying JIRA Issues: " + query, e);
		}
	}

	private Collection<JIRATicketInfo> readTickets(JsonNode jsonNode) {
		List<JIRATicketInfo> tickets = new ArrayList<>();
		jsonNode.forEach(node -> readTicket(node).ifPresent(tickets::add));
		return tickets;
	}

	private static Optional<JIRATicketInfo> readTicket(JsonNode node) {
		String key = node.path("key").textValue();
		if (key == null) {
			return Optional.empty();
		}
		JIRATicketInfo info = new JIRATicketInfo();
		info.setKey(key);
		info.setId(node.path("id").textValue());
		return Optional.of(info);
	}

	@Override
	public void populateMergedPRInfo(JIRATicketInfo ticket) throws BusinessException {
		// https://jira.devfactory.com/rest/dev-status/1.0/issue/detail?issueId=2491225&applicationType=github&dataType=pullrequest
		StringBuilder query = new StringBuilder();
		query.append(jiraInstanceURL).append("/rest/dev-status/1.0/issue/detail").append("?issueId=")
				.append(ticket.getId()).append("&applicationType=github&dataType=pullrequest");
		LOGGER.debug("Rest API for getting PR data {}", query.toString());
		try {
			ResponseEntity<String> response = restTemplate.getForEntity(query.toString(), String.class);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response.getBody());
			populateMergedPRData(root.get("detail"), ticket);
		} catch (HttpClientErrorException e) {
			throw new BusinessException(e.getResponseBodyAsString(), e);
		} catch (RestClientException | IOException e) {
			throw new BusinessException("Querying JIRA Detail : " + query, e);
		}
	}

	private void populateMergedPRData(JsonNode root, JIRATicketInfo ticket) {
		root.forEach(detail -> detail.get("pullRequests").forEach(pr -> processPullRequest(pr, ticket)));
	}

	private void processPullRequest(JsonNode pr, JIRATicketInfo ticket) {
		String status = pr.get("status").asText();
		String key = ticket.getKey();
		if ("MERGED".equals(status)) {
			String id = pr.get("id").asText().replaceAll("#", "");
			String url = pr.get("url").asText();
			PullRequestDTO pullRequestDTO = new PullRequestDTO(Integer.valueOf(id), url, key,
					key.substring(0, key.indexOf('-')), Constants.CURRENT_QUARTER, Constants.CURRENT_YEAR);
			ticket.getMergedPRs().add(pullRequestDTO);
		}
	}

	static class PaginationInfo {
		private int startNumber;
		private boolean hasMoreData;
		private int maxResults;
		private List<JIRATicketInfo> dataReadSoFar;

		public List<JIRATicketInfo> getDataReadSoFar() {
			return dataReadSoFar;
		}

		public void setDataReadSoFar(List<JIRATicketInfo> dataReadSoFar) {
			this.dataReadSoFar = dataReadSoFar;
		}

		public int getMaxResults() {
			return maxResults;
		}

		public void setMaxResults(int maxResults) {
			this.maxResults = maxResults;
		}

		public int getStartNumber() {
			return startNumber;
		}

		public void setStartNumber(int startNumber) {
			this.startNumber = startNumber;
		}

		public boolean isHasMoreData() {
			return hasMoreData;
		}

		public void setHasMoreData(boolean hasMoreData) {
			this.hasMoreData = hasMoreData;
		}

		@Override
		public String toString() {
			return "PaginationInfo [startNumber=" + startNumber + ", hasMoreData=" + hasMoreData + ", maxResults="
					+ maxResults + ", dataReadSoFar=" + dataReadSoFar + "]";
		}
	}

}
