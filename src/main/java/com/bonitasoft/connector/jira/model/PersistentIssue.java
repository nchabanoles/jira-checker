/**
 * 
 */
package com.bonitasoft.connector.jira.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.NamedEntity;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Subtask;

/**
 * @author Nicolas Chabanoles
 * 
 */
public class PersistentIssue {

	private static final int DEFAULT_INITIAL_CAPACITY = 5;
	private static JSONArray SPRINT_KEYS;
	static {
		try {
			SPRINT_KEYS = new JSONArray("[value]");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private Set<String> affectedVersions;
	private String assignee;
	private Set<String> components;
	private String creationDate;
	private String description;
	private String dueDate;
	private Set<String> expandos;
	private Map<String, String> fields;
	private Set<String> fixVersions;
	private String issueType;
	private String key;
	private Set<String> labels;
	private String priority;
	private Integer estimation;
	private Integer remaining;
	private Integer timeSpent;
	private String updateDate;
	private String project;
	private String reporter;
	private String resolution;
	private String status;
	private String summary;
	private String sprint;
	private String parent;

	public PersistentIssue(Issue model) {
		this.affectedVersions = listNames(model.getAffectedVersions());
		this.assignee = getName(model.getAssignee());
		this.components = listNames(model.getComponents());
		this.creationDate = getStringOrNull(model.getCreationDate());
		this.description = model.getDescription();
		this.dueDate = getStringOrNull(model.getDueDate());
		this.expandos = toSet(model.getExpandos());
		this.fields = toMap(model.getFields());
		this.fixVersions = listNames(model.getFixVersions());
		this.issueType = getName(model.getIssueType());
		this.key = model.getKey();
		this.labels = model.getLabels();
		this.priority = getName(model.getPriority());
		this.project = getName(model.getProject());
		this.reporter = getName(model.getReporter());
		this.resolution = getName(model.getResolution());
		this.status = getName(model.getStatus());
		this.summary = model.getSummary();
		this.estimation = model.getTimeTracking().getOriginalEstimateMinutes();
		this.remaining = model.getTimeTracking().getRemainingEstimateMinutes();
		this.timeSpent = model.getTimeTracking().getTimeSpentMinutes();
		this.updateDate = getStringOrNull(model.getUpdateDate());
		Field field = model.getFieldByName("Parent");
		this.parent = getCustomFieldAttribute(field, "key");

		// Extract meaningful fields
		this.sprint = getSprint(model.getFieldByName("Sprint"));
	}

	private String getSprint(Field fieldByName) {
		if (fieldByName == null || fieldByName.getValue() == null) {
			return null;
		} else {
			
			try {
				String originalString = (String)((JSONArray) fieldByName.getValue()).get(0);
				int startingPosition = originalString.indexOf("name=");
				final String stringStartingWithSprintName = originalString.substring(startingPosition + "name=".length());
				int endingPosition = stringStartingWithSprintName.indexOf(",");
				return stringStartingWithSprintName.substring(0, endingPosition);
			} catch (JSONException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	private String getCustomFieldAttribute(Field fieldByName, String attribute) {
		if (fieldByName == null || fieldByName.getValue() == null) {
			return "";
		} else {
			
			try {
				return ((JSONObject) fieldByName.getValue()).getString(attribute);
			} catch (JSONException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	private Set<String> toSet(Iterable<String> expandos) {
		if (expandos == null) {
			return null;
		} else {
			Set<String> result = new HashSet<String>(DEFAULT_INITIAL_CAPACITY);
			for (String expand : expandos) {
				result.add(expand);
			}
			return result;
		}

	}

	private String getStringOrNull(DateTime date) {
		if (date == null) {
			return null;
		} else {
			return date.toString();
		}
	}

	private Map<String, String> toMap(Iterable<Field> fields) {

		if (fields == null) {
			return Collections.emptyMap();
		}
		final HashMap<String, String> result = new HashMap<String, String>(
				DEFAULT_INITIAL_CAPACITY);
		for (Field entity : fields) {
			// ignore fields with null value.
			if (entity.getValue() != null) {
				result.put(entity.getName(), entity.getValue().toString());
			}
		}
		return result;
	}

	private Set<String> listNames(Iterable<? extends NamedEntity> entities) {
		if (entities == null) {
			return Collections.emptySet();
		}
		final HashSet<String> names = new HashSet<String>(
				DEFAULT_INITIAL_CAPACITY);
		for (NamedEntity entity : entities) {
			names.add(entity.getName());
		}
		return names;
	}

	private String getName(NamedEntity entity) {
		if (entity != null) {
			return entity.getName();
		} else {
			return null;
		}
	}

	/**
	 * @return the affectedVersions
	 */
	public Set<String> getAffectedVersions() {
		return affectedVersions;
	}

	/**
	 * @param affectedVersions
	 *            the affectedVersions to set
	 */
	public void setAffectedVersions(Set<String> affectedVersions) {
		this.affectedVersions = affectedVersions;
	}

	/**
	 * @return the assignee
	 */
	public String getAssignee() {
		return assignee;
	}

	/**
	 * @param assignee
	 *            the assignee to set
	 */
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	/**
	 * @return the components
	 */
	public Set<String> getComponents() {
		return components;
	}

	/**
	 * @param components
	 *            the components to set
	 */
	public void setComponents(Set<String> components) {
		this.components = components;
	}

	/**
	 * @return the creationDate
	 */
	public String getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate
	 *            the creationDate to set
	 */
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the dueDate
	 */
	public String getDueDate() {
		return dueDate;
	}

	/**
	 * @param dueDate
	 *            the dueDate to set
	 */
	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 * @return the expandos
	 */
	public Iterable<String> getExpandos() {
		return expandos;
	}

	/**
	 * @param expandos
	 *            the expandos to set
	 */
	public void setExpandos(Set<String> expandos) {
		this.expandos = expandos;
	}

	/**
	 * @return the fields
	 */
	public Map<String, String> getFields() {
		return fields;
	}

	/**
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}

	/**
	 * @return the fixVersions
	 */
	public Set<String> getFixVersions() {
		return fixVersions;
	}

	/**
	 * @param fixVersions
	 *            the fixVersions to set
	 */
	public void setFixVersions(Set<String> fixVersions) {
		this.fixVersions = fixVersions;
	}

	/**
	 * @return the issueType
	 */
	public String getIssueType() {
		return issueType;
	}

	/**
	 * @param issueType
	 *            the issueType to set
	 */
	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the labels
	 */
	public Set<String> getLabels() {
		return labels;
	}

	/**
	 * @param labels
	 *            the labels to set
	 */
	public void setLabels(Set<String> labels) {
		this.labels = labels;
	}

	/**
	 * @return the priority
	 */
	public String getPriority() {
		return priority;
	}

	/**
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}

	/**
	 * @return the estimation
	 */
	public Integer getEstimation() {
		return estimation;
	}

	/**
	 * @param estimation
	 *            the estimation to set
	 */
	public void setEstimation(Integer estimation) {
		this.estimation = estimation;
	}

	/**
	 * @return the remaining
	 */
	public Integer getRemaining() {
		return remaining;
	}

	/**
	 * @param remaining
	 *            the remaining to set
	 */
	public void setRemaining(Integer remaining) {
		this.remaining = remaining;
	}

	/**
	 * @return the timeSpent
	 */
	public Integer getTimeSpent() {
		return timeSpent;
	}

	/**
	 * @param timeSpent
	 *            the timeSpent to set
	 */
	public void setTimeSpent(Integer timeSpent) {
		this.timeSpent = timeSpent;
	}

	/**
	 * @return the updateDate
	 */
	public String getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate
	 *            the updateDate to set
	 */
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	/**
	 * @return the project
	 */
	public String getProject() {
		return project;
	}

	/**
	 * @param project
	 *            the project to set
	 */
	public void setProject(String project) {
		this.project = project;
	}

	/**
	 * @return the reporter
	 */
	public String getReporter() {
		return reporter;
	}

	/**
	 * @param reporter
	 *            the reporter to set
	 */
	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	/**
	 * @return the resolution
	 */
	public String getResolution() {
		return resolution;
	}

	/**
	 * @param resolution
	 *            the resolution to set
	 */
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	/**
	 * @return the name
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the name to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * @param summary
	 *            the summary to set
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	
	
	/**
	 * @return the sprint
	 */
	public String getSprint() {
		return sprint;
	}

	/**
	 * @param sprint the sprint to set
	 */
	public void setSprint(String sprint) {
		this.sprint = sprint;
	}

	
	@Override
	public String toString() {
		String comma = "\t";

		StringBuffer sb = new StringBuffer();
		
		sb.append(getParent()).append(comma);
		sb.append(getKey()).append(comma);
		sb.append(getIssueType()).append(comma);
		sb.append(getSummary()).append(comma);
		sb.append(getStatus()).append(comma);
		sb.append(toTime(getEstimation())).append(comma);
		sb.append(toTime(getRemaining())).append(comma);
		sb.append(toTime(getTimeSpent())).append(comma);;
		sb.append(getPriority());

		return sb.toString();
	}

	private String getParent() {
		return parent;
	}

	/**
	 * @param comma
	 * @param sb
	 */
	protected int toTime(Integer time) {
		if(time == null) {
			return 0;
		}
		return time;
	}
}
