package com.redhat.mailinglistOnline.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;

import javax.annotation.ManagedBean;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.redhat.mailinglistOnline.client.entities.User;
import com.redhat.mailinglistOnline.jsf.PanelChangeListener;
import java.io.Serializable;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * The class handling the communication with the database.
 * 
 * @author Matej Briškár
 * @author Július Staššík
 */
@ApplicationScoped
public class DbClient  {	

	private static String DATABASE_PROPERTIES_FILE_NAME = "userDatabase.properties";	  
	    MongoCollection<User> coll;
	    MongoClient mongoClient;
	    final static Logger logger = Logger.getLogger(DbClient.class);
	    public DbClient() throws UnknownHostException, IOException {	    	
	        init();
	    }
	    
	    public void init() throws UnknownHostException, IOException {	    	
	        Properties prop = new Properties();
	        prop.load(DbClient.class.getClassLoader().getResourceAsStream((DATABASE_PROPERTIES_FILE_NAME)));
	        Integer defaultPort = Integer.valueOf(prop.getProperty("defaultMongoPort"));
	        String databaseUrl = prop.getProperty("defaultMongoUrl");
	        String defaultDatabaseName = prop.getProperty("defaultDatabaseName");
	        String defaultCollectionName = prop.getProperty("defaultCollection");
	        String user = prop.getProperty("user");
	        String password = prop.getProperty("password");
	        connect(databaseUrl, defaultDatabaseName, defaultPort, defaultCollectionName,user,password);
	    }
	    
	    private void connect(String mongoUrl, String databaseName, int mongoPort, String collectionName, String user, String password) throws UnknownHostException {
	    	ServerAddress serverAddress = new ServerAddress(mongoUrl, mongoPort);	    	
	    	CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
					fromProviders(PojoCodecProvider.builder().automatic(true).build()));	    	
	    	if(user != null && password != null && !user.isEmpty() && !password.isEmpty()
	    			&& !user.equals(" ") && !password.equals(" ")) {
	    		MongoCredential credential = MongoCredential.createScramSha1Credential(user, databaseName, password.toCharArray());
				mongoClient = new MongoClient(Arrays.asList(serverAddress), 
											  Arrays.asList(credential),
								MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
	        } else {
	        	mongoClient = new MongoClient(serverAddress, 
						MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
	        }
	        MongoDatabase db = mongoClient.getDatabase(databaseName);	        
	        mongoClient.setWriteConcern(WriteConcern.SAFE);
	        coll = db.getCollection(collectionName, User.class);	        
	    }
	    
	    @PreDestroy
	    public void closeConnection() {
	        mongoClient.close();
	    }
	    
	    public User getUserByName(String name) {	        	       
	        User user = coll.find(eq(User.ROOT_NAME_TAG, name)).first();
	        return user;
	    }	    	   

		public boolean saveUser(User user) {
	        coll.insertOne(user);
	        return true;
		}
		
		public boolean updateUser(User user) {			
			User found = coll.find(eq(User.ROOT_NAME_TAG, user.getUsername())).first();
			if(found != null) {				
				coll.replaceOne(eq(User.ROOT_NAME_TAG, user.getUsername()), user);
				return true;
			} else {
				return false;
			}
		}
	
}
