/**
 * 
 */
package com.bonitasoft.connector.jira;

import java.net.URL;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.connector.jira.configuration.JiraConfiguration;
import com.bonitasoft.connector.jira.csv.ConsoleCSVStorage;

/**
 * @author Nicolas Chabanoles
 *
 */
public class JiraIssueExtractorTest {

	protected static final String CONFIGURATION_FILENAME_KEY = "org.bonitasoft.jira.configuration";
	private JiraIssueExtractor jiraIssueExtractor;
	
	@Before
	public void setup() throws Exception {
		ensureValidConfiguration();
		IssueStorage storage = new ConsoleCSVStorage();
		JiraConfiguration config = new JiraConfiguration(new HasPropertyFileConfiguration(CONFIGURATION_FILENAME_KEY));
		JiraClient client = new JiraClient(config);
		jiraIssueExtractor = new JiraIssueExtractor(client, storage);
	}
	
	public static void ensureValidConfiguration() {
		URL confFileURL = JiraIssueExtractorTest.class.getClassLoader().getResource("jiraExtractor.cfg");
		final String filePath = confFileURL.getPath();
		System.setProperty(CONFIGURATION_FILENAME_KEY, filePath);
	}

//	@Test
//	public void extractStudioRefactoringEpic() throws Exception {
//		String epicIssueId = "BS-1827";
//		int nbStories = jiraIssueExtractor.extractEpicStories(epicIssueId);
//		Assertions.assertThat(nbStories).isGreaterThan(0);
//	}
	
	/*
	 * Portal L&F
	 */
//	@Test
//	public void extractPortalLFEpic() throws Exception {
//		String epicIssueId = "BS-1828";
//		int nbStories = jiraIssueExtractor.extractEpicStories(epicIssueId);
//		Assertions.assertThat(nbStories).isGreaterThan(0);
//	}
	
//	@Test
//	public void extractProductMigrationEpic() throws Exception {
//		String epicIssueId = "BS-1823";
//		int nbStories = jiraIssueExtractor.extractEpicStories(epicIssueId);
//		Assertions.assertThat(nbStories).isGreaterThan(0);
//	}
	
//	@Test
//	public void extractSprintCoreProduct() throws Exception {
//		String sprintName = "'CP-End refactoring+L&F'";
//		int nbStories = jiraIssueExtractor.extractSprintTasks(sprintName);
//		Assertions.assertThat(nbStories).isGreaterThan(0);
//	}



    @Test
    public void extractIssuesFromQuery() {
        String jqlQuery = "project = \"Bonita BPM\" AND issuetype in (bug) AND summary !~ CLONE AND resolution not in (\"Cannot Reproduce\", Duplicate, \"Not a bug\", Rejected) AND affectedVersion >= 6.0.0 AND (fixVersion > 6.2.4 OR fixVersion is EMPTY OR fixVersion = \"N/A\") AND \"Earliest Affected Version\" <= 6.2.4 AND affectedVersion != 6.2.4";

        int matchingResult = jiraIssueExtractor.countResult(jqlQuery);
        Assertions.assertThat(matchingResult).isEqualTo(862);
    }
}
