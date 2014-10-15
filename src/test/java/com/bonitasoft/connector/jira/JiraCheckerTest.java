package com.bonitasoft.connector.jira;

import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Nicolas Chabanoles on 07/10/14.
 */
public class JiraCheckerTest {


    @Test
    public void shouldSeparateDocVersionsFromProductVersions() {
        JiraChecker jiraChecker = new JiraChecker();
        String[] docVersions = new String[]{"DOC-60"};
        String[] migrationVersions = new String[]{"1.0.0","1.1.0"};
        String[] productVersions = new String[]{"6.0"};
        String versionsList = jiraChecker.buildVersionsList(docVersions, migrationVersions, productVersions);

        assertEquals("Versions list is not well formatted",
                "Doc versions:\n\tDOC-60\nMigration versions:\n\t1.0.0,1.1.0\nProduct versions:\n\t6.0", versionsList);
    }

    @Test
    public void shouldMinorProductVersionsBeSeparatedByNewLine() {
        JiraChecker jiraChecker = new JiraChecker();
        String[] docVersions = new String[]{};
        String[] migrationVersions = new String[]{};
        String[] productVersions = new String[]{"6.0.0", "6.0.2", "6.2.0",};
        String versionsList = jiraChecker.buildVersionsList(docVersions, migrationVersions, productVersions);

        assertEquals("Versions list is not well formatted",
                "Doc versions:\n\t\nMigration versions:\n\t\nProduct versions:\n\t6.0.0,6.0.2\n\t6.2.0", versionsList);
    }


    @Test
    public void shouldQueriesHaveNameAndJPQL() {
        JiraChecker jiraChecker = new JiraChecker();
        String[] queryNames = new String[]{"Missing affected"};
        MessageFormat[] queries = new MessageFormat[]{new MessageFormat("project=\"Bonita BPM\"")};
        String queriesList = jiraChecker.buildQueriesList(queryNames, queries, "", new MessageFormat(""), new MessageFormat(""));

        assertEquals("Queries List is not well formatted","1) Missing affected\n\tproject=\"Bonita BPM\"\n", queriesList);
    }



    @Test
    public void shouldMergeVersionsConcatenateArrays() {
        JiraChecker jiraChecker = new JiraChecker();
        String[] docVersions = new String[]{"DOC-O"};
        String[] migrationVersions = new String[]{"1.0.0", "1.1.0"};
        String[] productVersions = new String[]{"6.0.0", "6.0.2", "6.2.0"};
        String[] versions = jiraChecker.mergeVersions(docVersions,migrationVersions,productVersions);
        String[] expected = new String[]{"DOC-O", "1.0.0", "1.1.0", "6.0.0", "6.0.2", "6.2.0"};
        assertArrayEquals("Version not merged properly!", expected, versions);

    }



    @Test
    public void shouldUseResponseAsInputOfSecondQuery() throws URISyntaxException {
        // Given
        JiraClient mockClient = Mockito.mock(JiraClient.class);
        JiraChecker jiraChecker = new JiraChecker(mockClient);

        String version = "6.0.1";

        // First query from which we need to retrieve keys
        List<BasicIssue> query1MatchingIssues = Arrays.asList(new BasicIssue(new URI("self"), "BS-07"), new BasicIssue(new URI("self"), "BS-100"));
        MessageFormat query1 = new MessageFormat("query 1 for version {0}");
        String query1Formatted = "query 1 for version " + version;
        SearchResult query1Result = new SearchResult(1,1000,10,query1MatchingIssues);
        Mockito.when(mockClient.searchJql(Mockito.eq(query1Formatted),Mockito.eq(1000),Mockito.eq(0),Mockito.any(NullProgressMonitor.class))).thenReturn(query1Result);


        // Second query parametrized with key BS-07
        List<BasicIssue> query2MatchingIssues = Arrays.asList(new BasicIssue(new URI("self"), "BS-100"));
        MessageFormat query2 = new MessageFormat("query 2 with version {0} and keys in ({1})");
        String query2Formatted = "query 2 with version "+version+" and keys in (BS-07,BS-100)";
        SearchResult query2Result = new SearchResult(1,1000,10,query2MatchingIssues);
        Mockito.when(mockClient.searchJql(Mockito.eq(query2Formatted),Mockito.eq(1000),Mockito.eq(0),Mockito.any(NullProgressMonitor.class))).thenReturn(query2Result);


        // When
        String keysCSV = jiraChecker.chainQueriesUsingKeys(query1, query2, version);

        // Then
        assertEquals("BS-07",keysCSV);
    }
}
