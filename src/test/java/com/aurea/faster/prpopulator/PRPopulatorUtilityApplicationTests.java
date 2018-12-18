package com.aurea.faster.prpopulator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PRPopulatorUtilityApplicationTests {

	@Test(expected = IllegalArgumentException.class)
	public void givenPRPopulatorUtilityApplicationWhenMainWithoutModeFlagsThenIllegalArgumentExceptionThrown()
			throws Exception {
		PRPopulatorUtilityApplication.main(new String[] {});
	}

}
