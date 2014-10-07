/**
 * 
 */
package com.bonitasoft.connector.jira;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Nicolas Chabanoles
 *
 */
public class HasPropertyFileConfiguration {

	private Properties configuration;
	
	private final String configurationFilePath; 
	
	public HasPropertyFileConfiguration(String configurationFilenameKey) throws FileNotFoundException, IOException {
		configurationFilePath = configurationFilenameKey;
		loadConfigurationFromFile();
	}

	protected void loadConfigurationFromFile()
			throws FileNotFoundException, IOException {
		final String dbPropertyFilePath = System
				.getProperty(configurationFilePath);
		if (dbPropertyFilePath == null) {
			throw new IllegalStateException(
					"System property not set: "
							+ configurationFilePath
							+ ". Please specify the path to the configuration file that provide Jira and Database access information.");
		} else {
			configuration = new Properties();
			configuration.load(new FileInputStream(dbPropertyFilePath));
		}

	}
	
	protected Properties getConfiguration() {
		return configuration;
	}
	
	public String getProperty(String name) {
		return configuration.getProperty(name);
	}
	
	
}
