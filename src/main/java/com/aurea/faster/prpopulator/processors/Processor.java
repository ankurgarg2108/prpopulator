package com.aurea.faster.prpopulator.processors;

import java.util.Set;

import com.aurea.faster.prpopulator.dto.PullRequestDTO;
import com.aurea.faster.prpopulator.exception.BusinessException;

public interface Processor {
	public void process() throws BusinessException;

	Set<PullRequestDTO> getEligiblePullRequestDTO() throws BusinessException;

	void writePullRequestDataToSheet(Set<PullRequestDTO> dtoList) throws BusinessException;
}
