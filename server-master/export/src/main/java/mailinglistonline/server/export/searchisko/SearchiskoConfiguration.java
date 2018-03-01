package mailinglistonline.server.export.searchisko;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Object containing the information about the configuration for the searchisko instance.
 * 
 * @author Matej Briškár
 */
public class SearchiskoConfiguration {
	private String searchiskoUrl;
	private String username;
	private String password;
	

	public String getSearchiskoUrl() {
		return searchiskoUrl;
	}
	public void setSearchiskoUrl(String searchiskoUrl) {
		this.searchiskoUrl = searchiskoUrl;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public SearchiskoConfiguration readFromPropertyFile(InputStream stream) {
		Properties prop = new Properties();
		try {
			prop.load(stream);
		} catch (IOException e) {
			throw new IllegalArgumentException("Readingsearchisko configuration file threw and exception for file stream ", e);
		}
        searchiskoUrl = prop.getProperty("searchiskoUrl");
        username = prop.getProperty("username");
        password = prop.getProperty("password");
		return this;
	}
}
