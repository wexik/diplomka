/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglistonline.server.export.database.entities;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * The main entity used to store the emails.
 * 
 * @author Matej Briškár
 * @author Július Staššík
 */
public class Email extends MiniEmail {

	private static final int MESSAGE_SNIPPET_LENGTH = 150;
	public static final String ROOT_MONGO_TAG = "threadRoot";
	public static final String IN_REPLY_TO_MONGO_TAG = "inReplyTo";
	public static final String REPLIES_MONGO_TAG = "replies";
	public static final String ATTACHMENTS_MONGO_TAG = "attachments";
	public static final String MAIN_CONTENT_MONGO_TAG = "mainContent";
	public static final String SHARD_KEY_MONGO_TAG = "emailShardKey";
	public static final String DATE_OF_LAST_MESSAGE_ADDED_TO_THREAD_MONGO_TAG = "lastMessageDate";
	public static final String ID_OF_LAST_MESSAGE = "lastMessageId";
	
	private MiniEmail threadRoot;
	private String lastMessageId;
	private Long lastMessageDate;
	private MiniEmail inReplyTo;
	private List<MiniEmail> replies;
	private String emailShardKey;
	private List<ContentPart> attachments;
	private List<ContentPart> mainContent;		
	
	public Email() {
		super();
		//replies = new ArrayList<MiniEmail>();
	}	
	
	public Email(Email email) {
		super(email);		
		threadRoot = email.getThreadRoot();
		lastMessageDate = email.getLastMessageDate();
		lastMessageId = email.getLastMessageId();
		inReplyTo = email.getInReplyTo();
		replies = email.getReplies();
		emailShardKey = email.getEmailShardKey();
		attachments = email.getAttachments();
		mainContent = email.getMainContent();
	}
						

	public void setMainContent(List<ContentPart> mainContent) {
		String mainText = mainContent.get(0).getText();
		String snippet = mainText.substring(0,
		Math.min(mainText.length(), MESSAGE_SNIPPET_LENGTH));
		this.mainContent = mainContent;
		this.setMessageSnippet(snippet);
	}	

	public MiniEmail getThreadRoot() {
		return threadRoot;
	}

	public void setThreadRoot(MiniEmail threadRoot) {
		this.threadRoot = threadRoot;
	}

	public String getLastMessageId() {
		return lastMessageId;
	}

	public void setLastMessageId(String lastMessageId) {
		this.lastMessageId = lastMessageId;
	}
	public Long getLastMessageDate() {
		return lastMessageDate;
	}

	public void setLastMessageDate(Long lastMessageDate) {
		this.lastMessageDate = lastMessageDate;
	}

	public MiniEmail getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(MiniEmail inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	public List<MiniEmail> getReplies() {
		return replies;
	}

	public void setReplies(List<MiniEmail> replies) {
		this.replies = replies;
	}

	public String getEmailShardKey() {
		return emailShardKey;
	}

	public void setEmailShardKey(String emailShardKey) {
		this.emailShardKey = emailShardKey;
	}

	public List<ContentPart> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<ContentPart> attachments) {
		this.attachments = attachments;
	}

	public List<ContentPart> getMainContent() {
		return mainContent;
	}

	public void addReply(MiniEmail reply) {
		List<MiniEmail> list =  getReplies();
		if (list == null) {
			setReplies(new ArrayList<MiniEmail>());
			list = getReplies();
		}
		list.add(new MiniEmail(reply));
	}

	public void removeReply(MiniEmail email) {
		List<MiniEmail> list = getReplies();
		if (list == null) {
			setReplies(new ArrayList<MiniEmail>());
			list = getReplies();
		}
		list.remove(email);

	}
	
	public void addAttachment(ContentPart part) {
		List<ContentPart> list = getAttachments();
		if (list == null) {
			setAttachments(new ArrayList<ContentPart>());
			list = getAttachments();
		}
		list.add(part);
	}
	
	@Override
	public String toString() {
		return super.toString() + "Email [threadRoot=" + threadRoot + ", lastMessageId=" + lastMessageId + ", lastMessageDate="
				+ lastMessageDate + ", inReplyTo=" + inReplyTo + ", replies=" + replies + ", emailShardKey="
				+ emailShardKey + ", attachments=" + attachments + ", mainContent=" + mainContent + "]";
	}
	
}
