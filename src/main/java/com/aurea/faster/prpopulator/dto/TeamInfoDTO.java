package com.aurea.faster.prpopulator.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TeamInfoDTO {
	private String teamName;
	@EqualsAndHashCode.Include
	private String id;
	private String fetchReposURL;
	private List<RepoInfoDTO> repoInfo;
}
