package com.aurea.faster.prpopulator.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class JIRATicketInfo {
	@EqualsAndHashCode.Include
	private String key;
	private String id;
	private List<PullRequestDTO> mergedPRs = new ArrayList<>();
	private boolean mapped;
	private List<String> reposScannedForPR = new ArrayList<String>();

	public String getProductKey() {
		return getKey().substring(0, getKey().indexOf('-'));
	}
}
