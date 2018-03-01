/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglistonline.server.export.database.entities;

import com.mongodb.BasicDBObject;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bson.types.ObjectId;

/**
 * Entity used to save information about the content,attachments(files, signature etc.) of the email.
 * @author Matej Briškár
 * @author Július Staššík
 */
//@XmlRootElement(name = "ContentPart")
public class ContentPart {
	private String type;
	private String text;
	private String link;
	private ObjectId id;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
		
      
}
