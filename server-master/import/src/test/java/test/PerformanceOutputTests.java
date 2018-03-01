package test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

import mailinglist.importing.MboxImporter;
import mailinglistonline.server.export.database.DatabaseConfiguration;
import mailinglistonline.server.export.database.DbClient;
import mailinglistonline.server.export.database.MongoDbClient;
import mailinglistonline.server.export.util.PropertiesParser;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * This tests do not run from the maven test phase, because they end with 'Tests'. These classes are just giving the output
 * of the importing part, which has no limitations right now (=> no assertions).
 */
public class PerformanceOutputTests {

    public static final String TEST_MAILS_PATH = "src/test/java/mboxes/test-mails.mbox";
    public static final String TEST_MAILS_PATH2 ="src/test/java/mboxes/test-mails2";
    public static final String SIMPLE_MAIL_PATH ="src/test/java/mboxes/simpleMail";
    public static final String MBOX_FOLDER_PATH ="src/test/java/mboxes/folder";
    private DbClient dbClient;
    private DatabaseConfiguration configuration;

    public PerformanceOutputTests() throws UnknownHostException {
    	long before = System.currentTimeMillis();
        configuration = PropertiesParser.parseDatabaseConfigurationFile(MboxImporter.class
				.getClass()
				.getResource((MongoDbClient.DATABASE_PROPERTIES_FILE_NAME))
				.getPath());
        configuration.setDefaultCollectionName(ImportingTest.TEST_COLLECTION_NAME);
        dbClient = new MongoDbClient(configuration);
        dbClient.dropTable();
        long after = System.currentTimeMillis();
        System.out.println("The DBclient initialization took " + (after-before) + "ms");
        Logger.getLogger(PerformanceOutputTests.class.getName()).log(Level.INFO, null, 
        		"The DBclient initialization took " + (after-before) + "ms");
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        dbClient.dropTable();
    }

    @Test
    public void testMboxNumberOfMessages() throws UnknownHostException, NoSuchProviderException, MessagingException, IOException {
    	long before = System.currentTimeMillis();
    	MboxImporter mbox = new MboxImporter(dbClient,false);
    	for (int i =0;i<10;i++) {
    		mbox.importMbox(TEST_MAILS_PATH);
            tearDown();
    	}
    	long after = System.currentTimeMillis();
    	System.out.println("The importing took " + (after-before) + "ms");
    	Logger.getLogger(PerformanceOutputTests.class.getName()).log(Level.INFO, null, 
    			"The importing took " + (after-before) + "ms");
    }

}
