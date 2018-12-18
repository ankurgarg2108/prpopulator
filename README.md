#Pull Request Populator Utility

PRs populator tool is a CLI application which extracts information about Merged pull requests made for JIRA issues (found using configurable JQL filter) to the spreadsheet (to be used by Revert tool later).

From the user perspective, basically this tool has three modes.


	JIRA Mode will merged PR information from JIRA using JIRA API

	Github Mode will use Github API to fetch PR information
	
	JIRA and Github Mode will use both above mentioned modes and then also generate a file containing the discrepancies



See more datails on confluence:

https://confluence.devfactory.com/display/EN/PRs+populator%3A+DEV+documentation
