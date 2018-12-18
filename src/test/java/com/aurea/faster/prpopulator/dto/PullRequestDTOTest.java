package com.aurea.faster.prpopulator.dto;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class PullRequestDTOTest {

	@Test
	public void givenTwoEqualDTOWhenHashCodeThenTrueReturn() {
		// arrange
		PullRequestDTO j1 = new PullRequestDTO();
		j1.setPrId(1);
		j1.setJiraId("A-1");
		PullRequestDTO j2 = new PullRequestDTO();
		j2.setPrId(1);
		j2.setJiraId("A-1");
		// act and assert
		assertEquals(true, j1.hashCode() == j2.hashCode());
	}

	@Test
	public void givenTwoEqualDTOWhenEqualsThenTrueReturn() {
		// arrange
		PullRequestDTO j1 = new PullRequestDTO();
		j1.setPrId(1);
		j1.setJiraId("A-1");
		PullRequestDTO j2 = new PullRequestDTO();
		j2.setPrId(1);
		j2.setJiraId("A-1");
		// act and assert
		assertEquals(true, j1.equals(j2));
	}

	@Test
	public void givenTwoUnequalDTOWhenEqualsThenReturnFalse() {
		// arrange
		PullRequestDTO j1 = new PullRequestDTO();
		PullRequestDTO j2 = new PullRequestDTO();
		j1.setPrId(1);
		j1.setJiraId("A-1");
		j2.setPrId(1);
		// act and assert
		assertEquals(false, j1.equals(j2));
	}
}
