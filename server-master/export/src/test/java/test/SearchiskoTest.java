package test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mailinglistonline.server.export.database.DatabaseConfiguration;
import mailinglistonline.server.export.database.MongoDbClient;
import mailinglistonline.server.export.database.entities.ContentPart;
import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.MiniEmail;
import mailinglistonline.server.export.searchisko.SearchManager;
import mailinglistonline.server.export.util.PropertiesParser;

public class SearchiskoTest {
	
	private MongoDbClient dbClient;
	private SearchManager manager;
	
	public SearchiskoTest(){
		DatabaseConfiguration configuration = PropertiesParser.parseDatabaseConfigurationFile(MongoDbClient.class
				.getClass()
				.getResource((MongoDbClient.DATABASE_PROPERTIES_FILE_NAME))
				.getPath());
        configuration.setDefaultCollectionName("test");
        configuration.setDatabaseUrl("127.0.0.1");
        dbClient = new MongoDbClient(configuration);
        dbClient.dropTable();
        manager = new SearchManager();
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
	public void searchTest() throws IOException, InterruptedException {	   
	   Email email1 = createEmail("1");    
	   dbClient.saveMessage(email1);	   
	   manager.addEmail(email1);
	   Thread.sleep(2000);	   	   
	   List<MiniEmail> found = manager.searchByContent("xxxskuskaxxx");	   
	   assertEquals(found.get(0).getId().toString(), email1.getId().toString());
	   manager.removeEmail("mlonline_email-" + email1.getId().toString());
	   Thread.sleep(2000);	
	}
   
   @Test
   public void tagFiltrationTest() throws IOException, InterruptedException {
	   Email email = createEmail("1");
	   Email email2 = createEmail("2");
	   String tag = "supertestovacitagnadtag";
	   List<String> tags = new ArrayList<String>();
	   tags.add(tag);
	   email.setTags(tags);
	   dbClient.saveMessage(email);
	   dbClient.saveMessage(email2);	   
	   manager.addEmail(email);	    
	   Thread.sleep(2000);	   	   
	   manager.addEmail(email2);	   
	   Thread.sleep(2000);	   
	   List<MiniEmail> found = manager.searchByContentFilteredByTags("xxxskuskaxxx", tags);
	   Thread.sleep(2000);	   
	   assertEquals(1, found.size());
	   manager.removeEmail("mlonline_email-" + email.getId().toString());
	   manager.removeEmail("mlonline_email-" + email2.getId().toString());	  
	   Thread.sleep(2000);	
   }
   
   public Email createEmail(String s) {
	   Email email1 = new Email();
       email1.setFrom("from1338@from1338.sk" + s);
       email1.setMessageId("message1testik" + s);
       email1.setThreadRoot(null);
       email1.setMailinglist("mailinglist1@mailinglist1.sk" + s);
       ContentPart mainContent = new ContentPart();
       mainContent.setType("text/plain");
       mainContent.setText("testicok xxxskuskaxxx test testik"+ s);
       List<ContentPart> lcp = new ArrayList<ContentPart>();
       lcp.add(mainContent);
       email1.setMainContent(lcp);   
       return email1;
   }

}
