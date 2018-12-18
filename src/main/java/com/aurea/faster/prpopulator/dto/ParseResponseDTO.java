package com.aurea.faster.prpopulator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ParseResponseDTO {
	private String nextPageLink;
	private boolean isLastPage;
}
