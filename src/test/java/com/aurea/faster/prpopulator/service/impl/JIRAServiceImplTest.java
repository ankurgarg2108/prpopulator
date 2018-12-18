package com.aurea.faster.prpopulator.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.aurea.faster.prpopulator.dto.JIRATicketInfo;
import com.aurea.faster.prpopulator.exception.BusinessException;
import com.aurea.faster.prpopulator.processors.BaseTest;

public class JIRAServiceImplTest extends BaseTest {

	@InjectMocks
	JIRAServiceImpl jiraService;

	@Mock
	RestTemplate template;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		jiraService.setJiraInstanceURL("InstanceUrl");
		jiraService.setJiraRestAPI("apiBasePath");

	}

	@Test
	public void givenJiraAPiReturnRecordsWhenGetAllTicketsWithSpecifiedJQLThenReturnListOfJiraTickets()
			throws Exception {
		// arrange
		final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		ResponseEntity<String> r = new ResponseEntity<String>(LIST_JIRA_RESPONSE, headers, HttpStatus.ACCEPTED);
		Mockito.when(template.getForEntity(Mockito.any(String.class), Mockito.any(Class.class))).thenReturn(r);
		// act
		List<JIRATicketInfo> ticketList = jiraService.getAllTicketsWithSpecifiedJQL("someJql");
		// assert
		assertEquals(true, !ticketList.isEmpty());
		assertEquals("XOWS-2527", ticketList.get(0).getKey());
		assertEquals("XOC-4330", ticketList.get(1).getKey());
	}

	@Test
	public void givenJiraAPIThrowsHttpClientErrorExceptionWhenGetAllTicketsWithSpecifiedJQLThenBusinessExceptionThrown()
			throws Exception {
		// arrange
		HttpClientErrorException clientException = new HttpClientErrorException(HttpStatus.BAD_GATEWAY,
				"SomeException");
		Mockito.when(template.getForEntity(Mockito.any(String.class), Mockito.any(Class.class)))
				.thenThrow(clientException);
		// act and assert
		Assertions.assertThrows(BusinessException.class, () -> jiraService.getAllTicketsWithSpecifiedJQL("someJql"));
		;

	}

	@Test
	public void givenJiraAPIThrowsRestClientExceptionWhenGetAllTicketsWithSpecifiedJQLThenBusinessExceptionThrown()
			throws Exception {
		// arrange
		Mockito.when(template.getForEntity(Mockito.any(String.class), Mockito.any(Class.class)))
				.thenThrow(RestClientException.class);
		// act and assert
		Assertions.assertThrows(BusinessException.class, () -> jiraService.getAllTicketsWithSpecifiedJQL("someJql"));
	}

	@Test
	public void givenTicketInfoWhenPopulateMergedPRInfoThenPRInformationShouldBePopulated() throws Exception {
		// arrange
		final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		JIRATicketInfo jiraTicket = getJIraTicketInfo();
		ResponseEntity<String> r = new ResponseEntity<String>(LIST_JIRA_PR_REPSONSE, headers, HttpStatus.ACCEPTED);
		Mockito.when(template.getForEntity(Mockito.any(String.class), Mockito.any(Class.class))).thenReturn(r);
		// act
		jiraService.populateMergedPRInfo(jiraTicket);
		// assert
		assertEquals(true, !jiraTicket.getMergedPRs().isEmpty());
		assertEquals(2674, jiraTicket.getMergedPRs().get(0).getPrId());
	}

	@Test
	public void givenJiraAPIThrowsHttpClientErrorExceptionWhenPopulateMergedPRInfoThenBusinessExceptionThrown()
			throws Exception {
		// arrange
		HttpClientErrorException clientException = new HttpClientErrorException(HttpStatus.BAD_GATEWAY,
				"SomeException");
		Mockito.when(template.getForEntity(Mockito.any(String.class), Mockito.any(Class.class)))
				.thenThrow(clientException);
		JIRATicketInfo jiraTicket = getJIraTicketInfo();
		// act and assert
		Assertions.assertThrows(BusinessException.class, () -> jiraService.populateMergedPRInfo(jiraTicket));
		;

	}

	@Test
	public void givenJiraAPIThrowsRestClientExceptionWhenPopulateMergedPRInfoThenBusinessExceptionThrown()
			throws Exception {
		// arrange
		Mockito.when(template.getForEntity(Mockito.any(String.class), Mockito.any(Class.class)))
				.thenThrow(RestClientException.class);
		JIRATicketInfo jiraTicket = getJIraTicketInfo();
		// act and assert
		Assertions.assertThrows(BusinessException.class, () -> jiraService.populateMergedPRInfo(jiraTicket));
	}

	private JIRATicketInfo getJIraTicketInfo() {
		JIRATicketInfo jiraTicket = new JIRATicketInfo();
		jiraTicket.setKey("A-1");
		jiraTicket.setId("1");
		return jiraTicket;
	}
}
