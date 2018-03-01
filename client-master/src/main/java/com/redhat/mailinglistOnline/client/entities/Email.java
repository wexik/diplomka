package com.redhat.mailinglistOnline.client.entities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * The main entity used to store the emails.
 * 
 * @author Matej Briškár
 * @author Július Staššík
 */

@XmlRootElement(name = "email")
public class Email extends MiniEmail{

	private static final long serialVersionUID = 9122202937488854481L;
	public static final String ROOT_MONGO_TAG = "threadRoot";
    public static final String IN_REPLY_TO_MONGO_TAG = "inReplyTo";
    public static final String REPLIES_MONGO_TAG = "replies";
    public static final String ATTACHMENTS_MONGO_TAG = "attachments";
    public static final String MAIN_CONTENT_MONGO_TAG = "mainContent";
    public static final String SHARD_KEY_MONGO_TAG = "emailShardKey";
    public static final String DATE_OF_LAST_MESSAGE_ADDED_TO_THREAD_MONGO_TAG = "lastMessageDate";
    public static final String ID_OF_LAST_MESSAGE = "lastMessageId";
    
    @XmlElement(name = ROOT_MONGO_TAG)
    private MiniEmail root;
    
    @XmlElement(name = IN_REPLY_TO_MONGO_TAG)
    private MiniEmail inReplyTo;
    
    @XmlElement(name = REPLIES_MONGO_TAG)
    private List<MiniEmail> replies= new ArrayList<MiniEmail>();
    
    @XmlElement(name = MAIN_CONTENT_MONGO_TAG)
    private List<ContentPart> mainContent=new ArrayList<ContentPart>();
    
    @XmlElement(name = ATTACHMENTS_MONGO_TAG)
    private List<ContentPart> attachments =new ArrayList<ContentPart>();
    
    @XmlElement(name = SHARD_KEY_MONGO_TAG)
    private String shardKey;
    
    @XmlElement(name = DATE_OF_LAST_MESSAGE_ADDED_TO_THREAD_MONGO_TAG)
    private Long lastMessageDate;
    
    @XmlElement(name = ID_OF_LAST_MESSAGE)
    private String lastMessageId;
        
	public Email() {
        super();
    }       
        
    public MiniEmail getRoot() {
        return root;
    }
    
    public void setRoot(MiniEmail root) {
        this.root=root;
    }
    
    public Long getLastMessageDate() {
		return lastMessageDate;
	}

	public void setLastMessageDate(Long lastMessageDate) {
		this.lastMessageDate = lastMessageDate;
	}
	
	public String getLastMessageId() {
		return lastMessageId;
	}

	public void setLastMessageId(String lastMessageId) {
		this.lastMessageId = lastMessageId;
	}
	
    public MiniEmail getInReplyTo() {
        return inReplyTo;
    }
        
    public void setInReplyTo(MiniEmail inReplyTo) {
        this.inReplyTo=inReplyTo;
    }

        
    public void setReplies(List<MiniEmail> replies) {
    	this.replies=replies;
    }

    public void addReply(MiniEmail reply) {
        if(replies == null) {
        	replies = new ArrayList<MiniEmail>();
        }
        replies.add(new MiniEmail(reply));
    }
       
    public List<MiniEmail> getReplies() {
       return replies;
    }

    public void addAttachment(ContentPart part) {
        if(attachments == null) {
        	attachments =new ArrayList<ContentPart>();
        }
        attachments.add(part);
    }
        
    public void setAttachments(List<ContentPart> attachments) {
    	this.attachments=attachments;
    }
    
    public List<ContentPart> getMainContent() {
        return mainContent;
    }
    
    public void setMainContent(List<ContentPart> mainContent) {
         this.mainContent=mainContent;
    }
        
    public List<ContentPart> getAttachments() {
        return attachments;
    }
    
    public String getDateInString() {
    	DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    	return df.format(new Date(getDate()));
    }
        
    public String getShardKey() {
        return shardKey;
    }
    
    public void setShardKey(String key) {
        this.shardKey=key;
    }
    
}


