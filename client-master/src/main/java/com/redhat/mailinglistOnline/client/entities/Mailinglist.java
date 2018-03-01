package com.redhat.mailinglistOnline.client.entities;

import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ViewScoped;
import javax.inject.Named;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Entity used to handle information about the mailinglists being processed.
 * 
 * @author Matej Briškár
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement(name = "mailinglist")
public class Mailinglist{	
		
	private String name;
	
	private String description;
			
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}