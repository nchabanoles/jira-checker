/**
 * 
 */
package com.bonitasoft.connector.jira.hibernate;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.hibernate.Session;

import com.bonitasoft.connector.jira.HasPropertyFileConfiguration;
import com.bonitasoft.connector.jira.IssueStorage;
import com.bonitasoft.connector.jira.model.PersistentIssue;

/**
 * @author Nicolas Chabanoles
 *
 */
public class HibernateIssueStorage extends HasPropertyFileConfiguration implements IssueStorage {

	protected static final String DATABASE_KEY = "db.url";
	protected static final String DATABASE_DRIVER_KEY = "db.driver";
	protected static final String DATABASE_USERNAME_KEY = "db.username";
	protected static final String DATABASE_PASSWORD_KEY = "db.password";
	
	public HibernateIssueStorage(String configurationFilenameKey) throws FileNotFoundException, IOException {
		super(configurationFilenameKey);
	}
	
	

	
	
	/* (non-Javadoc)
	 * @see com.bonitasoft.connector.jira.IssueStorage#store(com.atlassian.jira.rest.client.domain.Issue)
	 */
	@Override
	public void store(PersistentIssue issue) {
		

        try {
        	
            // Begin unit of work
        	final Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            session.beginTransaction();

            // transform issue into a model easier to persistent
            session.save(issue);

            // End unit of work
            session.getTransaction().commit();
        }
        catch (Exception ex) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new RuntimeException(ex);
        }
		
	}

}
