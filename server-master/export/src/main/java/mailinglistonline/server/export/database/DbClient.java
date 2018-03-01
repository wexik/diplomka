package mailinglistonline.server.export.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.Mailinglist;
import mailinglistonline.server.export.database.entities.MiniEmail;

import com.mongodb.DBCollection;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * Interface containing all the methods that the client to the persistent store of the emails should have.
 * @author Matej Briškár
 */
public interface DbClient {

	public void addMailinglist(Mailinglist mailinglist);
	
	public void dropTable();

	public long emailCount();

	public long filesCount();

	public GridFSFile findFileById(String id);
	
	//public byte[] getBinaryFileById(String id);
	public InputStream getBinaryFileById(String id);

	/*
	 * Returns the ID of the object, which has the given message_ID and is in
	 * the same mailinglist
	 */
	public String getId(String messageId, String mailinglist);
	
	List<Email> getWholeThreadWithMessage(String id);

	public Email findFirstMessageWithMessageId(String messageId);

	public List<Email> getAllEmails();

	public Email getEmailWithId(String id);

	public MiniEmail getMessage(String mongoId);

	public boolean saveMessage(Email email) throws IOException;

	public boolean deleteMessage(Email email) throws IOException;

	/*
	 * Creates file in the database
	 */
	public String createFile(byte[] bytes, String fileName,
			String contentType);

	public boolean addTagToEmail(String emailId, String tag);
	public void deleteFile(String id);

	public boolean updateEmail(Email newEmail) throws IOException;

	public String getId(String messageId, ArrayList<String> mailinglist);

	/*
	 * Get all the roots from the mailinglist. It is object so it is allowed to put there a regexp pattern.
	 */
	public List<Email> getMailinglistRoot(Object mailinglist);

	public List<Mailinglist> getMailingLists();


	public List<Email> getEmails(String from, Object mailinglist,
			List<String> tags, int count, List<String> ascending,
			List<String> descending);

	public boolean removeTagFromEmail(String id, String tag);

	public List<Email> getMailinglistRoot(Object mailinglist, int fromNumber,
			int toNumber);

	public int getMailinglistRootCount(String mailinglist);

}
