package com.aurea.faster.prpopulator.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ParseRepositoryResponseDTO extends ParseResponseDTO {
	private List<RepoInfoDTO> listRepos = new ArrayList<>();

}
