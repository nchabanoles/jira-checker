/**
 * 
 */
package com.bonitasoft.connector.jira.configuration;

import com.bonitasoft.connector.jira.HasPropertyFileConfiguration;

/**
 * @author Nicolas Chabanoles
 *
 */
public class JiraConfiguration {

	protected static final String JIRA_URL_KEY = "jira.url"; // https://bonitasoft.atlassian.net
	protected static final String JIRA_USERNAME_KEY = "jira.username";
	protected static final String JIRA_PASSWORD_KEY = "jira.password";
	protected static final String JIRA_SEARCH_PAGE_SIZE_KEY = "jira.search.result.page.size";
	
	private String url;
	private String username;
	private String password;
	private int searchResultPageSize;

	public JiraConfiguration(HasPropertyFileConfiguration props) {
		final String jiraUrl = props.getProperty(JIRA_URL_KEY);
		final String username = props.getProperty(JIRA_USERNAME_KEY);
		final String password = props.getProperty(JIRA_PASSWORD_KEY);
		final String searchResultPageSize = props.getProperty(JIRA_SEARCH_PAGE_SIZE_KEY);
		
		if (jiraUrl == null) {
			throw new IllegalStateException("Jira URL not set in configuration! Please specify the " + JIRA_URL_KEY + " property.");
		}
		if (username == null) {
			throw new IllegalStateException("Jira Username not set in configuration! Please specify the " + JIRA_USERNAME_KEY
					+ " property.");
		}
		if (password == null) {
			throw new IllegalStateException("Jira password not set in configuration! Please specify the " + JIRA_PASSWORD_KEY
					+ " property.");
		}
		if (searchResultPageSize == null) {
			throw new IllegalStateException("Jira search result page size not set in configuration! Please specify the " + JIRA_SEARCH_PAGE_SIZE_KEY
					+ " property.");
		} 
		setUrl(jiraUrl);
		setUsername(username);
		setPassword(password);
		setSearchResultPageSize(Integer.parseInt(searchResultPageSize));
		
	}
	
	public JiraConfiguration(String url, String username, String password) {
		this.setUrl(url);
		this.setUsername(username);
		this.setPassword(password);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setSearchResultPageSize(int size) {
		this.searchResultPageSize = size;
	}

	public int getSearchResultPageSize() {
		return this.searchResultPageSize;
	}
}
