package com.aurea.faster.prpopulator.processors;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aurea.faster.prpopulator.utils.ToolRunMode;

public class ProcessorFactoryTest {

	@InjectMocks
	ProcessorFactory factory;

	@Mock
	private JIRAModeProcessor jiraModeProcessor;

	@Mock
	private GithubModeProcessor githubModeProcessor;

	@Mock
	private JIRAGitHubModeProcessor jiraGithubModeProcessor;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void givenProcessFactoryWhenGetProcessorThenVerifyProcessorType() {
		// Act
		Assert.assertTrue(factory.getProcessor(ToolRunMode.JIRA) == jiraModeProcessor);
		Assert.assertTrue(factory.getProcessor(ToolRunMode.GITHUB) == githubModeProcessor);
		Assert.assertTrue(factory.getProcessor(ToolRunMode.JIRA_GITHUB) == jiraGithubModeProcessor);
	}
}
