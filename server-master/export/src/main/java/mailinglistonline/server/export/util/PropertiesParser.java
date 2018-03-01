package mailinglistonline.server.export.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import mailinglistonline.server.export.SecurityFilter;
import mailinglistonline.server.export.database.DatabaseConfiguration;
import mailinglistonline.server.export.database.entities.Client;
import mailinglistonline.server.export.database.entities.Mailinglist;

/**
 * The util class used to parse the .properties files.
 * 
 * @author Matej Briškár
 * @author Július Staššík
 */
public class PropertiesParser {
	private static final Logger log = Logger.getLogger(PropertiesParser.class);
	public static DatabaseConfiguration parseDatabaseConfigurationFile(
			String path) {
		DatabaseConfiguration configuration = new DatabaseConfiguration();
		Properties prop = new Properties();
		try {
			return parseDatabaseConfigurationFile(new FileInputStream(path));
		} catch (IllegalArgumentException e) {
			log.error("Reading database configuration file threw and exception for file path " +path + " " + e);
			throw new IllegalArgumentException(
					"Reading database configuration file threw and exception for file path "
							+ path, e);
			
		} catch (FileNotFoundException e) {
			log.error("Reading database configuration file threw and exception for file path " +path + " " + e);
			throw new IllegalArgumentException(
					"Reading database configuration file threw and exception for file path "
							+ path, e);
		}
	}

	public static DatabaseConfiguration parseDatabaseConfigurationFile(
			InputStream stream) {
		DatabaseConfiguration configuration = new DatabaseConfiguration();
		Properties prop = new Properties();
		try {
			prop.load(stream);
		} catch (IOException e) {
			log.error("IOException occured when reading the stream " + e);
			throw new IllegalArgumentException(
					"IOException occured when reading the stream", e);
		}
		configuration.setDefaultPort(Integer.valueOf(prop
				.getProperty("defaultMongoPort")));
		configuration.setDatabaseUrl(prop.getProperty("defaultMongoUrl"));
		configuration.setDefaultDatabaseName(prop
				.getProperty("defaultDatabaseName"));
		configuration.setDefaultCollectionName(prop
				.getProperty("defaultCollection"));
		configuration.setMailinglistsCollectionName(prop
				.getProperty("mailinglistsCollectionName"));
		configuration.setUser(prop.getProperty("user"));
		configuration.setPassword(prop.getProperty("password"));
		return configuration;
	}	
	
	public static List<Client> parseClientsProperties (InputStream stream){		
		List<Client> clients = new ArrayList<Client>();
		Properties prop = new Properties();
		
		try {			
			prop.load(stream);			
		} catch (IOException e) {	
			log.error("IOException occured when reading the stream" + e);
			throw new IllegalArgumentException(
					"IOException occured when reading the stream", e);			
		}		
		String name = prop.getProperty("name" + 1);		
		String pwd = prop.getProperty("password" + 1);
		String role = prop.getProperty("role" + 1);
		int i = 1;
		while(name != null) {
			Client client = new Client();
			client.setName(name);
			client.setPwd(pwd);
			client.setRole(role);
			clients.add(client);
			i++;
			name = prop.getProperty("name" + i);
			pwd = prop.getProperty("password" + i);
			role = prop.getProperty("role" + i);
		}		
		return clients;
	}
	
	public static List<String> parseClamavProperties(InputStream stream){
		List<String> result = new ArrayList<String>();
		Properties prop = new Properties();
		try {
			prop.load(stream);
		} catch (IOException e) {	
			log.error("IOException occured when reading the stream" + e);
			throw new IllegalArgumentException(
					"IOException occured when reading the stream", e);			
		}
		String url = prop.getProperty("url");
		String port = prop.getProperty("port");
		result.add(url);
		result.add(port);
		return result;
	}

}
