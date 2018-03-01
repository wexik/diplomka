package test;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import mailinglist.MessageManager;
import mailinglist.importing.MboxImporter;
import mailinglist.importing.MessageReceiver;
import mailinglistonline.server.export.database.DatabaseConfiguration;
import mailinglistonline.server.export.database.DbClient;
import mailinglistonline.server.export.database.MongoDbClient;
import mailinglistonline.server.export.database.entities.ContentPart;
import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.MiniEmail;
import mailinglistonline.server.export.util.PropertiesParser;
import mailinglistonline.server.export.searchisko.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;

/**
 *
 * @author Matej Briškár
 */
public class ImportingTest {

    public static final String TEST_MAILS_PATH = "src/test/java/mboxes/test-mails.mbox";
    public static final String TEST_MAILS_PATH2 ="src/test/java/mboxes/test-mails2.mbox";
    public static final String SIMPLE_MAIL_PATH ="src/test/java/mboxes/simpleMail.mbox";
    public static final String BINARIES_MAIL_PATH ="src/test/java/mboxes/oneimageonedoublepdfodt.mbox";
    public static final String MBOX_FOLDER_PATH ="src/test/java/mboxes/folder";
    private DbClient dbClient;
    private DatabaseConfiguration configuration;
    static final String TEST_COLLECTION_NAME = "test";

    public ImportingTest() throws UnknownHostException {
        configuration = PropertiesParser.parseDatabaseConfigurationFile(MboxImporter.class
				.getClass()
				.getResource((MongoDbClient.DATABASE_PROPERTIES_FILE_NAME))
				.getPath());
        configuration.setDefaultCollectionName(TEST_COLLECTION_NAME);
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
    public void testMboxNumberOfMessages() throws UnknownHostException, NoSuchProviderException, MessagingException, IOException {
    	MboxImporter mbox = new MboxImporter(dbClient,false);
        mbox.importMbox(TEST_MAILS_PATH);
        assertEquals(dbClient.emailCount(), 62);
        mbox.importMbox(TEST_MAILS_PATH);
        assertEquals(dbClient.emailCount(), 62);

    }

    @Test
    public void testMboxFolderNumberOfMessages() throws UnknownHostException, NoSuchProviderException, MessagingException, IOException {
        MboxImporter mbox = new MboxImporter(dbClient,false);
        mbox.importMboxDirectory(MBOX_FOLDER_PATH);
        // one email has two mailinglists
        assertEquals(72+62+1, dbClient.emailCount());
        mbox.importMboxDirectory(MBOX_FOLDER_PATH);
        assertEquals( "Any email should not be added twice",72+62+1,dbClient.emailCount());
    }
    /*
    @Test
    public void testBinaries() throws UnknownHostException, NoSuchProviderException, MessagingException, IOException {
        MboxImporter mbox = new MboxImporter(dbClient,false);
        mbox.importMbox(BINARIES_MAIL_PATH);
        // one email has two mailinglists
        assertEquals(2, dbClient.emailCount());
        assertEquals(3, dbClient.filesCount());
        mbox.importMbox(BINARIES_MAIL_PATH);
        assertEquals(2, dbClient.emailCount());
        assertEquals(3, dbClient.filesCount());
        
        
        Email email=(Email) dbClient.findFirstMessageWithMessageId("<CAGc8r1XFOSW96dVvnWvaq0tMBdQeGR+X95YNpX+r6qJoZbHhBg@mail.gmail.com>");
        ArrayList<ContentPart> attachments = email.getAttachments();
        assertEquals(2, attachments.size());
        String firstLink=attachments.get(0).getLink();
        String secondLink=attachments.get(1).getLink();
        GridFSDBFile firstFile = dbClient.findFileById(firstLink);
        GridFSDBFile secondFile = dbClient.findFileById(secondLink);
        
        assertEquals(firstLink, firstFile.getId().toString());
        assertEquals(secondLink, secondFile.getId().toString());
        
        byte[] buf = new byte[100*1024];
        firstFile.getInputStream().read(buf,0, 100*1024);
        String str = new String(buf, "UTF-8");
        assertTrue(str.startsWith("UEsDBBQAAAgAAM6BcERexjIMJwAAACcAAAAIAAAAbWltZXR5cGVhcH"));
        
        secondFile.getInputStream().read(buf,0, 100*1024);
        str = new String(buf, "UTF-8");
        assertTrue(str.startsWith("JVBERi0xLjQKJcOkw7zDtsOfCjIgMCBvYmoKP"));
        
    }*/

    @Test
    public void testSaveMessage() throws AddressException, MessagingException, IOException {
        final String address = "address@adress.sk";
        final String text = "abc";
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        message.setFrom(new InternetAddress(address));
        message.setText(text);
        message.setHeader("Message-ID", text);
        message.setRecipient(Message.RecipientType.TO, new InternetAddress("linux@lists.linux.sk"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
        MessageManager manager = new MessageManager(dbClient,false);
        assertTrue(manager.createAndSaveMessage(message));
        assertEquals(1, dbClient.emailCount());
        Email email = (Email) dbClient.findFirstMessageWithMessageId(text);
        ContentPart cp = email.getMainContent().get(0);
        assertEquals(text, cp.getText());
        assertEquals(text, email.getMessageId());
        assertEquals(address, email.getFrom());


    }

    private void writeDBItems() {
        List<Email> objects = dbClient.getAllEmails();
        for (Email email : objects) {
        	if(email.getId() != null) {
            System.out.println("ID: " + email.getId().toString());
            	Logger.getLogger(ImportingTest.class.getName()).log(Level.INFO, null, 
            									"ID: " + email.getId().toString());
        	}
        	if(email.getMessageId() != null) {
            System.out.println("MessageID: " + email.getMessageId());
            Logger.getLogger(ImportingTest.class.getName()).log(Level.INFO, null, 
            		"MessageID: " + email.getMessageId());
        	}
            if(email.getInReplyTo() != null) {
            System.out.println("IN REPLY TO: " + email.getInReplyTo().toString());
            Logger.getLogger(ImportingTest.class.getName()).log(Level.INFO, null, 
            		"IN REPLY TO: " + email.getInReplyTo().toString());
            }
            if(email.getReplies() != null) {
            System.out.println("REPLIES: " + email.getReplies().toString());
            Logger.getLogger(ImportingTest.class.getName()).log(Level.INFO, null, 
            		"REPLIES: " + email.getReplies().toString());
            }
            if(email.getThreadRoot() != null) {
            System.out.println("ROOT: " + email.getThreadRoot().toString());
            Logger.getLogger(ImportingTest.class.getName()).log(Level.INFO, null, 
            		"ROOT: " + email.getThreadRoot().toString());
            }
            if(email.getDate() != null) {
            System.out.println("SENT DATE: " + email.getDate().toString());
            Logger.getLogger(ImportingTest.class.getName()).log(Level.INFO, null, 
            		"SENT DATE: " + email.getDate().toString());
            }
            if(email.getMainContent() != null) {
            System.out.println("MAIN TEXT: " + email.getMainContent().toString());
            Logger.getLogger(ImportingTest.class.getName()).log(Level.INFO, null, 
            		"MAIN TEXT: " + email.getMainContent().toString());
            }
            if(email.getAttachments() != null) {
            System.out.println("ATTACHMENTS: " + email.getAttachments().toString());
            Logger.getLogger(ImportingTest.class.getName()).log(Level.INFO, null, 
            		"ATTACHMENTS: " + email.getAttachments().toString());
            }
            System.out.println("");
            Logger.getLogger(ImportingTest.class.getName()).log(Level.INFO, null,"");
        }
    }

    @Test
    public void testMboxMessageAttributes() throws UnknownHostException, NoSuchProviderException, MessagingException, IOException {
        MboxImporter mbox = new MboxImporter(dbClient,false);
        mbox.importMbox(TEST_MAILS_PATH);
        mbox.importMbox(TEST_MAILS_PATH2);

        Email email=(Email) dbClient.findFirstMessageWithMessageId("<4E7CA9DA.9040904@gmail.com>");
        assertNotNull(email);
        assertTrue(email.getMessageId().equals("<4E7CA9DA.9040904@gmail.com>"));
        assertTrue(email.getFrom().equals("martin.kyrc@gmail.com"));
        assertEquals(email.getMailinglist(), "linux@lists.linux.sk");
        assertEquals(email.getThreadRoot(), null);
        assertNull(email.getAttachments());
        assertEquals("text/plain", email.getMainContent().get(0).getType());
        assertTrue(email.getMainContent().get(0).getText().toString().startsWith("ahojte,"));

        email = (Email) dbClient.findFirstMessageWithMessageId("<CAJ37LfSeBctpzD3WS7Cbm2G_uD7c-eSkcBYJ=FtVRRqXc4GWnw@mail.gmail.com>");
        assertTrue(email.getMessageId().equals("<CAJ37LfSeBctpzD3WS7Cbm2G_uD7c-eSkcBYJ=FtVRRqXc4GWnw@mail.gmail.com>"));
        assertTrue(email.getFrom().equals("remenec@gmail.com"));
        Email replyToEmail = (Email) dbClient.findFirstMessageWithMessageId("<d7f794ac9a203ebc1d49776968da0d61@localhost>");
        assertTrue(replyToEmail.getId().equals(email.getInReplyTo().getId()));
        assertEquals(email.getMailinglist(), "linux@lists.linux.sk");
        assertEquals(email.getThreadRoot().getId(), replyToEmail.getId());
        assertNull(email.getAttachments());
        assertEquals("text/plain", email.getMainContent().get(0).getType());
        assertTrue(email.getMainContent().get(0).getText().startsWith("Kedysika"));
        //assertEquals(((BasicBSONList) (testObj).get("replies")).size(), 1); V principe tam su, ale nie je v In-reply-to

        email =(Email) dbClient.findFirstMessageWithMessageId("<4F2A6865.3030805@lavabit.com>");
        assertTrue(email.getMessageId().equals("<4F2A6865.3030805@lavabit.com>"));
        replyToEmail = (Email) dbClient.findFirstMessageWithMessageId("<20120127193813.GG25134@athena.platon.sk>");
        Email rootEmail = (Email) dbClient.findFirstMessageWithMessageId("<CAJ37LfR9GUeEQ=EQJvvZ4BSoL489F=a2DUwAK1r4Ebb4tw=haA@mail.gmail.com>");
        assertTrue(email.getFrom().equals("rabgulo@lavabit.com"));
        assertTrue(email.getInReplyTo().getId().equals(replyToEmail.getId()));
        assertEquals(email.getThreadRoot().getId(), rootEmail.getId());
        assertEquals(email.getMailinglist(), "linux@lists.linux.sk");
        assertNull(email.getAttachments());
        assertEquals("text/plain", email.getMainContent().get(0).getType());
        assertTrue( email.getMainContent().get(0).getText().startsWith("On 27.01.2012 20:38"));

        email =(Email) dbClient.findFirstMessageWithMessageId("<20120214202407.GI6838@ksp.sk>");
        assertTrue(email.getMessageId().equals("<20120214202407.GI6838@ksp.sk>"));
        assertTrue(email.getFrom().equals("michal.petrucha@ksp.sk"));
        replyToEmail = (Email) dbClient.findFirstMessageWithMessageId("<20120203104407.GA27369@fantomas.sk>");
        rootEmail = (Email) dbClient.findFirstMessageWithMessageId("<CAJ37LfR9GUeEQ=EQJvvZ4BSoL489F=a2DUwAK1r4Ebb4tw=haA@mail.gmail.com>");
        assertEquals(email.getThreadRoot().getId(), rootEmail.getId());
        assertEquals(email.getMailinglist(), "linux@lists.linux.sk");
        assertTrue(email.getInReplyTo().getId().equals(replyToEmail.getId()));
        assertEquals(email.getAttachments().size(), 1);
        assertEquals("text/plain", email.getMainContent().get(0).getType());
        assertTrue(email.getMainContent().get(0).getText().startsWith("On Fri, Feb 03, 2012 at "));
        // as we dont save the "sign"

        email = (Email)dbClient.findFirstMessageWithMessageId("<20120203104407.GA27369@fantomas.sk>");

        replyToEmail = (Email) dbClient.findFirstMessageWithMessageId("<20120201114442.GX6838@ksp.sk>");
        rootEmail = (Email) dbClient.findFirstMessageWithMessageId("<CAJ37LfR9GUeEQ=EQJvvZ4BSoL489F=a2DUwAK1r4Ebb4tw=haA@mail.gmail.com>");
        assertTrue(email.getMessageId().equals("<20120203104407.GA27369@fantomas.sk>"));
        assertTrue(email.getFrom().equals("uhlar@fantomas.sk"));
        assertEquals(email.getMailinglist(), "linux@lists.linux.sk");
        assertEquals(email.getThreadRoot().getId(), rootEmail.getId());
        assertTrue(email.getInReplyTo().getId().equals(replyToEmail.getId()));
        assertEquals(1, email.getReplies().size());
        MiniEmail miniReply = email.getReplies().get(0);
        assertTrue(miniReply.getMessageSnippet().startsWith("On Fri, Feb 03, 2012 at 11:44:07AM +0100"));
        assertTrue(miniReply.getSubject().contains("Re: [linux] Aky filesystem pouzit? (bolo: Rychlost software RAIDu na Debiane)"));
        assertNull(email.getAttachments());
        assertEquals("text/plain", email.getMainContent().get(0).getType());
        assertTrue(email.getMainContent().get(0).getText().startsWith("On 01.02.12 12:44"));
        
        
        email=(Email) dbClient.findFirstMessageWithMessageId("<1361268460-3092-7-git-send-email-swhiteho@redhat.com>");
        assertNotNull(email);
        assertTrue(email.getMessageId().equals("<1361268460-3092-7-git-send-email-swhiteho@redhat.com>"));
        assertTrue(email.getFrom().equals("swhiteho@redhat.com"));
        replyToEmail = (Email) dbClient.findFirstMessageWithMessageId("<1361268460-3092-1-git-send-email-swhiteho@redhat.com>");
        rootEmail = (Email)dbClient.findFirstMessageWithMessageId("<1361268460-3092-1-git-send-email-swhiteho@redhat.com>");
        assertEquals(email.getThreadRoot().getId(), rootEmail.getId());
        assertEquals(email.getMailinglist(), "cluster-devel@redhat.com");
        assertTrue(email.getInReplyTo().getId().equals(replyToEmail.getId()));
        assertEquals("text/plain", email.getMainContent().get(0).getType());
        //assertTrue(email.getMainContent().get(0).getType().toString().startsWith("The freeze code "));
        
        email=(Email) dbClient.findFirstMessageWithMessageId("<1361885169.2705.12.camel@menhir>");
        assertNotNull(email);
        assertTrue(email.getMessageId().equals("<1361885169.2705.12.camel@menhir>"));
        assertTrue(email.getFrom().equals("swhiteho@redhat.com"));
        assertEquals(email.getThreadRoot(), null);
        assertEquals(email.getMailinglist(), "cluster-devel@redhat.com");
        assertNull(email.getInReplyTo());
        assertEquals("text/plain", email.getMainContent().get(0).getType());
        assertTrue(email.getMainContent().get(0).getText().startsWith("This patch cleans up "));
        
        email=(Email) dbClient.findFirstMessageWithMessageId("<1361268460-3092-6-git-send-email-swhiteho@redhat.com>");
        assertNotNull(email);
        assertTrue(email.getMessageId().equals("<1361268460-3092-6-git-send-email-swhiteho@redhat.com>"));
        assertTrue(email.getFrom().equals("swhiteho@redhat.com"));
        replyToEmail = (Email) dbClient.findFirstMessageWithMessageId("<1361268460-3092-1-git-send-email-swhiteho@redhat.com>");
        rootEmail = (Email)dbClient.findFirstMessageWithMessageId("<1361268460-3092-1-git-send-email-swhiteho@redhat.com>");
        assertEquals(email.getThreadRoot().getId(), rootEmail.getId());
        assertEquals(email.getMailinglist(), "cluster-devel@redhat.com");
        assertTrue(email.getInReplyTo().getId().equals(replyToEmail.getId()));
        assertEquals("text/plain", email.getMainContent().get(0).getType());
        assertTrue(email.getMainContent().get(0).getText().startsWith("The locking in gfs2_attach_bufdata() "));
        
        

        writeDBItems();

    }

    @Test
    public void testMessageReceiver() throws FileNotFoundException, IOException, MessagingException {
        InputStream old = System.in;
        String output;
        FileInputStream stream = new FileInputStream(new File(SIMPLE_MAIL_PATH));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            output = Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
        InputStream testInput = new ByteArrayInputStream(output.getBytes("UTF-8"));
        System.setIn(testInput);
        String[] args = {configuration.getDatabaseUrl(), 
        				 configuration.getDefaultDatabaseName(), 
        				 String.valueOf(configuration.getDefaultPort()), 
        				 configuration.getDefaultCollectionName(), 
        				 configuration.getUser(), 
        				 configuration.getPassword(),"false"};
        MessageReceiver.main(args);
        testInput.close();
        assertEquals(1, dbClient.emailCount());
        Email email = (Email) dbClient.findFirstMessageWithMessageId("<4E7CA9DA.9040904@gmail.com>");
        assertTrue(email.getMessageId().equals("<4E7CA9DA.9040904@gmail.com>"));
        assertTrue(email.getFrom().equals("martin.kyrc@gmail.com"));
    }
    
    @Test
    public void testSearchisko() throws NoSuchProviderException, MessagingException, IOException {
    	MboxImporter mbox = new MboxImporter(dbClient,false);
        mbox.importMbox(TEST_MAILS_PATH);
    	SearchManager manager = new SearchManager();
    	Email email = (Email) dbClient.findFirstMessageWithMessageId("<4E7CA9DA.9040904@gmail.com>");
    	manager.addEmail(email);    	
    	Email emailFromSearchisko = manager.getEmailFromSearchisko("mlonline_email-"+email.getId().toString());
    	System.out.println("mlonline_email-"+email.getId().toString());
    	assertEquals(email.getDate(), emailFromSearchisko.getDate());
    	assertEquals(email.getFrom(), emailFromSearchisko.getFrom());
    	assertEquals(email.getSubject(), emailFromSearchisko.getSubject());
    	manager.removeEmail("mlonline_email-"+email.getId().toString());    	
    }
                                                        
}
