package com.bonitasoft.connector.jira;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.bonitasoft.connector.jira.configuration.JiraConfiguration;

public class JiraClient {

	private JiraConfiguration config;
	
	private JiraRestClient restClient;

	public JiraClient(JiraConfiguration config) throws URISyntaxException {
		this.config = config;
		restClient = buildJiraClient(config);
	}

	

	protected JiraRestClient buildJiraClient(final JiraConfiguration jiraConfiguration) throws URISyntaxException {
		final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
		final URI jiraServerUri = new URI(jiraConfiguration.getUrl());
		final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, jiraConfiguration.getUsername(),
				jiraConfiguration.getPassword());
		return restClient;
	}

	public int getSearchResultPageSize() {
		return config.getSearchResultPageSize();
	}



	public SearchResult searchJql(String jqlQuery, int pageSize,
			int startIndex, NullProgressMonitor pm) {
		return restClient.getSearchClient().searchJql(jqlQuery, pageSize, startIndex, pm);
	}



	public Issue getIssue(String key, NullProgressMonitor pm) {
		return restClient.getIssueClient().getIssue(key, pm);
	}

}
