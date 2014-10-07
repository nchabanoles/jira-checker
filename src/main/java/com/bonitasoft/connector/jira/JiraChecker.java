package com.bonitasoft.connector.jira;

import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.bonitasoft.connector.jira.configuration.JiraConfiguration;
import com.bonitasoft.connector.jira.csv.ConsoleCSVStorage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;

/**
 * Created by Nicolas Chabanoles on 24/09/14.
 */
public class JiraChecker {

    protected static final String CONFIGURATION_FILENAME_KEY = "org.bonitasoft.jira.configuration";

    private final JiraClient jiraClient;

    public JiraChecker() {
        ensureValidConfiguration();

        JiraConfiguration config = null;
        try {
            config = new JiraConfiguration(new HasPropertyFileConfiguration(CONFIGURATION_FILENAME_KEY));
            jiraClient = new JiraClient(config);

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }


    private void ensureValidConfiguration() {
        URL confFileURL = JiraChecker.class.getClassLoader().getResource("jiraExtractor.cfg");
        final String filePath = confFileURL.getPath();
        System.setProperty(CONFIGURATION_FILENAME_KEY, filePath);
    }

    private String buildResultList(MessageFormat[] queries, String[] versions) {
        int i = 1;
        StringBuffer sb = new StringBuffer();
        for (MessageFormat query : queries) {
            System.out.println("Executing Query: " + i + "/" + queries.length);
            for (String version : versions) {
                int matchingResults = getResult(query, version);
                if (matchingResults > 0) {
                    sb.append(i).append(") ").append(version).append(" --> ").append(matchingResults).append("\n");
                }
            }
            sb.append("\n");
            i++;
        }
        return sb.toString();
    }

    private int getResult(MessageFormat query, String version) {
        final NullProgressMonitor pm = new NullProgressMonitor();
        SearchResult result = jiraClient.searchJql(query.format(new Object[]{version}), 1, 0, pm);
        return result.getTotal();
        //return jiraIssueExtractor.countResult(query.format(new Object[]{version}));
    }

    private String toCSV(String[] versions) {
        StringBuffer sb = new StringBuffer();
        for (String version : versions) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(version);
        }
        return sb.toString();
    }

    private void check(String[] docVersions, String[] migrationVersions, String[] productVersions, String[] queryNames, MessageFormat[] queries) {
        MessageFormat form = new MessageFormat("Bonjour Jira Checkers, \n\n Nous sommes le {0}. \n\n Liste des released versions: \n\n {1}\n\n " +
                "Les requetes sont: \n {2}\n\n Les requetes qui retournent des résultats sont: \n\n {3} \n\n Bon nettoyage!");

        Date now = new Date();

        String versionsList = buildVersionsList(docVersions, migrationVersions, productVersions);

        String queriesList = buildQueriesList(queryNames, queries);

        String[] versions = mergeVersions(docVersions, migrationVersions, productVersions);


        String resultList = buildResultList(queries, versions);

        String body = form.format(new Object[]{now, versionsList, queriesList, resultList});

        System.err.println(body);
    }

    String[] mergeVersions(String[] docVersions, String[] migrationVersions, String[] productVersions) {
        String[] docAndMigrationVersions = new String[docVersions.length + migrationVersions.length];
        System.arraycopy(docVersions, 0, docAndMigrationVersions, 0, docVersions.length);
        System.arraycopy(migrationVersions, 0, docAndMigrationVersions, docVersions.length, migrationVersions.length);

        String[] allVersions = new String[docAndMigrationVersions.length + productVersions.length];
        System.arraycopy(docAndMigrationVersions, 0, allVersions, 0, docAndMigrationVersions.length);
        System.arraycopy(productVersions, 0, allVersions, docAndMigrationVersions.length, productVersions.length);

        return allVersions;
    }

    protected String buildVersionsList(String[] docVersions, String[] migrationVersions, String[] productVersions) {

        StringBuffer sb = new StringBuffer();
        sb.append("Doc versions:\n");
        sb.append("\t").append(toCSV(docVersions));

        sb.append("\n").append("Migration versions:\n");
        sb.append("\t").append(toCSV(migrationVersions));

        sb.append("\n").append("Product versions:");


        StringBuffer productVersionsSb = new StringBuffer();
        String previousMinorVersion="";
        for (String version : productVersions) {
            String currentMinorVersion = version.substring(0,3);
            if(currentMinorVersion.equals(previousMinorVersion)) {
                productVersionsSb.append(",");
            } else {
                productVersionsSb.append("\n\t");
                previousMinorVersion = currentMinorVersion;
            }
            productVersionsSb.append(version);
        }

        sb.append(productVersionsSb);

        return sb.toString();

    }


    protected String buildQueriesList(String[] queryNames, MessageFormat[] queries) {
        int i = 1;
        StringBuffer sb = new StringBuffer();
        for (MessageFormat query : queries) {
            sb.append(i).append(") ").append(queryNames[i-1]).append("\n").append("\t").append(query.toPattern()).append("\n");
            i++;
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        //,"6.4"
        String[] queryNames = new String[]{"missing affect version", "bugs with earliestAffectedVersion > fixVersion", "bugs with affectversion < earliestAffectedVersion", "bugs with wrong issueFiexdIn = Development", "bugs with wrong issueFiexdIn = Released version", "bugs with affectVersion > fixversion"};

        String[] docVersions = new String[]{"Doc-6.0","Doc-6.1","Doc-6.2","Doc-6.3","Doc-6.4"};
        String[] migrationVersions = new String[]{"1.0.0","1.1.0","1.1.1","1.1.2","1.2.0","1.3.0","1.3.1","1.4.0","1.5.0","1.6.0","1.7.0","1.8.0","1.8.1","1.9.0","1.10.0","1.11.0","1.12.0","1.13.0"};
        String[] productVersions = new String[]{"6.0.0","6.0.1","6.0.2","6.0.3","6.0.4","6.1.0","6.1.1","6.1.2","6.2.0","6.2.1","6.2.2","6.2.3","6.2.4","6.2.5","6.2.6","6.3.0","6.3.1","6.3.2","6.3.3","6.3.4","6.3.5","6.3.6","6.3.7"};


        MessageFormat[] queries = new MessageFormat[]{
                new MessageFormat("project = \"Bonita BPM\" AND issuetype in (bug) AND (resolution not in (Duplicate, \"Not a bug\", \"Cannot Reproduce\", Rejected) OR resolution is EMPTY) AND affectedVersion >= Doc-6.0 AND (fixVersion > {0} OR fixVersion is EMPTY OR fixVersion = \"N/A\") AND \"Earliest Affected Version\" <= {0} AND affectedVersion != {0}")
                ,
                new MessageFormat("project = \"Bonita BPM\" AND issuetype in (bug) AND (resolution not in (Duplicate, \"Not a bug\", \"Cannot Reproduce\", Rejected) OR resolution is EMPTY) AND affectedVersion >= Doc-6.0 AND fixVersion = {0} AND AffectedVersion > {0}")
                ,
                new MessageFormat("project = \"Bonita BPM\" AND issuetype in (bug) AND (resolution not in (Duplicate, \"Not a bug\", \"Cannot Reproduce\", Rejected) OR resolution is EMPTY) AND affectedVersion >= Doc-6.0 AND affectedVersion = {0} AND \"Earliest Affected Version\" > {0}")
                ,
                new MessageFormat("project = \"Bonita BPM\" AND issuetype in (bug) AND (resolution not in (Duplicate, \"Not a bug\", \"Cannot Reproduce\", Rejected) OR resolution is EMPTY) AND affectedVersion >= Doc-6.0 AND fixVersion = {0} AND \"Earliest Affected Version\" != {0} AND \"Issue impacting\" = Development")
                ,
                new MessageFormat("project = \"Bonita BPM\" AND issuetype in (bug) AND (resolution not in (Duplicate, \"Not a bug\", \"Cannot Reproduce\", Rejected) OR resolution is EMPTY) AND affectedVersion >= Doc-6.0 AND fixVersion = {0} AND \"Earliest Affected Version\" = {0} AND \"Issue impacting\" =  \"Released version\" ")
                ,
                new MessageFormat("project = \"Bonita BPM\" AND issuetype in (bug) AND (resolution not in (Duplicate, \"Not a bug\", \"Cannot Reproduce\", Rejected) OR resolution is EMPTY) AND affectedVersion >= Doc-6.0 AND affectedVersion = {0} AND fixVersion is not EMPTY and fixVersion != \"N/A\" AND ((fixVersion = {0} AND \"Issue impacting\" != \"Development\") OR fixVersion < {0}) ")
        };

        new JiraChecker().check(docVersions, migrationVersions, productVersions, queryNames, queries);
    }


}
