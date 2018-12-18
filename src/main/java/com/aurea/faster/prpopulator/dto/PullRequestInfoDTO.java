package com.aurea.faster.prpopulator.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PullRequestInfoDTO {
	private String title;
	@ToString.Exclude
	private String firstComment;
	@ToString.Exclude
	private String createdAt;
	@EqualsAndHashCode.Include
	private String number;
	@ToString.Exclude
	private String prDetailFetchURL;
	@ToString.Exclude
	private String htmlURL;
}
