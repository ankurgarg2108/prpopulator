package com.aurea.faster.prpopulator.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aurea.faster.prpopulator.dto.PullRequestDTO;

public interface GoogleSheetsService {
	List<PullRequestDTO> listPullRequests(Integer quarter, Integer year);

	Map<String, Set<String>> readProductToURLSheet();

	int writePullRequestDataToSheet(Set<PullRequestDTO> list);
}
