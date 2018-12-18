package com.aurea.faster.prpopulator.service;

import java.util.List;
import java.util.Optional;

import com.aurea.faster.prpopulator.dto.PullRequestInfoDTO;
import com.aurea.faster.prpopulator.dto.RepoInfoDTO;
import com.aurea.faster.prpopulator.dto.TeamInfoDTO;
import com.aurea.faster.prpopulator.exception.BusinessException;

public interface GitHubService {

	Optional<TeamInfoDTO> findTeamUsingOrgNameAndTeamName(String orgName, String teamName) throws BusinessException;

	List<RepoInfoDTO> findReposForTeamUsingFetchRepoURL(String fetchReposURL) throws BusinessException;

	List<PullRequestInfoDTO> fetchMergedPRNewerThanDepth(String pullUrl) throws BusinessException;
}
