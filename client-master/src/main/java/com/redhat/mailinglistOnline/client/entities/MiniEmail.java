package com.redhat.mailinglistOnline.client.entities;

import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.codehaus.jackson.annotate.JsonGetter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

/**
 * Entity used to handle basic information about the email. This entity is good when only part of the information is needed.
 * 
 * @author Matej Briškár
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MiniEmail implements Serializable {

	private static final long serialVersionUID = 9069501944384263364L;
	public static final String ID_MONGO_TAG = "id";
	public static final String SEARCHISKO_MONGO_ID_TAG = "mongo_id";
	public static final String MAILINGLIST_MONGO_TAG = "mailinglist";
	public static final String MESSAGE_ID_MONGO_TAG = "messageId";
	public static final String SUBJECT_MONGO_TAG = "subject";
	public static final String DATE_MONGO_TAG = "date";
	public static final String FROM_MONGO_TAG = "from";
	// message snippet is the first x letters from the email
	public static final String MESSAGE_SNIPPET_MONGO_TAG = "messageSnippet";
	public static final String TAGS_MONGO_TAG = "tags";
	private static final String HIGHLIGHTED = "highlighted";
	public static final String DATE_OF_LAST_MESSAGE_ADDED_TO_THREAD_MONGO_TAG = "lastMessageDate";
	
	
	@XmlElement(name="id")
	private MyId myId;
			
	private String id;

	@XmlElement(name = MESSAGE_ID_MONGO_TAG)
	private String messageId;
	
	@XmlElement(name = SUBJECT_MONGO_TAG)
	private String subject;
	
	@XmlElement(name = DATE_MONGO_TAG)
	private long date;
	
	@XmlElement(name = FROM_MONGO_TAG)
	private String from;
	
	@XmlElement(name = MAILINGLIST_MONGO_TAG)
	private String mailinglist;
	
	@XmlElement(name = TAGS_MONGO_TAG)
	private List<String> tags = new ArrayList<String>();
	
	@XmlElement(name = MESSAGE_SNIPPET_MONGO_TAG)
	private String messageSnippet;
	
	@XmlElement(name = HIGHLIGHTED)
	private Map<String,List<String>> highlighted;

	public MiniEmail() {
		super();
	}

	/*
	 * Needed when adding miniemail to mongodb
	 */
	public MiniEmail(MiniEmail email) {
		setIid(email.getId());
		setMyId(email.getMyId());
		setMailinglist(email.getMailinglist());
		setMessageId(email.getMessageId());
		setSubject(email.getSubject());
		setDate(email.getDate());
		setFrom(email.getFrom());
		setMessageSnippet(email.getMessageSnippet());
		setTags(email.getTags());
	}
	
	/*
	 * TODO: Waiting to know how to make it work.. Jackson issue.
	 */
	//@JsonProperty(ID_MONGO_TAG)		
	public String getId() {
		return myId.getExpectedId();
	}

	//@JsonSetter(ID_MONGO_TAG)
	//@JsonProperty(ID_MONGO_TAG)	
	public void setIid(String id) {
		this.id = id;

	}
	
	public MyId getMyId() {
		return myId;
	}

	public void setMyId(MyId myId) {
		if (myId == null) return;
		this.myId = myId;
		this.id = getMyId().getExpectedId();
	}

	//@JsonProperty(HIGHLIGHTED)
    public Map<String,List<String>> getHighlighted() {
		return highlighted;
    }
    
	//@JsonProperty(HIGHLIGHTED)
    public void setHighLighted(Map<String,List<String>> highlighted) {
    	this.highlighted = highlighted;
	}
	
	@JsonProperty(SEARCHISKO_MONGO_ID_TAG)
	public void setMongoId(String id) {
		//this.id = id;
	}
	
	//@JsonProperty(MAILINGLIST_MONGO_TAG)
	public void setMailinglist(String mailinglist) {
		this.mailinglist = mailinglist;
	}

	//@JsonProperty(MESSAGE_ID_MONGO_TAG)
	public String getMessageId() {
		return messageId;
	}

	//@JsonProperty(MESSAGE_ID_MONGO_TAG)
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	//@JsonProperty(MESSAGE_SNIPPET_MONGO_TAG)
	public String getMessageSnippet() {
		return messageSnippet;
	}

	//@JsonProperty(MESSAGE_SNIPPET_MONGO_TAG)
	public void setMessageSnippet(String messageSnippet) {
		this.messageSnippet = messageSnippet;
	}

	//@JsonProperty(SUBJECT_MONGO_TAG)
	public String getSubject() {
		return subject;
	}

	//@JsonProperty(SUBJECT_MONGO_TAG)
	public void setSubject(String subject) {
		this.subject = subject;
	}

	//@JsonProperty(DATE_MONGO_TAG)
	public long getDate() {
		return date;
	}
	//@JsonProperty(DATE_MONGO_TAG)
	public void setDate(long sentDate) {
		this.date = sentDate;
	}

	//@JsonProperty(MAILINGLIST_MONGO_TAG)
	public String getMailinglist() {
		return mailinglist;
	}

	//@JsonProperty(FROM_MONGO_TAG)
	public String getFrom() {
		return from;
	}

	//@JsonProperty(FROM_MONGO_TAG)
	public void setFrom(String from) {
		this.from = from;
	}

	//@JsonProperty(TAGS_MONGO_TAG)
	public List<String> getTags() {
		return tags;
	}

	//@JsonProperty(TAGS_MONGO_TAG)
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public void addTag(String tag) {
		if (tags.isEmpty()) {
			tags = new ArrayList<String>();
		}
		tags.add(tag);
	}
	
	public String getReadableDate() {
		Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	    return format.format(getDate()); 
	}
}
