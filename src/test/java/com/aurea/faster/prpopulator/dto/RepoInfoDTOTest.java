package com.aurea.faster.prpopulator.dto;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class RepoInfoDTOTest {
	@Test
	public void givenTwoEqualDTOWhenHashCodeThenTrueReturn() {
		// arrange
		RepoInfoDTO j1 = new RepoInfoDTO();
		j1.setRepoUrl("url1");
		RepoInfoDTO j2 = new RepoInfoDTO();
		j2.setRepoUrl("url1");
		// act and assert
		assertEquals(true, j1.hashCode() == j2.hashCode());
	}

	@Test
	public void givenTwoEqualDTOWhenEqualsThenTrueReturn() {
		// arrange
		RepoInfoDTO j1 = new RepoInfoDTO();
		j1.setRepoUrl("url1");
		RepoInfoDTO j2 = new RepoInfoDTO();
		j2.setRepoUrl("url1");
		// act and assert
		assertEquals(true, j1.equals(j2));
	}

	@Test
	public void givenTwoUnequalDTOWhenEqualsThenReturnFalse() {
		// arrange
		RepoInfoDTO j1 = new RepoInfoDTO();
		RepoInfoDTO j2 = new RepoInfoDTO();
		j1.setRepoUrl("url1");
		// act and assert
		assertEquals(false, j1.equals(j2));
	}
}
