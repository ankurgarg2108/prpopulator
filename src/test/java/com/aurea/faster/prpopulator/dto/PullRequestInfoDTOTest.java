package com.aurea.faster.prpopulator.dto;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class PullRequestInfoDTOTest {

	@Test
	public void givenTwoEqualDTOWhenHashCodeThenTrueReturn() {
		// arrange
		PullRequestInfoDTO j1 = new PullRequestInfoDTO();
		j1.setNumber("100");
		PullRequestInfoDTO j2 = new PullRequestInfoDTO();
		j2.setNumber("100");
		// act and assert
		assertEquals(true, j1.hashCode() == j2.hashCode());
	}

	@Test
	public void givenTwoEqualDTOWhenEqualsThenTrueReturn() {
		// arrange
		PullRequestInfoDTO j1 = new PullRequestInfoDTO();
		j1.setNumber("100");
		PullRequestInfoDTO j2 = new PullRequestInfoDTO();
		j2.setNumber("100");
		// act and assert
		assertEquals(true, j1.equals(j2));
	}

	@Test
	public void givenTwoUnequalDTOWhenEqualsThenReturnFalse() {
		// arrange
		PullRequestInfoDTO j1 = new PullRequestInfoDTO();
		PullRequestInfoDTO j2 = new PullRequestInfoDTO();
		j2.setNumber("100");
		// act and assert
		assertEquals(false, j1.equals(j2));
	}
}
