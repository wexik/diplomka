package mailinglistonline.server.export.database.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import com.mongodb.BasicDBObject;

/**
 * Entity used to handle basic information about the email. This entity is good when only part of the information is needed.
 * The scenarios where this entity is useful:
 * 1. Basic information about the emails that the email is in connection with
 * 2. To provide information about the list of requested emails.
 * @author Matej Briškár
 * @author Július Staššík
 */
//@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class MiniEmail {

	private static final long serialVersionUID = 3749184648068897751L;
	
	public static final String ID_MONGO_TAG = "_id";
    public static final String MAILINGLIST_MONGO_TAG = "mailinglist";
    public static final String MESSAGE_ID_MONGO_TAG = "messageId";
    public static final String SUBJECT_MONGO_TAG = "subject";
    public static final String DATE_MONGO_TAG = "date";
    public static final String FROM_MONGO_TAG = "from";
    // message snippet is the first x letters from the email
    public static final String MESSAGE_SNIPPET_MONGO_TAG = "messageSnippet";
    public static final String TAGS_MONGO_TAG = "tags";
    public static final String HIGHLIGHTED = "highlighted";
        
    private ObjectId id;
    private String mailinglist;
    private String messageId;
    private String messageSnippet;
    private String subject;
    private Long date;
    private Map<String,List<String>> highlighted;    
	private String from;
    private List<String> tags;
    
        
    public MiniEmail() {        
    }
    
    /*
     * Needed when adding miniemail to mongodb
     */
    public MiniEmail(MiniEmail email) {
    	setId(email.getId());
    	setMailinglist(email.getMailinglist());
    	setMessageId(email.getMessageId());
    	setSubject(email.getSubject());
    	if (email.getDate() !=null) {
    		setDate(email.getDate());
    	}
    	setFrom(email.getFrom());
    	setMessageSnippet(email.getMessageSnippet());
    	setTags(email.getTags());
    }                                                   
    
    public void addHighLight(String key,String value) {
    	Map<String,List<String>> object = getHighlighted();
        if(object == null) {
           setHighlighted(new HashMap<String,List<String>>());
            object = getHighlighted();
        }
        if(object.get(key) == null ) {
        	object.put(key, new ArrayList<String>());
        }
        object.get(key).add(value);
	}
                            
    public void addTag(String tag) {
        List<String>list = getTags();
        if(list == null) {
            setTags(new ArrayList<String>());
            list = getTags();
        }
        list.add(tag);
    }
    
    public void removeTag(String tag) {
        List<String>list = getTags();
        if(list == null) {
            throw new IllegalArgumentException("Email does not contain tag '" + tag + "'.");
        }
        list.remove(tag);
    }

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getMailinglist() {
		return mailinglist;
	}

	public void setMailinglist(String mailinglist) {
		this.mailinglist = mailinglist;
	}	

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageSnippet() {
		return messageSnippet;
	}

	public void setMessageSnippet(String messageSnippet) {
		this.messageSnippet = messageSnippet;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public Map<String, List<String>> getHighlighted() {
		return highlighted;
	}

	public void setHighlighted(Map<String, List<String>> highlighted) {
		this.highlighted = highlighted;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}	  
	
	@Override
	public String toString() {
		return "MiniEmail [id=" + id + ", mailinglist=" + mailinglist + ", messageId=" + messageId + ", messageSnippet="
				+ messageSnippet + ", subject=" + subject + ", date=" + date + ", highlighted=" + highlighted
				+ ", from=" + from + ", tags=" + tags + "]";
	}
	
}