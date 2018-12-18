package com.aurea.faster.prpopulator.dto;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class TeamInfoDTOTest {
	@Test
	public void givenTwoEqualDTOWhenHashCodeThenTrueReturn() {
		// arrange
		TeamInfoDTO j1 = new TeamInfoDTO();
		j1.setId("A-1");
		TeamInfoDTO j2 = new TeamInfoDTO();
		j2.setId("A-1");
		// act and assert
		assertEquals(true, j1.hashCode() == j2.hashCode());
	}

	@Test
	public void givenTwoEqualDTOWhenEqualsThenTrueReturn() {
		// arrange
		TeamInfoDTO j1 = new TeamInfoDTO();
		j1.setId("A-1");
		TeamInfoDTO j2 = new TeamInfoDTO();
		j2.setId("A-1");
		// act and assert
		assertEquals(true, j1.equals(j2));
	}
}
