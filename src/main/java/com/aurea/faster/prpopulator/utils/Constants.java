package com.aurea.faster.prpopulator.utils;

import java.time.LocalDate;
import java.time.temporal.IsoFields;

public final class Constants {
	public static final LocalDate CURRENT_DATE = LocalDate.now();
	public static final int CURRENT_QUARTER = CURRENT_DATE.get(IsoFields.QUARTER_OF_YEAR);
	public static final int CURRENT_YEAR = CURRENT_DATE.getYear();

	private Constants() {
		throw new UnsupportedOperationException("Cannot be instantiated");
	}

}
