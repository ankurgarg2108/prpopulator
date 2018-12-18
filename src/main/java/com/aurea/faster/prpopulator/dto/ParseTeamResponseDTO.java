package com.aurea.faster.prpopulator.dto;

import java.util.Optional;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ParseTeamResponseDTO extends ParseResponseDTO {
	Optional<TeamInfoDTO> teamInfoDTO;
}
