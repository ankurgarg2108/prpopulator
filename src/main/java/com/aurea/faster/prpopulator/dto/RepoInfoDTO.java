package com.aurea.faster.prpopulator.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RepoInfoDTO {

	@Include
	private String repoUrl;
	private String repoName;
	private String id;
	private String pullRequestURL;
	private List<PullRequestInfoDTO> prList = new ArrayList<>();
	private Set<String> jiraIssues = new HashSet<>();
	private Set<String> products = new HashSet<>();

	public RepoInfoDTO() {
	}

	public RepoInfoDTO(String url) {
		this.repoUrl = url;
	}
}
