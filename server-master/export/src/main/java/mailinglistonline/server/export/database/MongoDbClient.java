/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglistonline.server.export.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.ejb.Singleton;

import mailinglistonline.server.export.database.entities.ContentPart;
import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.Mailinglist;
import mailinglistonline.server.export.database.entities.MiniEmail;
import mailinglistonline.server.export.searchisko.SearchManager;
import mailinglistonline.server.export.util.PropertiesParser;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Indexes;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;


/**
 * MongoDB implementation of the DbClient interface. Accessing and storing emails in the MongoDB instance.
 * @author Matej Briškár
 * @author Július Staššík
 */

@Singleton
public class MongoDbClient implements DbClient{
	private static final String MONGODB_FILES_COLLECTION = "fs";
	public static final String DATABASE_PROPERTIES_FILE_NAME = "/database.properties";	
	private static final String DATABASE_MAILINGLISTS_COLLECTION_NAME = "mailinglists";		
	final static Logger logger = Logger.getLogger(MongoClient.class);

	List<Mailinglist> mailingLists;
	MongoCollection<Email> coll;	
	SearchManager searchManager;
	MongoClient mongoClient;
	MongoDatabase db;		
	MongoCollection<Mailinglist> mailinglistsCollection;
	GridFSBucket gridFSBucket;
		

	public MongoDbClient() throws UnknownHostException, IOException {		
		this(PropertiesParser.parseDatabaseConfigurationFile(MongoDbClient.class
				.getResourceAsStream(DATABASE_PROPERTIES_FILE_NAME)));		
	}

	public MongoDbClient(DatabaseConfiguration configuration) {		
		searchManager = new SearchManager();		
		try {
			connect(configuration.getDatabaseUrl(),
					configuration.getDefaultDatabaseName(),
					configuration.getDefaultPort(),
					configuration.getDefaultCollectionName(),					
					configuration.getUser(),
					configuration.getPassword());
		} catch (UnknownHostException e) {		
			logger.error(e);
		}		
		if(configuration.getMailinglistsCollectionName() != null) {
			readMailinglists(configuration.getMailinglistsCollectionName());
		}
	}

	public synchronized void readMailinglists(String collectionName) {		
		mailinglistsCollection = db.getCollection(collectionName, Mailinglist.class);		
		FindIterable<Mailinglist> mls = mailinglistsCollection.find();		
		mailingLists = new ArrayList<Mailinglist>();			
		for(Mailinglist ml : mls) {			
			mailingLists.add(ml);
		}		
	}
	
	public synchronized void addMailinglist(Mailinglist mailinglist) {		
		if(!mailingLists.contains(mailinglist)) {
			mailinglistsCollection.insertOne(mailinglist);
			mailingLists.add(mailinglist);		
		}
	}	

	private synchronized void connect(String mongoUrl, String databaseName,
			int mongoPort, String collectionName, String user, String password) throws UnknownHostException {
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                					fromProviders(PojoCodecProvider.builder().automatic(true).build()));		
		ServerAddress serverAddress = new ServerAddress(mongoUrl, mongoPort);
		if(user != null && password !=null && !user.isEmpty() && !password.isEmpty()
    			&& !user.equals(" ") && !password.equals(" ")) {
			MongoCredential credential = MongoCredential.createScramSha1Credential(user, databaseName, password.toCharArray());
			mongoClient = new MongoClient(Arrays.asList(serverAddress), 
										  Arrays.asList(credential),
					MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
		} else {
			mongoClient = new MongoClient(serverAddress, 
					MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
		}
		db = mongoClient.getDatabase(databaseName);		
		gridFSBucket = GridFSBuckets.create(db);				
		coll = db.getCollection(collectionName, Email.class);	
		//coll.createIndex(new Document(Email.DATE_OF_LAST_MESSAGE_ADDED_TO_THREAD_MONGO_TAG, -1));
		coll.createIndex(Indexes.compoundIndex(
				Indexes.descending(Email.DATE_OF_LAST_MESSAGE_ADDED_TO_THREAD_MONGO_TAG),
						Indexes.ascending(Email.MAILINGLIST_MONGO_TAG)));
	}

	@PreDestroy
	public void closeConnection() {
		mongoClient.close();		
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getFilesColl()
	 */
	public MongoCollection getFilesColl() {
		return db.getCollection(MONGODB_FILES_COLLECTION + ".files");
	}

	public MongoCollection<Email> getColl() {
		return coll;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#dropTable()
	 */
	@Override
	public synchronized void dropTable() {
		this.coll.drop();
		db.getCollection(MONGODB_FILES_COLLECTION + ".files").drop();
		db.getCollection(MONGODB_FILES_COLLECTION + ".chunks").drop();
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#emailCount()
	 */
	@Override
	public long emailCount() {
		return coll.count();
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#filesCount()
	 */
	@Override
	public synchronized long filesCount() {
		return db.getCollection(MONGODB_FILES_COLLECTION + ".files").count();
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#findFileById(java.lang.String)
	 */
	@Override
	public GridFSFile findFileById(String id)  {		
		GridFSFile file = gridFSBucket.find(eq(Email.ID_MONGO_TAG, new ObjectId(id))).first();
		return file;
								
	}
	
	public InputStream getBinaryFileById(String id) {		
		GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(new ObjectId(id));
		int fileLength = (int) downloadStream.getGridFSFile().getLength();
		byte[] bytesToWriteTo = new byte[fileLength];
		downloadStream.read(bytesToWriteTo);	
		downloadStream.close();
		return new ByteArrayInputStream(bytesToWriteTo);		
	}			

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#findFirstMessageWithMessageId(java.lang.String)
	 */
	@Override
	public Email findFirstMessageWithMessageId(String messageId) {		
		return coll.find(eq(MiniEmail.MESSAGE_ID_MONGO_TAG, messageId)).first();
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getAllEmails()
	 */
	@Override
	public List<Email> getAllEmails() {		
		FindIterable<Email> ems = coll.find();		
		List<Email> emails = new ArrayList<Email>();		
		for(Email email : ems) {						
			emails.add(email);
		}		
		return emails;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getEmailWithId(java.lang.String)
	 */
	@Override
	public Email getEmailWithId(String id) {
		return coll.find(eq(Email.ID_MONGO_TAG, new ObjectId(id))).first();
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getMessage(java.lang.String)
	 */
	@Override
	public synchronized MiniEmail getMessage(String mongoId) {
		return coll.find(eq(Email.ID_MONGO_TAG, new ObjectId(mongoId))).first();
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#saveMessage(mailinglistonline.server.export.database.entities.Email)
	 */
	@Override
	public synchronized boolean saveMessage(Email email) throws IOException {
		// check if the message is not already saved		
		if (getId(email.getMessageId(), email.getMailinglist()) != null) {
			if (email.getAttachments() != null) {
				for (ContentPart part : email.getAttachments()) {
					if (part.getLink() != null) {
						deleteFile(part.getLink());
					}
				}
			}
			return false;
		}		
		coll.insertOne(email);		
		Email id = coll.find(eq(Email.MESSAGE_ID_MONGO_TAG, email.getMessageId())).first();		
		email.setId(id.getId());
		id.setEmailShardKey(id.getMailinglist() + id.getId().toString());		
		coll.updateOne(eq(Email.ID_MONGO_TAG, new ObjectId(id.getId().toString())), 
								set(Email.SHARD_KEY_MONGO_TAG, id.getEmailShardKey()));
		
		if (id.getInReplyTo() != null
				&& id.getInReplyTo().getId().toString() != null) {
			Email parent = coll.find(eq(Email.ID_MONGO_TAG, 
										new ObjectId(id.getInReplyTo().getId().toString()))).first();			
			parent.addReply(id);				
			coll.replaceOne(eq(Email.ID_MONGO_TAG,
									new ObjectId(parent.getId().toString())), parent);			
		}
		return true;
	}
	
	/*
	 * Returns the ID of the object, which has the given message_ID and is in
	 * the same mailinglist
	 */
	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getId(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized String getId(String messageId, String mailinglist) {				
		Email findOne = coll.find(and(eq(Email.MESSAGE_ID_MONGO_TAG, messageId), 
									eq(Email.MAILINGLIST_MONGO_TAG, mailinglist))).first();
		if (findOne == null) {			
			return null;
		}		
		return findOne.getId().toString();
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#deleteMessage(mailinglistonline.server.export.database.entities.Email)
	 */
	@Override
	public synchronized boolean deleteMessage(Email email) throws IOException {
		coll.deleteOne(eq(Email.ID_MONGO_TAG, new ObjectId(email.getId().toString())));
		for (ContentPart cp : email.getAttachments()) {
			if (cp.getLink() != null) {
				deleteFile(cp.getLink());
			}
		}	
		if (email.getInReplyTo() != null) {
			Email parent = coll.find(eq(Email.ID_MONGO_TAG,
									new ObjectId(email.getInReplyTo().getId().toString()))).first();
			parent.removeReply(email);
			coll.updateOne(eq(Email.ID_MONGO_TAG, new ObjectId(parent.getId().toString())), 
									set(Email.REPLIES_MONGO_TAG, parent.getReplies()));
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#createFile(byte[], java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized String createFile(byte[] bytes, String fileName, String contentType) {
		try {			
			InputStream stream = new ByteArrayInputStream(bytes);
			GridFSUploadOptions options = new GridFSUploadOptions()
	                						.chunkSizeBytes(358400)
	                						.metadata(new Document("type", contentType));
			ObjectId fileId = gridFSBucket.uploadFromStream(fileName, stream, options);
					
			return fileId.toString();
		} catch(Exception e) {
			logger.error(e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#deleteFile(java.lang.String)
	 */
	@Override
	public void deleteFile(String id) {		
		try {
			gridFSBucket.delete(new ObjectId(id));
		} catch(Exception e) {
			logger.error(e);
		}
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#updateEmail(mailinglistonline.server.export.database.entities.Email)
	 */
	@Override
	public boolean updateEmail(Email newEmail) throws IOException {
		coll.replaceOne(eq(Email.ID_MONGO_TAG, new ObjectId(newEmail.getId().toString())), newEmail);
		return true;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getId(java.lang.String, java.util.ArrayList)
	 */
	@Override
	public String getId(String messageId, ArrayList<String> mailinglist) {
		BasicDBObject emailObject = new BasicDBObject(Email.MESSAGE_ID_MONGO_TAG, messageId);
		emailObject.put(Email.MAILINGLIST_MONGO_TAG, mailinglist);
		Email findOne =  coll.find(emailObject).first();
		if (findOne == null) {
			return null;
		}
		return findOne.getId().toString();
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getMailinglistRoot(java.lang.Object)
	 */
	@Override
	public List<Email> getMailinglistRoot(Object mailinglist) {
		BasicDBObject query = new BasicDBObject();
		query.append(Email.DATE_OF_LAST_MESSAGE_ADDED_TO_THREAD_MONGO_TAG, 
							new BasicDBObject("$gt", 100));
		query.append(Email.MAILINGLIST_MONGO_TAG, mailinglist);
		FindIterable<Email> find = coll.find(query);		
		List<Email> emails = new ArrayList<Email>();
		for(Email email : find) {
			emails.add(email);
		}
		return emails;
	}
	
	@Override
	public List<Email> getMailinglistRoot(Object mailinglist, int fromNumber,
			int toNumber) {
		BasicDBObject query = new BasicDBObject();		
		query.append(Email.DATE_OF_LAST_MESSAGE_ADDED_TO_THREAD_MONGO_TAG, 
							new BasicDBObject("$gt", 100));
		query.append(Email.MAILINGLIST_MONGO_TAG, mailinglist);
		FindIterable<Email> find = coll.find(query);
		find.skip(fromNumber);
		find.limit(toNumber-fromNumber);
		List<Email> emails = new ArrayList<Email>();
		for (Email email : find) {
			emails.add(email);
		}		
		return emails;
	}

	public List<Email> getWholeThreadWithMessage(String id) {
		Email email = (Email) getEmailWithId(id);
		List<Email> replyPath = new ArrayList<Email>();
		if (email == null) {
			return replyPath;
		}
		BasicDBObject query;
		List<MiniEmail> replyIds = new ArrayList<MiniEmail>();
		replyIds.addAll(email.getReplies());

		replyPath.add(email);
		for (int i = 0; i < replyIds.size(); i++) {
			MiniEmail reply = replyIds.get(i);
			query = new BasicDBObject(Email.ID_MONGO_TAG, new ObjectId(reply.getId().toString()));
			Email replyEmail = coll.find(query).first();
			replyIds.addAll(replyEmail.getReplies());
			replyPath.add(replyEmail);
		}
		return replyPath;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getMailingLists()
	 */
	@Override
	public List<Mailinglist> getMailingLists() {		
		readMailinglists(DATABASE_MAILINGLISTS_COLLECTION_NAME);
		return mailingLists;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#addTagToEmail(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean addTagToEmail(String emailId, String tag) {
		Email email = getEmailWithId(emailId);
		if ((email.getTags() != null) && (email.getTags().contains(tag))) {
			return false;
		}
		email.addTag(tag);
		try {
			updateEmail(email);
			return true;
		} catch (IOException e) {
			logger.error(e);
		}
		return false;
	}	

	public List<Email> getEmailsFrom(String author) {
		FindIterable<Email> find = coll.find(new Document(Email.FROM_MONGO_TAG, author));
		List<Email> emails = new ArrayList<Email>();
		for(Email email : find) {
			emails.add(email);
		}
		return emails;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#getEmails(java.lang.String, java.lang.Object, java.util.List, int, java.util.List, java.util.List)
	 */
	@Override
	public List<Email> getEmails(String from, Object mailinglist,
			List<String> tags, int count, List<String> ascending,
			List<String> descending) {
		BasicDBObject query = new BasicDBObject();		
		if (mailinglist != null && !mailinglist.equals("")) {
			query.append(Email.MAILINGLIST_MONGO_TAG, mailinglist);
		}
		if (from != null && !from.equals("")) {
			query.append(Email.FROM_MONGO_TAG, from);
		}
		if (tags != null && tags.size() != 0) {
			DBObject inTagObject = new BasicDBObject();
			inTagObject.put("$in", tags);
			query.append(Email.TAGS_MONGO_TAG, inTagObject);
		}
		FindIterable<Email> found = coll.find(query);
		BasicDBObject sortingObject = new BasicDBObject();
		if (ascending != null && ascending.size() != 0) {
			for (String sortParameter : ascending) {
				sortingObject.append(sortParameter, 1);
			}
		}
		if (descending != null && descending.size() != 0) {
			for (String sortParameter : descending) {
				sortingObject.append(sortParameter, -1);
			}
		}
		if (sortingObject.size() != 0) {
			found.sort(sortingObject);
		}
		if (count != 0) {
			found.limit(count);
		}
		List<Email> emails = new ArrayList<Email>();
		for(Email email : found) {
			emails.add(email);
		}		
		return emails;
	}

	/* (non-Javadoc)
	 * @see mailinglistonline.server.export.database.DbClient#removeTagFromEmail(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean removeTagFromEmail(String id, String tag) {
		Email email = getEmailWithId(id);
		if ((email.getTags() == null) || (!email.getTags().contains(tag))) {
			return false;
		}
		email.removeTag(tag);
		try {
			updateEmail(email);
			return true;
		} catch (IOException e) {
			logger.error(e);
		}
		return false;
	}
	
	
	public int getMailinglistRootCount(String mailinglist) {
		List<Email> mailinglistRoot = getMailinglistRoot(mailinglist);
		return mailinglistRoot.size();
	};

}
