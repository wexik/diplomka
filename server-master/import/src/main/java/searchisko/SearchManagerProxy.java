package searchisko;

import java.util.List;

import javax.mail.internet.MimeMessage;

import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.MiniEmail;
import mailinglistonline.server.export.searchisko.SearchClient;
import mailinglistonline.server.export.searchisko.SearchManager;
import mailinglistonline.server.export.searchisko.SearchiskoConfiguration;

/**
 *	A proxy to the SearchClient implementation for the Searchisko.
 * 
 * @author Matej Briškár
 */
public class SearchManagerProxy implements SearchClient{

	private static String SERVER_PROPERTIES_FILE_NAME = "searchisko.properties";
	SearchManager searchManager;

	public SearchManagerProxy(){
		this.searchManager= new SearchManager(new SearchiskoConfiguration().readFromPropertyFile(
				SearchManager.class.getClassLoader().getResourceAsStream((SERVER_PROPERTIES_FILE_NAME))),true);
	}

	public boolean addEmail(Email email) {
		return searchManager.addEmail(email);
	}

	public boolean removeEmail(String id) {
		return searchManager.removeEmail(id);
	}

	@Override
	public List<MiniEmail> searchByContent(String content) {
		return searchManager.searchByContent(content);
	}

	@Override
	public boolean updateEmail(Email email) {
		return searchManager.updateEmail(email);
	}

	@Override
	public List<MiniEmail> searchByContentFilteredByTags(String content, List<String> tags) {
		return searchManager.searchByContentFilteredByTags(content, tags);		
	}

}