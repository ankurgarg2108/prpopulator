package com.aurea.faster.prpopulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PullRequestDTO {
	@EqualsAndHashCode.Include
	private int prId;
	private String url;
	@EqualsAndHashCode.Include
	private String jiraId;
	private String product;
	private int quarter;
	private int year;

	public void setJiraId(String jiraId) {
		this.jiraId = jiraId;
		this.product = jiraId.substring(0, jiraId.indexOf('-'));
	}

}