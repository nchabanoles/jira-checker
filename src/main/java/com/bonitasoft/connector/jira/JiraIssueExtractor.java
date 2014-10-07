/**
 * 
 */
package com.bonitasoft.connector.jira;

import java.sql.SQLException;

import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.bonitasoft.connector.jira.model.PersistentIssue;

/**
 * @author Nicolas Chabanoles
 * 
 *         TODO: Change class to servlet
 *         TODO: Build War 
 *         TODO: get the list of projects directly from Jira
 *         TODO: Store issues in CSV using toString on an Issue
 * 
 *         Done: Use REST to extract issues
 *         Done: Store issues in DB using JDBC
 *         Done: Store issues in DB using Hibernate
 */
public class JiraIssueExtractor {


	private final IssueStorage issueStorage;
	private JiraClient jiraClient;


	public JiraIssueExtractor(JiraClient client, IssueStorage storage) {
		this.jiraClient = client;
		issueStorage = storage;
	}

	public int extractEpicStories(final String epicIssueId) throws SQLException { 
		String jqlQuery = "'Epic Link' = "+epicIssueId;
		System.out.println("Extracting issues of query='" + jqlQuery + "'");
		int nbExtracted = extractPagesFromSearchResult(jqlQuery);
		System.out.println("Extracted Issues: " + nbExtracted);
		return nbExtracted;
		
	}

	private void extractIssuePage(final NullProgressMonitor pm, Iterable<BasicIssue> issues)
			throws SQLException {
		Issue issue;
		for (BasicIssue basicIssue : issues) {
			issue = jiraClient.getIssue(basicIssue.getKey(), pm);
			issueStorage.store(new PersistentIssue(issue));
			extractPagesFromSearchResult("parent in (" + basicIssue.getKey() + ")");
		}
	}
	

	private int extractPagesFromSearchResult(String jqlQuery)
			throws SQLException {
		int startIndex = 0;
		int pageSize = jiraClient.getSearchResultPageSize();
		final NullProgressMonitor pm = new NullProgressMonitor();


		SearchResult result;
		Iterable<BasicIssue> issues;
		do {
			result = jiraClient.searchJql(jqlQuery, pageSize, startIndex, pm);
			
			issues = result.getIssues();
			extractIssuePage(pm, issues);
			startIndex = startIndex + pageSize;
		} while (startIndex < result.getTotal());

		return result.getTotal();
	}

	public int extractSprintTasks(String sprintName) throws SQLException{
		String jqlQuery = "Sprint = "+sprintName;
		System.out.println("Extracting issues of query='" + jqlQuery + "'");
		int nbExtracted = extractPagesFromSearchResult(jqlQuery);
		System.out.println("Extracted Issues: " + nbExtracted);
		return nbExtracted;
	}


    public int countResult(String jqlQuery) {
        final NullProgressMonitor pm = new NullProgressMonitor();
        SearchResult result = jiraClient.searchJql(jqlQuery, 1, 0, pm);
        return result.getTotal();
    }
}
