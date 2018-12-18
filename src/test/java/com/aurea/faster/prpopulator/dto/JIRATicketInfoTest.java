package com.aurea.faster.prpopulator.dto;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class JIRATicketInfoTest {

	@Test
	public void givenTwoEqualDTOWhenHashCodeThenTrueReturn() {
		// arrange
		JIRATicketInfo j1 = new JIRATicketInfo();
		j1.setKey("A-1");
		JIRATicketInfo j2 = new JIRATicketInfo();
		j2.setKey("A-1");
		// act and assert
		assertEquals(true, j1.hashCode() == j2.hashCode());
	}

	@Test
	public void givenTwoEqualDTOWhenEqualsThenTrueReturn() {
		// arrange
		JIRATicketInfo j1 = new JIRATicketInfo();
		j1.setKey("A-1");
		JIRATicketInfo j2 = new JIRATicketInfo();
		j2.setKey("A-1");
		// act and assert
		assertEquals(true, j1.equals(j2));
	}
}
