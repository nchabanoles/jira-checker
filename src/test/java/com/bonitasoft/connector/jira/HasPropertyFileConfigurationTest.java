package com.bonitasoft.connector.jira;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HasPropertyFileConfigurationTest {

	protected static final String CONFIGURATION_FILENAME_KEY = "org.bonitasoft.jira.configuration";
	
	private HasPropertyFileConfiguration propertyFileConfiguration;
	
	@Before
	public void setup() {
		ensureValidConfiguration();
	}
	
	@After
	public void clearSystemProperty() {
		System.clearProperty(CONFIGURATION_FILENAME_KEY);
	}
	
	public static void ensureValidConfiguration() {
		URL confFileURL = HasPropertyFileConfigurationTest.class.getClassLoader().getResource("jiraChecker.cfg");
		final String filePath = confFileURL.getPath();
		System.setProperty(CONFIGURATION_FILENAME_KEY, filePath);
	}
	
	@Test
	public void testLoadConfigurationFromFileNominal() throws FileNotFoundException, IOException {
		ensureValidConfiguration();
		propertyFileConfiguration = new HasPropertyFileConfiguration(CONFIGURATION_FILENAME_KEY);
		
		propertyFileConfiguration.loadConfigurationFromFile();
		
		Assertions.assertThat(propertyFileConfiguration.getConfiguration()).isNotNull();
		Assertions.assertThat(propertyFileConfiguration.getConfiguration()).isNotEmpty();
	}

	@Test(expected=IllegalStateException.class)
	public void testLoadConfigurationFromFileNoPropertySet() throws FileNotFoundException, IOException {
		clearSystemProperty();
		propertyFileConfiguration = new HasPropertyFileConfiguration(CONFIGURATION_FILENAME_KEY);
		propertyFileConfiguration.loadConfigurationFromFile();
	}

	
	@Test(expected=FileNotFoundException.class)
	public void testLoadConfigurationFromFileFileNotFound() throws FileNotFoundException, IOException {
		// make sure the file will not be found.
		System.setProperty(CONFIGURATION_FILENAME_KEY, "doesNotExistFile.cfg");
		propertyFileConfiguration = new HasPropertyFileConfiguration(CONFIGURATION_FILENAME_KEY);
		
		propertyFileConfiguration.loadConfigurationFromFile();
	}
}
