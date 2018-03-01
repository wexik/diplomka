package com.redhat.mailinglistOnline.client.responses;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import com.mongodb.MongoClient;
import com.redhat.mailinglistOnline.client.entities.Mailinglist;
import com.redhat.mailinglistOnline.client.rest.RestClient;

/**
 * A response from the server containing the processed mailinglists.
 * 
 * @author Matej Briškár
 * @author Július Staššík
 */
@Named("mailinglists")
@ViewScoped
public class MailingListsResponse implements Serializable {
	
	private static final long serialVersionUID = 457982468524356819L;
	public static List<Mailinglist> mailingLists;
	public final static String ALL_MAILINGLISTS="all";
	private Mailinglist newMailinglist;	
	private String name;
	private String description;

	final static Logger logger = Logger.getLogger(MailingListsResponse.class);
	
	@Inject
	RestClient client;
	
	//@PostConstruct Issue 19, sessions are constantly (about 4 per second) created when deployed to scaled app.
	public void init() {
		mailingLists=client.getAllMailingLists();
		Mailinglist allMailinglist = new Mailinglist();
		allMailinglist.setName(ALL_MAILINGLISTS);
		allMailinglist.setDescription("Mailinglist representing all the mailinglists");
		mailingLists.add(allMailinglist);				
	}
	
	public List<Mailinglist> getMailingLists() {		
		init();		
		return mailingLists;
	}

	public void setMailingLists(List<Mailinglist> emails) {
		this.mailingLists = emails;
	}
	
	public boolean isMailingList(String list) {
		return mailingLists.contains(list);
	}

	public Mailinglist getDefaultMailinglist() {
		return mailingLists.get(0);
	}
	
	public void addMailinglist() {
		newMailinglist = new Mailinglist();		
		newMailinglist.setName(name);
		newMailinglist.setDescription(description);
		if(!mailinglistsCheck(newMailinglist)) {
			client.addMailinglist(newMailinglist);
		}
		newMailinglist = new Mailinglist();
	}
	
	public Mailinglist getNewMailinglist() {
		return newMailinglist;
	}

	public void setNewMailinglist(Mailinglist newMailinglist) {
		this.newMailinglist = newMailinglist;
	}

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
	
	private boolean mailinglistsCheck(Mailinglist mailinglist) {		
		for(Mailinglist ml : mailingLists) {
			if (mailinglist.getName().equals(ml.getName())) {
				return true;
			}
		}
		return false;
	}
}
