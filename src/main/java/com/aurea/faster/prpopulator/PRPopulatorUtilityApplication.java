package com.aurea.faster.prpopulator;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.aurea.faster.prpopulator.exception.BusinessException;
import com.aurea.faster.prpopulator.executor.PRPopulatorExecutor;
import com.aurea.faster.prpopulator.utils.ToolRunMode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class PRPopulatorUtilityApplication {

	private static final String JIRA_MODE = "-j";
	private static final String GITHUB_MODE = "-gh";

	public static void main(String[] args) throws Exception {
		try {
			ConfigurableApplicationContext context = SpringApplication.run(PRPopulatorUtilityApplication.class, args);
			validateArgs(args);
			ToolRunMode runMode = getToolRunMode(args);
			context.getBean(PRPopulatorExecutor.class).execute(runMode);
		} catch (RuntimeException | BusinessException excp) {
			LOGGER.error("Exception Occurred {}", excp);
			throw excp;
		}

	}

	private static ToolRunMode getToolRunMode(String[] args) {
		List<String> argList = Arrays.asList(args);
		if (argList.contains(JIRA_MODE) && argList.contains(GITHUB_MODE))
			return ToolRunMode.JIRA_GITHUB;
		else if (argList.contains(JIRA_MODE))
			return ToolRunMode.JIRA;
		else
			return ToolRunMode.GITHUB;

	}

	private static void validateArgs(String[] args) {
		if (!(Arrays.asList(args).contains(JIRA_MODE) || Arrays.asList(args).contains(GITHUB_MODE))) {
			LOGGER.error(
					"Application not started with right program arguments...Correct usage is like java -jar prpopulator-0.0.1-SNAPSHOT.jar -j");
			throw new IllegalArgumentException(
					"Application not started with right program arguments...Correct usage is like java -jar prpopulator-0.0.1-SNAPSHOT.jar -j");
		}

	}

}
