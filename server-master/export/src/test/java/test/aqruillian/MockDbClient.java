package test.aqruillian;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.Singleton;
import javax.enterprise.inject.Alternative;

import org.bson.types.ObjectId;

import mailinglistonline.server.export.database.DbClient;
import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.Mailinglist;
import mailinglistonline.server.export.database.entities.MiniEmail;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.gridfs.GridFSDBFile;

@Alternative
@Singleton
public class MockDbClient implements DbClient{
	public static final String THIRD_ID = "4edd40c86762e0fb12000003";
	public static final String SECOND_ID = "4edd40c86762e0fb12000002";
	public static final String FIRST_ID = "4edd40c86762e0fb12000001";
	List<Mailinglist> mailingLists = new ArrayList<Mailinglist>();
	GridFSDBFile alwaysReturnedFile = new GridFSDBFile();
	List<Email> emails = new ArrayList<Email>();
	
	public MockDbClient()  {
		refreshEmails();
	}
	
	private void refreshEmails() {
		emails.clear();
		emails.add(EmailCreator.createSimpleEmail(FIRST_ID));
		emails.add(EmailCreator.createSimpleEmail(SECOND_ID));
		emails.add(EmailCreator.createSimpleEmail(THIRD_ID));
	}
	
	@Override
	public synchronized void dropTable() {
	}
	
	@Override
	public long emailCount() {
		return 1;
	}

	@Override
	public synchronized long filesCount() {
		return 1;
	}

	@Override
	public GridFSFile findFileById(String id) {		
		return null;
	}

	@Override
	public synchronized String getId(String messageId, String mailinglist) {
		return FIRST_ID;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#findFirstMessageWithMessageId(java.lang.String)
	 */
	@Override
	public Email findFirstMessageWithMessageId(String messageId) {
		return emails.get(0);
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getAllEmails()
	 */
	@Override
	public List<Email> getAllEmails() {
		return emails;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getEmailWithId(java.lang.String)
	 */
	@Override
	public Email getEmailWithId(String id) {
		for(Email email : emails) {
			if(email.getId().equals(id)) {
				return email;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getMessage(java.lang.String)
	 */
	@Override
	public synchronized MiniEmail getMessage(String mongoId) {
		return emails.get(0);
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#saveMessage(mailinglistonline.server.export.database.entities.Email)
	 */
	@Override
	public synchronized boolean saveMessage(Email email) throws IOException {
		return true;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#deleteMessage(mailinglistonline.server.export.database.entities.Email)
	 */
	@Override
	public synchronized boolean deleteMessage(Email email) throws IOException {
		return true;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#createFile(byte[], java.lang.String, java.lang.String)
	 */
	@Override
	public String createFile(byte[] bytes, String fileName, String contentType) {
		return "file";
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#deleteFile(java.lang.String)
	 */
	@Override
	public void deleteFile(String id) {
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#updateEmail(mailinglistonline.server.export.database.entities.Email)
	 */
	@Override
	public boolean updateEmail(Email newEmail) throws IOException {
		return true;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getId(java.lang.String, java.util.ArrayList)
	 */
	@Override
	public String getId(String messageId, ArrayList<String> mailinglist) {
		return emails.get(0).getId().toString();
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getMailinglistRoot(java.lang.Object)
	 */
	@Override
	public List<Email> getMailinglistRoot(Object mailinglist) {
		return emails;
	}

	public List<Email> getWholeThreadWithMessage(String id) {
		return emails;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getMailingLists()
	 */
	@Override
	public List<Mailinglist> getMailingLists() {
		return mailingLists;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#addTagToEmail(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean addTagToEmail(String emailId, String tag) {
		for(Email email : emails) {
			if(email.getId().equals(emailId)) {
				 email.addTag(tag);
				 return true;
			}
		}
		return false;
	}

	public List<Email> getEmailsNotStrictMatch(String mailinglist, String from,
			List<String> tags) {
		return emails;
	}

	public List<Email> getEmailsFrom(String author) {
		return emails;

	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getEmails(java.lang.String, java.lang.Object, java.util.List, int, java.util.List, java.util.List)
	 */
	@Override
	public List<Email> getEmails(String from, Object mailinglist,
			List<String> tags, int count, List<String> ascending,
			List<String> descending) {
		return emails;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#removeTagFromEmail(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean removeTagFromEmail(String id, String tag) {
		return false;
	}
	
	@Override
	public List<Email> getMailinglistRoot(Object mailinglist, int fromNumber,
			int toNumber) {
		return getMailinglistRoot(mailinglist);
	}

	@Override
	public int getMailinglistRootCount(String mailinglist) {
		return 3;
	}

	@Override
	public void addMailinglist(Mailinglist mailinglist) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InputStream getBinaryFileById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

}