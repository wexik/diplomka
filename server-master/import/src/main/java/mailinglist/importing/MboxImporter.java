/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist.importing;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

import mailinglist.MessageManager;
import mailinglistonline.server.export.database.DatabaseConfiguration;
import mailinglistonline.server.export.database.DbClient;
import mailinglistonline.server.export.database.MongoDbClient;
import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.util.PropertiesParser;
import net.fortuna.mstor.MStorFolder;

/**
 *	Class capable of importing the emails saved in the .mbox files into the database.
 *
 * @author Matej Briškár
 */
public class MboxImporter {

    private static String DATABASE_PROPERTIES_FILE_NAME = "database.properties";
	private DbClient messageSaver;
	private boolean saveAlsoToSearchisko;	
	final static Logger logger = Logger.getLogger(MboxImporter.class);
    /**
     * Method callable from terminal, called with the path specifying the folder of mbox files or an mbox file itself.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NoSuchProviderException, MessagingException, IOException {
    	
    	MongoDbClient msgSaver =new MongoDbClient(PropertiesParser.parseDatabaseConfigurationFile(
    			MboxImporter.class.getClassLoader().getResource((DATABASE_PROPERTIES_FILE_NAME)).getPath()));
        MboxImporter mbox = new MboxImporter(msgSaver,true);
        if(!args[0].contains("/")) {
        	args[0]="./"+args[0];
        }

        if (args.length == 1) {
            File file = new File(args[0]);
            if (file.isDirectory()) {
                mbox.importMboxDirectory(args[0]);
            } else {
                mbox.importMbox(args[0]);
            }
            msgSaver.closeConnection();
        } else {
            System.out.println("Call the method with one parameter (mbox path)");
            logger.info("Call the method with one parameter (mbox path)");
        }

    }

    public MboxImporter(DbClient msgSaver, boolean saveAlsoToSearchisko) throws UnknownHostException {
        this.saveAlsoToSearchisko=saveAlsoToSearchisko;
    	messageSaver = msgSaver;
    }

    public void importMboxDirectory(String directoryPath) throws NoSuchProviderException, IOException, MessagingException {
    	ExecutorService executor =Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    	File directory = new File(directoryPath);
        File[] subfiles = directory.listFiles();
        String mboxDirectory = directoryPath;
        Properties props = new Properties();
        props.setProperty("mstor.mbox.metadataStrategy", "none");
        Session session = Session.getDefaultInstance(props);
        final MessageManager manager = new MessageManager(messageSaver,saveAlsoToSearchisko);
        Store store = session.getStore(new URLName("mstor:" + mboxDirectory));
        store.connect();
        for (final File file : subfiles) {
            if (file.isDirectory()) {
                importMboxDirectory(file.getAbsolutePath());
            } else {
            	executor.execute(new Runnable() {
                    public void run() {
                    	try {
							importMbox(file.getAbsolutePath());
						} catch (Exception e) {
							System.err.println("Unable to import Mbox file: " + file.getAbsolutePath());
							logger.error("Unable to import Mbox file: " + file.getAbsolutePath());
						}
                    }
                });
            	
            }
           
        }
        executor.shutdown();
        boolean executorFinished=false;
        try {
        	executorFinished=executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
         } catch (InterruptedException ex) {
         } 
        if(!executorFinished) {
        	System.err.println("Executor was interrupted or the operation took too long. Not all the emails were saved.");
        	logger.error("Executor was interrupted or the operation took too long. Not all the emails were saved.");
        }
        store.close();
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
        MessageManager manager = new MessageManager(messageSaver,saveAlsoToSearchisko);
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
