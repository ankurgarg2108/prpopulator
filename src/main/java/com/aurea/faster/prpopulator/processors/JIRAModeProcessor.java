package com.aurea.faster.prpopulator.processors;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.aurea.faster.prpopulator.dto.JIRATicketInfo;
import com.aurea.faster.prpopulator.dto.PullRequestDTO;
import com.aurea.faster.prpopulator.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JIRAModeProcessor extends AbstractProcessor {

	@Override
	public Set<PullRequestDTO> getEligiblePullRequestDTO() throws BusinessException {
		List<JIRATicketInfo> tickets = getEligibleJIRATickets();
		if (tickets.isEmpty())
			return Collections.emptySet();
		tickets.parallelStream().forEach(ticket -> {
			try {
				getJiraService().populateMergedPRInfo(ticket);
			} catch (BusinessException e) {
				LOGGER.error("Exception Occurred {}", e);
			}
		});
		LOGGER.debug("After Populating PRInfo {}", tickets);
		Set<PullRequestDTO> prSet = tickets.stream().map(ticket -> ticket.getMergedPRs()).flatMap(list -> list.stream())
				.collect(Collectors.toSet());
		LOGGER.debug(prSet.toString());
		LOGGER.debug("Total PRDTO fetched using JIRA mode{}", prSet.size());
		return prSet;
	}

}
