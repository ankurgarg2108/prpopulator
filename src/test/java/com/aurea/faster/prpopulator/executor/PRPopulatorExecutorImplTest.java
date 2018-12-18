package com.aurea.faster.prpopulator.executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.aurea.faster.prpopulator.processors.GithubModeProcessor;
import com.aurea.faster.prpopulator.processors.JIRAGitHubModeProcessor;
import com.aurea.faster.prpopulator.processors.JIRAModeProcessor;
import com.aurea.faster.prpopulator.processors.ProcessorFactory;
import com.aurea.faster.prpopulator.utils.ToolRunMode;

public class PRPopulatorExecutorImplTest {

	@InjectMocks
	PRPopulatorExecutorImpl sut;

	@Mock
	ProcessorFactory factory;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);

	}

	@Test
	public void givenPRPopulatorExecutorWhenExecuteWithJIRAModeThenJIRAModeProcessorProcessShouldBeInvoked()
			throws Exception {
		// arrange
		JIRAModeProcessor processor = Mockito.mock(JIRAModeProcessor.class);
		Mockito.when(factory.getProcessor(ToolRunMode.JIRA)).thenReturn(processor);
		// act
		sut.execute(ToolRunMode.JIRA);
		// assert
		Mockito.verify(processor, Mockito.times(1)).process();
	}

	@Test
	public void givenPRPopulatorExecutorWhenExecuteWithGitHibModeThenGithubModeProcessorProcessShouldBeInvoked()
			throws Exception {
		// arrange
		GithubModeProcessor processor = Mockito.mock(GithubModeProcessor.class);
		Mockito.when(factory.getProcessor(ToolRunMode.GITHUB)).thenReturn(processor);
		// act
		sut.execute(ToolRunMode.GITHUB);
		// assert
		Mockito.verify(processor, Mockito.times(1)).process();
	}

	@Test
	public void givenPRPopulatorExecutorWhenExecuteWithJIRAGITHUBModeThenCobmibedModeProcessorProcessShouldBeInvoked()
			throws Exception {
		// arrange
		JIRAGitHubModeProcessor processor = Mockito.mock(JIRAGitHubModeProcessor.class);
		Mockito.when(factory.getProcessor(ToolRunMode.JIRA_GITHUB)).thenReturn(processor);
		// act
		sut.execute(ToolRunMode.JIRA_GITHUB);
		// assert
		Mockito.verify(processor, Mockito.times(1)).process();
	}
}
