package mailinglistonline.server.export.searchisko;

import java.util.List;

import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.MiniEmail;

/**
 * Interface that any connector to a search provider has to implement.
 * 
 * @author Matej Briškár
 */
public interface SearchClient {

	/**
	 * Return the emails that contain the content
	 * 
	 * @param content The content that is going to be searched.
	 */
	public List<MiniEmail> searchByContent(String content);
	
	
	/**
	 * Return the emails that contain the content and filtered by tags
	 * 
	 * @param content The content that is going to be searched.
	 * 
	 * @param tags Tags which will filter on searchisko
	 */
	public List<MiniEmail> searchByContentFilteredByTags(String content, List<String> tags);
	
	/**
	 * Update the email
	 * 
	 * @param email Email to be updated.
	 */
	public boolean updateEmail(Email email);
	
	/**
	 * Add the email
	 * 
	 * @param email Email to be added.
	 */
	public boolean addEmail(Email email);
}
