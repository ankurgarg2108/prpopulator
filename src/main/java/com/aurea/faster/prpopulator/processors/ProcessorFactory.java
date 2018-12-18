package com.aurea.faster.prpopulator.processors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aurea.faster.prpopulator.utils.ToolRunMode;

@Component
public class ProcessorFactory {

	@Autowired
	private JIRAModeProcessor jiraModeProcessor;

	@Autowired
	private GithubModeProcessor githubModeProcessor;

	@Autowired
	private JIRAGitHubModeProcessor jiraGithubModeProcessor;

	public JIRAModeProcessor getJiraModeProcessor() {
		return jiraModeProcessor;
	}

	public void setJiraModeProcessor(JIRAModeProcessor jiraModeProcessor) {
		this.jiraModeProcessor = jiraModeProcessor;
	}

	public GithubModeProcessor getGithubModeProcessor() {
		return githubModeProcessor;
	}

	public void setGithubModeProcessor(GithubModeProcessor githubModeProcessor) {
		this.githubModeProcessor = githubModeProcessor;
	}

	public JIRAGitHubModeProcessor getJiraGithubModeProcessor() {
		return jiraGithubModeProcessor;
	}

	public void setJiraGithubModeProcessor(JIRAGitHubModeProcessor jiraGithubModeProcessor) {
		this.jiraGithubModeProcessor = jiraGithubModeProcessor;
	}

	public Processor getProcessor(ToolRunMode runmode) {
		if (runmode == ToolRunMode.JIRA)
			return jiraModeProcessor;
		else if (runmode == ToolRunMode.GITHUB)
			return githubModeProcessor;
		else
			return jiraGithubModeProcessor;
	}
}
