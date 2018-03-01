/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.util.SharedByteArrayInputStream;

import mailinglistonline.server.export.database.DbClient;
import mailinglistonline.server.export.database.MongoDbClient;
import mailinglistonline.server.export.database.entities.ContentPart;
import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.Mailinglist;
import mailinglistonline.server.export.searchisko.SearchClient;
import mailinglistonline.server.export.util.PropertiesParser;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import searchisko.SearchManagerProxy;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;
import xyz.capybara.clamav.commands.scan.result.ScanResult.Status;

import com.mongodb.MongoClient;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64DecoderStream;

import exceptions.MalformedMessageException;

/**
 *	Object used to create an object from the data provided in the {@link MimeMessage} instance. Accessing the searchClient to send information to the Search provider and also
 * the DbClient to save the data in the database.
 * 
 * @author Matej Briškár
 */
public class MessageManager {

    private DbClient dbClient;
    private SearchClient searchManager;
    private final ArrayList<String> mailingLists;
	private boolean sendMessagesToSearchisko=true;
	private ClamavClient client;
    private static String MAILINGLISTS_PROPERTIES_FILE_NAME = "mailinglists.properties";
    private static String CLAMAV_PROPERTIES_PROPERTIES_FILE_NAME = "clamav.properties";    
    final static Logger logger = Logger.getLogger(MessageManager.class);
    
    public MessageManager(DbClient dbClient, boolean sendMessagesToSearchisko) throws IOException {
    	this(dbClient);
    	this.sendMessagesToSearchisko=sendMessagesToSearchisko;
    }
    public MessageManager(DbClient dbClient) throws IOException {
        this.dbClient = dbClient;
        searchManager = new SearchManagerProxy();
        mailingLists = new ArrayList<String>();        
        List<Mailinglist> mls = dbClient.getMailingLists();        
        for(Mailinglist ml : mls) {        	
        	mailingLists.add(ml.getName());
        }
        List<String> props = PropertiesParser.parseClamavProperties
        						(MessageManager.class.getClassLoader().getResourceAsStream
        								(CLAMAV_PROPERTIES_PROPERTIES_FILE_NAME));
        client = new ClamavClient(props.get(0), Integer.parseInt(props.get(1)));
    }


    /*
     * List of emails can be returned if there are more found mailinglists in header
     * for the processed email
     */
    public List<Email> createMessage(MimeMessage message) throws MessagingException, IOException, MalformedMessageException {
        if (message.getMessageID() == null && message.getFrom() == null) {
            throw new MalformedMessageException();
        }
        Email email = new Email();
        email.setMessageId(message.getMessageID());
        if(message.getSentDate() != null) {
        	email.setDate(message.getSentDate().getTime());
        }
        String fromField = extractEmailAddress(message.getFrom()[0].toString());
        email.setFrom(fromField);
        Map<String, List<ContentPart>> list = getContentParts(message,false);
        email.setSubject(message.getSubject());
        email.setMainContent(list.get("main"));
        if(!list.get("attachments").isEmpty()) {
             email.setAttachments(list.get("attachments"));
        }
        Address[] addresses = message.getAllRecipients();
        List<String> stringAddresses = new ArrayList<String>();
        List<String> mailingListAddresses = new ArrayList<String>();
        for (Address ad : addresses) {
            InternetAddress iad = (InternetAddress) ad;
            stringAddresses.add(iad.getAddress());
            if (mailingLists.contains(iad.getAddress().toLowerCase())) {
            	mailingListAddresses.add(iad.getAddress().toLowerCase());
            }
        }
        if (mailingListAddresses.isEmpty()) {
            System.out.println("Not found mailinglist in addresses :" + stringAddresses);
            logger.info("Not found mailinglist in addresses :" + stringAddresses);
            System.out.println("Email with messageId " + email.getMessageId() + " was not registered");
            logger.info("Email with messageId " + email.getMessageId() + " was not registered");
            return null;
        }
       
        // now create a clone of the email for each mailinglist
        List<Email> emails = new ArrayList<Email>();
        for (String mailinglist: mailingListAddresses) {
        	Email clone = new Email(email);
        	//mailinglist-specific data setters
        	clone.setMailinglist(mailinglist);
        	if (message.getHeader("In-Reply-To") != null) {
        		setInReplyToFor(clone, message.getHeader("In-Reply-To")[0],clone.getMailinglist());
        	}
        	setRootFor(clone);
        	emails.add(clone);
        }
        return emails;

    }
    
    private void setRootFor(Email email) {
    	if (email.getInReplyTo() != null) {
            Email parent =(Email) dbClient.getMessage(email.getInReplyTo().getId().toString());
            if (parent.getThreadRoot() == null) {
                email.setThreadRoot(email.getInReplyTo());                
                if(parent.getLastMessageDate() != null) {
	                if(parent.getLastMessageDate() < email.getDate() && email.getDate() != null) {
	                	parent.setLastMessageDate(email.getDate());
	                	parent.setLastMessageId("a"+email.getMessageId());
	                	try {
							dbClient.updateEmail(parent);
						} catch (IOException e) {						
							logger.error(e);
						}
	                }
                }                
            } else {
                email.setThreadRoot(parent.getThreadRoot());                 
                Email root = (Email) dbClient.getMessage(parent.getThreadRoot().getId().toString());
                if(root.getLastMessageDate() != null) {
                	if(root.getLastMessageDate() < email.getDate() && email.getDate()!= null) {
                		root.setLastMessageDate(email.getDate());
                		root.setLastMessageId("a"+email.getMessageId());
                		try {
							dbClient.updateEmail(root);
						} catch (IOException e) {							
							logger.error(e);
						}
                	}
                }
                	                
            }
        } else {
        	// if it is a root email, it does not point to another email
            email.setThreadRoot(null);
            email.setLastMessageDate(email.getDate());
        }
        
    }           

    private String extractEmailAddress(String address) {
        Pattern emailPattern = Pattern.compile("\\S+@\\S+");
        Matcher matcher = emailPattern.matcher(address);
        if (matcher.find()) {
            String email = matcher.group();
            if (email.startsWith("<") && email.endsWith(">")) {
                email = email.substring(1, email.length() - 1);
            }
            return email;
        }
        return null;

    }
    
    private void setInReplyToFor(Email email, String inReplyToMessageID,String mailingListAddress) {
    	String inReplyToMessageIdParsed = inReplyToMessageID.substring(0, inReplyToMessageID.indexOf(">")+1);    	
    	String inReplyToID = dbClient.getId(inReplyToMessageIdParsed, mailingListAddress);
        if(inReplyToID != null) {
            email.setInReplyTo(dbClient.getMessage(inReplyToID));
        } else {
            email.setInReplyTo(null);
        }
    }

    public boolean saveMessage(Email message, int atempts) throws MessagingException, IOException {
    	if (dbClient.saveMessage(message)) {     		
    		if(!sendMessagesToSearchisko) {
    			return true;
    		}
    		if ( searchManager.addEmail(message)) {
    			return true;
    		} else {
    			if(atempts != 3) {
    				dbClient.deleteMessage(message);
    			}
    			return false;
    		}
    	} else {    		
    		return false;
    	}
    }

    private Map<String, List<ContentPart>> getContentParts(Part p, boolean mainPartFound) throws
            MessagingException, IOException {
        Map<String, List<ContentPart>> list = new HashMap();
        list.put("main", new ArrayList<ContentPart>());
        list.put("attachments", new ArrayList<ContentPart>());
        if(!Part.ATTACHMENT.equals(p.getDisposition())) {
        if (p.isMimeType("text/*")) {
            String s = (String) p.getContent();
            if (p.isMimeType("text/html")) {
                ContentPart cp = new ContentPart();
                cp.setType("text/html");
                cp.setText(s);
                if (!mainPartFound) {
                    list.get("main").add(cp);
                } else {
                    list.get("attachments").add(cp);
                }
                return list;
            } else {
                ContentPart cp = new ContentPart();
                cp.setType("text/plain");
                cp.setText(s);
                if (!mainPartFound) {
                    list.get("main").add(cp);
                } else {
                    list.get("attachments").add(cp);
                }
                return list;
            }

        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    ContentPart cp = new ContentPart();
                    cp.setType("text/plain");
                    cp.setText(bp.getContent().toString());
                    if (!mainPartFound) {
                        list.get("main").add(cp);
                    } else {
                        list.get("attachments").add(cp);
                    }
                } else if (bp.isMimeType("text/html")) {
                    ContentPart cp = new ContentPart();
                    cp.setType("text/html");
                    cp.setText(bp.getContent().toString());
                    if (!mainPartFound) {
                        list.get("main").add(cp);
                    } else {
                        list.get("attachments").add(cp);
                    }
                } else {
                    if (!mainPartFound) {
                        list.get("main").addAll(getContentParts(bp,mainPartFound).get("main"));
                    } else {
                        list.get("attachments").addAll(getContentParts(bp,mainPartFound).get("attachments"));;
                    }
                }
            }
            mainPartFound=true;
            return list;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                if (!mainPartFound) {
                        list.get("main").addAll(getContentParts(mp.getBodyPart(i),mainPartFound).get("main"));
                        mainPartFound=true;
                    } else {
                        list.get("attachments").addAll(getContentParts(mp.getBodyPart(i),mainPartFound).get("attachments"));
                    }
            }
        }
        } else {
        	ContentPart cp = new ContentPart();
        	int indexForParsingContentType= p.getContentType().indexOf("name=");
        	String parsedContentType = null;
        	if (indexForParsingContentType != -1) {
   			 parsedContentType = p.getContentType().substring(0,indexForParsingContentType-2);
        	} else {
        		parsedContentType=p.getContentType();
        	}        	
        	cp.setType(parsedContentType);
        	if(p.getContent() instanceof BASE64DecoderStream) {
        		BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream)p.getContent();
        		 byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);
        		 //byte[] encodeBase64 = Base64.encodeBase64(byteArray);
        		 int indexOfFileName = p.getContentType().indexOf("name=");
        		 String fileName = null;
        		 if (indexOfFileName != -1) {
        			 fileName=p.getContentType().substring(indexOfFileName+6);
        			 fileName=fileName.substring(0,fileName.length()-1);
        		 }        		 
        		 //ScanResult result = client.scan(new ByteArrayInputStream(byteArray));
        		 //Status status = result.component1();
        		 //if( status.equals(Status.OK)) {
        		 String fileId=dbClient.createFile(byteArray,fileName,parsedContentType);
        		 cp.setLink(fileId);
        		 //}        		 
        	} else if(parsedContentType.contains("application/pgp-signature")){
        		SharedByteArrayInputStream sbais = (SharedByteArrayInputStream) p.getContent();        		
        		//MimeUtility.decode((InputStream) p.getContent(), "7bit");
        		byte[] byteArray =  new byte[sbais.available()];
        		sbais.read(byteArray);          		
        		//ScanResult result = client.scan(new ByteArrayInputStream(byteArray));
       		 	//Status status = result.component1();
        		//if( status.equals(Status.OK)) {
        		String fileId=dbClient.createFile(byteArray,"signature.asc",parsedContentType);
       		 	cp.setLink(fileId);
       		 	//}
        	}     	        
        	list.get("attachments").add(cp);
        }

        return list;
    }

    public boolean createAndSaveMessage(MimeMessage mimeMessage) {
        try {
            List<Email> messages = createMessage(mimeMessage);
            if (messages != null && !messages.isEmpty()) {
            	for (Email message : messages) {            		
            		for(int i = 0; i < 3; i++) {
            			if(saveMessage(message, i)) {
            				break;
            			} else {
            				if(i != 3) {	            				
            				} else {
            					logger.error("This message wasnt save to searchisko id: " + message.getId());            					
            				}
            			}
            		}
            	}
            }
            return true;
        } catch (MessagingException ex) {
        	logger.error(ex);
        } catch (IOException ex) {
        	logger.error(ex);
        } catch (MalformedMessageException ex) {
        	logger.error(ex);
        }
        return false;
    }
}
