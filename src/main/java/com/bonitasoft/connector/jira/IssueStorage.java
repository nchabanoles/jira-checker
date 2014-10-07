/**
 * 
 */
package com.bonitasoft.connector.jira;

import com.bonitasoft.connector.jira.model.PersistentIssue;

/**
 * @author Nicolas Chabanoles
 *
 */
public interface IssueStorage {

	void store(PersistentIssue issue);


}
