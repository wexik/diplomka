/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import mailinglistonline.server.export.database.DatabaseConfiguration;
import mailinglistonline.server.export.database.MongoDbClient;
import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.MiniEmail;
import mailinglistonline.server.export.util.PropertiesParser;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 *
 * @author Matej Briškár
 */
public class DbClientTest {

    private MongoDbClient dbClient;
    private ArrayList<Email> insertedEmails;

    public DbClientTest() throws UnknownHostException, IOException {
        DatabaseConfiguration configuration = PropertiesParser.parseDatabaseConfigurationFile(MongoDbClient.class
				.getClass()
				.getResource((MongoDbClient.DATABASE_PROPERTIES_FILE_NAME))
				.getPath());
        configuration.setDefaultCollectionName("test");
        configuration.setDatabaseUrl("127.0.0.1");
        dbClient = new MongoDbClient(configuration);
        dbClient.dropTable();
    }
    

    @Before
    public void setUp() throws IOException {
        insertedEmails = new ArrayList<Email>();
        Email email1 = new Email();
        email1.setFrom("from1@from1.sk");
        email1.setMessageId("message1");
        email1.setLastMessageDate(1200l);
        email1.setMailinglist("mailinglist1");
        email1.setReplies(null);
        insertedEmails.add(email1);

        Email email2 = new Email();
        email2.setFrom("from2@from2.sk");
        email2.setMessageId("message2");
        email2.setThreadRoot(email1);
        email2.setInReplyTo(email1);
        insertedEmails.add(email2);

        Email email3 = new Email();
        email3.setFrom("from3@from3.sk");
        email3.setMessageId("message3");
        email3.setThreadRoot(email1);
        email3.setInReplyTo(email1);
        email3.setMailinglist("mailinglist2");
        insertedEmails.add(email3);

        Email email4 = new Email();
        email4.setFrom("from4@from4.sk");
        email4.setMessageId("message4");
        email4.setThreadRoot(email1);
        email4.setInReplyTo(email3);
        email4.setMailinglist("mailinglist1");
        insertedEmails.add(email4);

        Email email5 = new Email();
        email5.setFrom("from2@from2.sk");
        email5.setMessageId("message5");
        email5.setLastMessageDate(1250l);
        email5.setMailinglist("mailinglist1");
        insertedEmails.add(email5);
        
        for(Email email : insertedEmails) {
        	dbClient.saveMessage(email);
        }
    }

    @After
    public void tearDown() {
        dbClient.dropTable();
    }

    @Test
    public void getEmailById() {
        Email email = dbClient.getEmailWithId(insertedEmails.get(0).getId().toString());
        email.setReplies(new ArrayList<MiniEmail>());
        assertEquals(email.getId().toString(),insertedEmails.get(0).getId().toString());
        assertEquals(email.getFrom(),insertedEmails.get(0).getFrom());
        assertEquals(email.getMessageId(),insertedEmails.get(0).getMessageId());
        assertEquals(email.getMailinglist(),insertedEmails.get(0).getMailinglist());
    }

    @Test
    public void getAllEmails() {
        assertEquals(5, dbClient.getAllEmails().size());        
        ArrayList<Email> allEmails = (ArrayList<Email>) dbClient.getAllEmails();        
        for (int i = 0; i < insertedEmails.size(); i++) {
            Email email =insertedEmails.get(i);            
            assertEquals(email.getId().toString(),allEmails.get(i).getId().toString());
            assertEquals(email.getFrom(),allEmails.get(i).getFrom());
            assertEquals(email.getMessageId(),allEmails.get(i).getMessageId());
            assertEquals(email.getMailinglist(),allEmails.get(i).getMailinglist());
        }
    }

    @Test
    public void getEmailByAuthor() {
        List<Email> fromEmails = dbClient.getEmailsFrom("from1@from1.sk");
        assertEquals(1, fromEmails.size());
        assertEquals(insertedEmails.get(0).getId().toString(),fromEmails.get(0).getId().toString());        
        fromEmails = dbClient.getEmailsFrom("from2@from2.sk");       
        assertEquals(2, fromEmails.size());        
    }

    @Test
    public void getMailingListRoots() {
        List<Email> pathEmails = dbClient.getMailinglistRoot("mailinglist1");                
        assertEquals(2, pathEmails.size());        
    }   
}
