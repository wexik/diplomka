package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import mailinglistonline.server.export.database.DatabaseConfiguration;
import mailinglistonline.server.export.database.DbClient;
import mailinglistonline.server.export.database.MongoDbClient;
import mailinglistonline.server.export.database.entities.ContentPart;
import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.MiniEmail;
import mailinglistonline.server.export.util.PropertiesParser;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;

/**
*
* @author matej
*/
public class MessageUpdatingTest {

   public static final String TEST_MAILS_PATH = "src/test/java/mboxes/test-mails";
   public static final String TEST_MAILS_PATH2 ="src/test/java/mboxes/test-mails2";
   public static final String SIMPLE_MAIL_PATH ="src/test/java/mboxes/simpleMail";
   public static final String BINARIES_MAIL_PATH ="src/test/java/mboxes/oneimageonedoublepdfodt.mbox";
   public static final String MBOX_FOLDER_PATH ="src/test/java/mboxes/folder";
   private DbClient dbClient;

   public MessageUpdatingTest() throws UnknownHostException {
	   DatabaseConfiguration configuration = PropertiesParser.parseDatabaseConfigurationFile(MongoDbClient.class
				.getClass()
				.getResource(MongoDbClient.DATABASE_PROPERTIES_FILE_NAME)
				.getPath());
       configuration.setDefaultCollectionName("test");
       configuration.setDatabaseUrl("127.0.0.1");
       dbClient = new MongoDbClient(configuration);
       dbClient.dropTable();
   }

   @BeforeClass
   public static void setUpClass() {
   }

   @AfterClass
   public static void tearDownClass() {
   }

   @Before
   public void setUp() {
   	dbClient.dropTable();
   }

   @After
   public void tearDown() {
       dbClient.dropTable();
   }

   @Test
   public void emailShouldNotHaveDoubleTag() throws IOException {
	   Email email1 = new Email();
       email1.setFrom("from1@from1.sk");
       email1.setMessageId("message1");
       email1.setThreadRoot(null);
       email1.setMailinglist("mailinglist1@mailinglist1.sk");
	   dbClient.saveMessage(email1);
	   dbClient.addTagToEmail(email1.getId().toHexString(), "simpleTag");
	   
	   email1 =dbClient.getEmailWithId(email1.getId().toString());
	   List<String> tags = email1.getTags();
	   assertTrue(tags.size() == 1);
	   assertEquals(tags.get(0),"simpleTag");
	   
	   dbClient.addTagToEmail(email1.getId().toString(), "simpleTag");
	   email1 =dbClient.getEmailWithId(email1.getId().toString());
	   assertTrue(tags.size() == 1);
	   assertEquals(tags.get(0),"simpleTag");
   }

}