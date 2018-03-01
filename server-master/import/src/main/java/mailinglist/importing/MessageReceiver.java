/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist.importing;

import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.mongodb.MongoClient;

import mailinglistonline.server.export.database.DatabaseConfiguration;
import mailinglistonline.server.export.database.MongoDbClient;
import mailinglist.MessageManager;

/**
 *	Class capable of importing the emails pushed to the instance by the terminal into the database.
 *
 * @author Matej Briškár
 */
public class MessageReceiver {
	final static Logger logger = Logger.getLogger(MessageReceiver.class);
    
	/*
	 * Main method, called with 5 or 7 parameters:
	 * 1. Database Url
	 * 2. Database Name
	 * 3. Database Port
	 * 4. Database Collection
	 * 5. Username to the database
	 * 6. Password for the user
	 * 7. True/False if the messages should be added to the searchisko also.
	 */
    public static void main(String[] args) throws MessagingException, IOException {
        Session s = Session.getDefaultInstance(new Properties());
        MimeMessage message = new MimeMessage(s, System.in);
        System.out.println("Message received through the terminal");
        System.out.println("Message ID: " + message.getMessageID());
        System.out.println("Message content: " + message.getContent().toString());
        logger.info("Message received through the terminal");
        logger.info("Message ID: " + message.getMessageID());
        logger.info("Message content: " + message.getContent().toString());
        MongoDbClient messageSaver;
        boolean sendMessageAlsoToSearchisko = true;
        if(args.length > 5) {
        	DatabaseConfiguration conf = new DatabaseConfiguration();
        	conf.setDatabaseUrl(args[0]);
        	conf.setDefaultDatabaseName(args[1]);
        	conf.setDefaultPort(Integer.valueOf(args[2]));
        	conf.setDefaultCollectionName(args[3]);
        	if(args.length == 7) {
        		conf.setUser(args[4]);
            	conf.setPassword(args[5]);
        	}
            messageSaver= new MongoDbClient(conf);
            sendMessageAlsoToSearchisko=Boolean.valueOf(args[6]);
        } else {
        	messageSaver = new MongoDbClient();
        }
        MessageManager manager= new MessageManager(messageSaver,sendMessageAlsoToSearchisko);
        manager.createAndSaveMessage(message);
        messageSaver.closeConnection();
    }
}
