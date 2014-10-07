package com.bonitasoft.connector.jira;

import org.junit.Test;

import java.text.MessageFormat;

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
        String queriesList = jiraChecker.buildQueriesList(queryNames, queries);

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
}
