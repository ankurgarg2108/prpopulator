package com.aurea.faster.prpopulator.processors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.aurea.faster.prpopulator.dto.JIRATicketInfo;
import com.aurea.faster.prpopulator.dto.PullRequestDTO;
import com.aurea.faster.prpopulator.exception.BusinessException;
import com.aurea.faster.prpopulator.service.GoogleSheetsService;
import com.aurea.faster.prpopulator.service.JIRAService;

public class JIRAModeProcessorTest extends BaseTest {
	
	@InjectMocks
	JIRAModeProcessor jiraProcessor;

	@Mock
	private JIRAService jiraService;

	@Mock
	private GoogleSheetsService googleSheetService;

	@BeforeEach
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(jiraService.getAllTicketsWithSpecifiedJQL(any(String.class))).thenReturn(setupListJIRATicketInfo());
		Mockito.doAnswer((Answer) invocation -> {
			JIRATicketInfo ticketInfo = (JIRATicketInfo) invocation.getArgument(0);
			ticketInfo.setMergedPRs(setupListPullRequestDTO());
			return null;
		}).when(jiraService).populateMergedPRInfo(any(JIRATicketInfo.class));
		when(googleSheetService.listPullRequests(any(Integer.class), any(Integer.class)))
				.thenReturn(setupListPullRequestDTO());
		jiraProcessor.setJql("jql");

	}

	@Test
	public void givenJIRAProcessorWhenGetEligiblePullRequestDTOShouldReturnOnlyMergedPRs() throws BusinessException {
		// act
		Set<PullRequestDTO> set = jiraProcessor.getEligiblePullRequestDTO();
		// assert
		assertEquals(6, set.size());
		assertEquals(true, set.containsAll(setupListPullRequestDTO()));
	}

}
