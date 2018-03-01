package mailinglistonline.server.export.importing;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.mongodb.MongoClient;

import mailinglistonline.server.export.database.DbClient;
import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.searchisko.SearchClient;
import net.fortuna.mstor.MStorFolder;

public class MboxImporter {

	private DbClient messageSaver;
	private boolean saveAlsoToSearchisko;
	private SearchClient searchManager;
	final static Logger logger = Logger.getLogger(MboxImporter.class);
	
	public MboxImporter(DbClient msgSaver, boolean saveAlsoToSearchisko, SearchClient searchManager) throws UnknownHostException {
        this.saveAlsoToSearchisko=saveAlsoToSearchisko;
    	messageSaver = msgSaver;
    	this.searchManager = searchManager;
    }
	
	public void importMbox(String mboxPath) throws NoSuchProviderException, MessagingException, IOException {
    	if(! (mboxPath.endsWith(".mbox")|| mboxPath.endsWith(".txt") ) ) {
    		System.err.println("File " + mboxPath + " doesn't end with .mbox nor .txt");
    		logger.error("File " + mboxPath + " doesn't end with .mbox nor .txt");
    		return;
    	}
        File file = new File(mboxPath);
        String mboxFile = file.getName();
        String mboxDirectory = file.getParentFile().getAbsolutePath();
        Properties props = new Properties();
        props.setProperty("mstor.mbox.metadataStrategy", "none");
        Session session = Session.getDefaultInstance(props);
        Store store = session.getStore(new URLName("mstor:" + mboxDirectory));
        store.connect();
        MStorFolder inbox = (MStorFolder) store.getDefaultFolder().getFolder(mboxFile);
        inbox.open(Folder.READ_ONLY);
        Message[] messages = inbox.getMessages();
        System.out.println("Importing" + messages.length + " messages.");
        logger.info("Importing" + messages.length + " messages.");
        MessageManager manager = new MessageManager(messageSaver,saveAlsoToSearchisko, searchManager);
        int i = 0;
        for (Message m : messages) {
        		i++;        		
                manager.createAndSaveMessage((MimeMessage) m);
        }
        List<Email> emails = messageSaver.getMailinglistRoot(Pattern.compile(".*"));        
        for(Email email : emails) {        	
        	String msgId = "";
        	if(email.getLastMessageId() != null && email.getLastMessageId().charAt(0) == 'a'
    				&& email.getLastMessageId().charAt(1) == '<') {
        		msgId = email.getLastMessageId().substring(1);
        	}        	
        	if(msgId == null || msgId.equals("")) continue;
        	Email e = messageSaver.findFirstMessageWithMessageId(msgId);
        	if(e != null) {
        		email.setLastMessageId(e.getId().toString());
        		messageSaver.updateEmail(email);
        	}
        }
        store.close();
        System.out.println("Done.");
        logger.info("Done.");
    }
	
}

