package com.redhat.mailinglistOnline.client.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Entity used to save information about the content,attachments(files, signature etc.) of the email.
 * 
 * @author Matej Briškár
 */
@XmlRootElement(name = "ContentPart")
public class ContentPart{

	private static final long serialVersionUID = 7103483924512914564L;
	@XmlElement(name = "type")
	private String type;
	@XmlElement(name = "text")
    private String content;
	@XmlElement(name = "link")
    private String link;
	
	@XmlElement(name="id")
	private MyId myId;
	
	private String id;
	
	public String getId() {
		return myId.getExpectedId();
	}
	
	public MyId getMyId() {
		return myId;
	}
	public void setMyId(MyId myId) {
		if (myId == null) return;
		this.myId = myId;
		this.id = getMyId().getExpectedId();
	}
    
    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type=type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content=content;
    }
    
    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
    	this.link=link;
    }
    
    
}