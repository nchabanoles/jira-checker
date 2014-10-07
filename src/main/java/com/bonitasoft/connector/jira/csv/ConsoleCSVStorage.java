package com.bonitasoft.connector.jira.csv;

import com.bonitasoft.connector.jira.IssueStorage;
import com.bonitasoft.connector.jira.model.PersistentIssue;

public class ConsoleCSVStorage implements IssueStorage {

	@Override
	public void store(PersistentIssue issue) {
		System.out.println(issue.toString());

	}
}
