package com.bonitasoft.connector.jira;

import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.bonitasoft.connector.jira.configuration.JiraConfiguration;


import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by Nicolas Chabanoles on 24/09/14.
 */
public class JiraChecker {

    protected static final String CONFIGURATION_FILENAME_KEY = "org.bonitasoft.jira.configuration";

    private final JiraClient jiraClient;

    public JiraChecker() {

        JiraConfiguration config = null;
        try {
            config = new JiraConfiguration(new HasPropertyFileConfiguration(CONFIGURATION_FILENAME_KEY));
            jiraClient = new JiraClient(config);

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public JiraChecker(JiraClient client) {
        jiraClient = client;
    }


    private String buildResultList(MessageFormat[] queries, MessageFormat doubleQuery1, MessageFormat doubleQuery2, String[] versions) {
        int i = 1;
        StringBuffer sb = new StringBuffer();
        for (MessageFormat query : queries) {
            // System.out.println("Executing Query: " + i + "/" + (queries.length + 1));
            for (String version : versions) {
                int matchingResults = getResultCount(query, version);
                if (matchingResults > 0) {
                    sb.append(i).append(") ").append(version).append(" --> ").append(matchingResults).append("\n");
                }
            }
            sb.append("\n");
            i++;
        }

        // System.out.println("Executing Query: " + i + "/" + (queries.length + 1));
        for (String version : versions) {

            String matchingResults = chainQueriesUsingKeys(doubleQuery1, doubleQuery2, version);
            if (matchingResults.length() > 0) {
                sb.append(i).append(") ").append(version).append(" --> ").append(matchingResults).append("\n");
            }
        }
        sb.append("\n");

        return sb.toString();
    }

    private int getResultCount(MessageFormat query, String version) {
        SearchResult result = getSearchResult(query, version);
        return result.getTotal();
    }

    private SearchResult getSearchResult(MessageFormat query, String version) {
        final NullProgressMonitor pm = new NullProgressMonitor();
        String formattedQuery = query.format(new Object[]{version});
        //System.out.println(formattedQuery);
        return jiraClient.searchJql(formattedQuery, 1, 0, pm);
    }

    public String chainQueriesUsingKeys(MessageFormat query1, MessageFormat query2, String version) {
        final NullProgressMonitor pm = new NullProgressMonitor();
        String formattedQuery = query1.format(new Object[]{version});
        //System.out.println(formattedQuery);
        SearchResult result = jiraClient.searchJql(formattedQuery, 1000, 0, pm);
        if (result.getTotal() > 0) {
            String keysCSV = extractIssuesKey(result.getIssues());
            formattedQuery = query2.format(new Object[]{version, keysCSV});
            //System.out.println(formattedQuery);
            SearchResult result2 = jiraClient.searchJql(formattedQuery, 1000, 0, pm);

            return substract(result.getIssues(), result2.getIssues());
        }
        return "";
    }

    private String substract(Iterable<BasicIssue> all, Iterable<BasicIssue> toSubstract) {
        List<String> result = new ArrayList<String>();
        for (BasicIssue basicIssue : all) {
            result.add(basicIssue.getKey());
        }
        for (BasicIssue basicIssue : toSubstract) {
            result.remove(basicIssue.getKey());
        }
        return toCSV(result.toArray(new String[]{}));
    }

    private String extractIssuesKey(Iterable<BasicIssue> issues) {
        StringBuffer sb = new StringBuffer();
        for (BasicIssue issue : issues) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(issue.getKey());
        }
        return sb.toString();
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


    protected  String[] mergeVersions(String[] docVersions, String[] migrationVersions, String[] productVersions) {
        String[] docAndMigrationVersions = new String[docVersions.length + migrationVersions.length];
        System.arraycopy(docVersions, 0, docAndMigrationVersions, 0, docVersions.length);
        System.arraycopy(migrationVersions, 0, docAndMigrationVersions, docVersions.length, migrationVersions.length);

        String[] allVersions = new String[docAndMigrationVersions.length + productVersions.length];
        System.arraycopy(docAndMigrationVersions, 0, allVersions, 0, docAndMigrationVersions.length);
        System.arraycopy(productVersions, 0, allVersions, docAndMigrationVersions.length, productVersions.length);

        return allVersions;
    }

    protected String buildVersionsList(String[] versions) {

        StringBuffer sb = new StringBuffer();
        StringBuffer productVersionsSb = new StringBuffer();
        String previousMinorVersion = "";
        for (String version : versions) {
            String currentMinorVersion = version.substring(0, 3);
            if (currentMinorVersion.equals(previousMinorVersion)) {
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

    protected String buildVersionsList(String[] docVersions, String[] migrationVersions, String[] productVersions) {

        StringBuffer sb = new StringBuffer();
        sb.append("Doc versions:\n");
        sb.append("\t").append(toCSV(docVersions));

        sb.append("\n").append("Migration versions:\n");
        sb.append("\t").append(toCSV(migrationVersions));

        sb.append("\n").append("Product versions:");


        StringBuffer productVersionsSb = new StringBuffer();
        String previousMinorVersion = "";
        for (String version : productVersions) {
            String currentMinorVersion = version.substring(0, 3);
            if (currentMinorVersion.equals(previousMinorVersion)) {
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


    protected String buildQueriesList(String[] queryNames, MessageFormat[] queries, String doubleQueryName, MessageFormat doubleQuery1, MessageFormat doubleQuery2) {
        int i = 1;
        StringBuffer sb = new StringBuffer();
        for (MessageFormat query : queries) {
            sb.append(i).append(") ").append(queryNames[i - 1]).append("\n").append("\t").append(query.toPattern()).append("\n");
            i++;
        }
        if(doubleQueryName.length()>0) {
            sb.append(i).append(") ").append(doubleQueryName).append("\n").append("\t").append(doubleQuery1.toPattern()).append("\n").append(doubleQuery2.toPattern()).append("\n");
        }

        return sb.toString();
    }

    private void checkProductMissingMinorVersion(JiraChecker jiraChecker) {

        System.out.println("\tProduct missing minor versions:\n");

        System.out.println("\t\tQueries:\n");
        System.out.println("\t\t\t1) project = \"Bonita BPM\" AND issuetype = bug AND affectedVersion in releasedVersions() and fixVersion > 6.0.0 and fixVersion < 6.1.0 and fixVersion not in (6.1.0) and \"Issue impacting\" = \"Released Version\"\n");
        System.out.println("\t\t\t2) project = \"Bonita BPM\" AND issuetype = bug AND affectedVersion in releasedVersions() and fixVersion > 6.1.0 and fixVersion < 6.2.0 and fixVersion not in (6.2.0) and \"Issue impacting\" = \"Released Version\"\n");
        System.out.println("\t\t\t3) project = \"Bonita BPM\" AND issuetype = bug AND affectedVersion in releasedVersions() and fixVersion > 6.2.0 and fixVersion < 6.3.0 and fixVersion not in (6.3.0) and \"Issue impacting\" = \"Released Version\"\n");
        System.out.println("\t\t\t4) project = \"Bonita BPM\" AND issuetype = bug AND affectedVersion in releasedVersions() and fixVersion > 6.3.0 and fixVersion < 6.4.0 and fixVersion not in (6.4.0, 6.4.0-alpha-01) and \"Issue impacting\" = \"Released Version\"\n");

        int resultCount610 = jiraChecker.getResultCount(new MessageFormat("project = \"Bonita BPM\" AND issuetype = bug AND affectedVersion in releasedVersions() and fixVersion > 6.0.0 and fixVersion < {0} and fixVersion not in ({0}) and \"Issue impacting\" = \"Released Version\""), "6.1.0");
        int resultCount620 = jiraChecker.getResultCount(new MessageFormat("project = \"Bonita BPM\" AND issuetype = bug AND affectedVersion in releasedVersions() and fixVersion > 6.1.0 and fixVersion < {0} and fixVersion not in ({0}) and \"Issue impacting\" = \"Released Version\""), "6.2.0");
        int resultCount630 = jiraChecker.getResultCount(new MessageFormat("project = \"Bonita BPM\" AND issuetype = bug AND affectedVersion in releasedVersions() and fixVersion > 6.2.0 and fixVersion < {0} and fixVersion not in ({0}) and \"Issue impacting\" = \"Released Version\""), "6.3.0");
        int resultCount640 = jiraChecker.getResultCount(new MessageFormat("project = \"Bonita BPM\" AND issuetype = bug AND affectedVersion in releasedVersions() and fixVersion > 6.3.0 and fixVersion < {0} and fixVersion not in (6.4.0, 6.4.0-alpha-01) and \"Issue impacting\" = \"Released Version\""), "6.4.0");

        System.out.println("\t1) 6.1.0 --> " + resultCount610);
        System.out.println("\t2) 6.2.0 --> " + resultCount620);
        System.out.println("\t3) 6.3.0 --> " + resultCount630);
        System.out.println("\t4) 6.4.0 --> " + resultCount640);
    }

    private String check(String sectionName, String queryPrefix, String[] versions, String[] queryNames, List<String> queries, String doubleQueryName, String doubleQuery1 , String doubleQuery2) {
        MessageFormat form = new MessageFormat("{0} \n\n Liste des released versions: \n\n {1}\n\n " +
                "Les requetes sont: \n {2}\n\n Les requetes qui retournent des résultats sont: \n\n {3}");


        String versionsList = buildVersionsList(versions);
        MessageFormat[] fullQueries = buildQueries(queryPrefix, queries);
        MessageFormat fullQuery1 = buildQuery(queryPrefix, doubleQuery1);
        MessageFormat fullQuery2 = buildQuery(queryPrefix, doubleQuery2);
        String queriesList = buildQueriesList(queryNames, fullQueries, doubleQueryName, fullQuery1, fullQuery2);

        String queryResults = buildResultList(fullQueries, fullQuery1, fullQuery2, versions);

        System.out.println(form.format(new Object[]{sectionName, versionsList, queriesList, queryResults}));

        return form.format(new Object[]{sectionName, versionsList, queriesList, queryResults});

    }

    private MessageFormat buildQuery(String queryPrefix, String query) {
        return new MessageFormat(queryPrefix + " " + query);
    }

    private MessageFormat[] buildQueries(String queryPrefix, List<String> queries) {
        MessageFormat[] result = new MessageFormat[queries.size()];
        int i = 0;
        for (String query : queries) {
            result[i] = buildQuery(queryPrefix,query);
            i++;
        }
        return result;
    }

    public static void main(String[] args) {


        if(System.getProperty(CONFIGURATION_FILENAME_KEY) == null) {
            System.out.println("No configuration file specified using System Property: " + CONFIGURATION_FILENAME_KEY + " . Using defaults.");
            URL confFileURL = JiraChecker.class.getClassLoader().getResource("jiraChecker.cfg");
            final String filePath = confFileURL.getPath();
            System.setProperty(CONFIGURATION_FILENAME_KEY, filePath);
        }

        // Common
        JiraChecker jiraChecker = new JiraChecker();

        System.out.println("Bonjour Jira Checkers,\n\n\tNous sommes le "+new Date()+".\n\n");

        String[] queryNames = new String[]{"bugs with earliestAffectedVersion > fixVersion", "bugs with affectversion < earliestAffectedVersion", "bugs with wrong issueFiexdIn = Development", "bugs with wrong issueFiexdIn = Released version", "bugs with affectVersion > fixversion"};
        List<String> queries = Arrays.asList(" AND fixVersion = {0} AND AffectedVersion > {0}",  "AND affectedVersion = {0} AND \"Earliest Affected Version\" > {0}", " AND fixVersion = {0} AND \"Earliest Affected Version\" != {0} AND \"Issue impacting\" = Development", " AND affectedVersion >= 1.0.0 AND fixVersion = {0} AND \"Earliest Affected Version\" = {0} AND \"Issue impacting\" =  \"Released version\" ", " AND affectedVersion = {0} AND fixVersion is not EMPTY and fixVersion != \"N/A\" AND fixVersion < {0} ");

        String doubleQueryName = "missing affect version";
        String doubleQuery1 = " AND affectedVersion != {0} AND \"Earliest Affected Version\" <= {0} AND (fixVersion > {0} OR fixVersion is EMPTY OR fixVersion = \"N/A\") ORDER BY key DESC";
        String doubleQuery2 = " AND affectedVersion != {0} AND \"Earliest Affected Version\" <= {0} AND (fixVersion <= {0} and fixVersion != \"N/A\") AND key in ({1}) ORDER BY key DESC";

        // Check for documentation
        String[] versions = new String[]{"Doc-6.0", "Doc-6.1", "Doc-6.2", "Doc-6.3", "Doc-6.4"};
        String queryPrefix = "project = \"Bonita BPM\" AND issuetype in (bug) AND (resolution not in (Duplicate, \"Not a bug\", \"Cannot Reproduce\", Rejected) OR resolution is EMPTY) AND affectedVersion >= Doc-6.0 AND affectedVersion < 1.0.0";
        jiraChecker.check("Documentation Issues:", queryPrefix, versions, queryNames, queries, doubleQueryName, doubleQuery1, doubleQuery2);

        // Check for product
        versions = new String[]{"6.0.0", "6.0.1", "6.0.2", "6.0.3", "6.0.4", "6.1.0", "6.1.1", "6.1.2", "6.2.0", "6.2.1", "6.2.2", "6.2.3", "6.2.4", "6.2.5", "6.2.6", "6.3.0", "6.3.1", "6.3.2", "6.3.3", "6.3.4", "6.3.5", "6.3.6", "6.3.7"};
        queryPrefix = "project = \"Bonita BPM\" AND issuetype in (bug) AND (resolution not in (Duplicate, \"Not a bug\", \"Cannot Reproduce\", Rejected) OR resolution is EMPTY) AND affectedVersion >= 6.0.0";
        jiraChecker.check("Product Issues:", queryPrefix, versions, queryNames, queries, doubleQueryName, doubleQuery1, doubleQuery2);

        // Product - Missing minor versions
        jiraChecker.checkProductMissingMinorVersion(jiraChecker);

        // Check for migration
        versions = new String[]{"1.0.0", "1.1.0", "1.1.1", "1.1.2", "1.2.0", "1.3.0", "1.3.1", "1.4.0", "1.5.0", "1.6.0", "1.7.0", "1.8.0", "1.8.1", "1.9.0", "1.10.0", "1.11.0", "1.12.0", "1.13.0"};
        queryPrefix = "project = \"Bonita BPM\" AND issuetype in (bug) AND (resolution not in (Duplicate, \"Not a bug\", \"Cannot Reproduce\", Rejected) OR resolution is EMPTY) AND affectedVersion >= 1.0.0 AND affectedVersion < 6.0.0";
        jiraChecker.check("Product Issues:", queryPrefix, versions, queryNames, queries, doubleQueryName, doubleQuery1, doubleQuery2);

        System.out.println("\n\n\tBon nettoyage!");

    }

}
