package com.redhat.mailinglistOnline.jsf;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AbortProcessingException;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.richfaces.event.ItemChangeEvent;
import org.richfaces.event.ItemChangeListener; 

import com.redhat.mailinglistOnline.client.entities.Email;
import com.redhat.mailinglistOnline.client.entities.MiniEmail;
import com.redhat.mailinglistOnline.client.responses.EmailsResponse;
import com.redhat.mailinglistOnline.client.responses.ResponseLoader;
import com.redhat.mailinglistOnline.client.rest.RestClient;


/**
 * The bean handling the selection on the top (latest, search, roots).
 * 
 * @author Matej Briškár
 */
@Named("panelChangeListener")
@ViewScoped
public class PanelChangeListener implements  Serializable { //ItemChangeListener,

	private static int LATEST_EMAIL_COUNT = 10;
	private static final String ALL_MAILINGLIST = "all";

	@ManagedProperty(value = "#{searchString}")
	private String searchString;
	@ManagedProperty(value = "#{fromString}")
	private String fromString;
	@ManagedProperty(value = "#{tagString}")
	private String tagString;

	@Inject
	RestClient client;

	@Inject
	ResponseLoader loader;

	@Inject
	private EmailsResponse contentEmails;
	
	@Inject 
	CurrentlySelectedMailinglist selectedMailinglist;
	
	private String selectedTab;	
	private static final Logger log = Logger.getLogger(PanelChangeListener.class);
	private int currentRootsStartNumber = 0;
	private int allRootsInMailinglist =0;

	public void processItemChange() { 
		if(selectedMailinglist.getMailinglist() != null) {
			if(allRootsInMailinglist == 0) {
				allRootsInMailinglist = client.getMailingListRootNumber(selectedMailinglist.getMailinglist());
			}	
			loader.getMailingListRoot(selectedMailinglist.getMailinglist(),currentRootsStartNumber, 
									  currentRootsStartNumber+10);				
		}
	}

	public void search() {
		if(tagString == null) {
		loader.searchEmailsByContent(searchString, selectedMailinglist.getMailinglist(),
				fromString, null);
		} else {
			loader.searchEmailsAndFilterByTags(searchString, selectedMailinglist.getMailinglist(), 
													Arrays.asList(tagString.split("\\s*,\\s*")));					
		}

	}
	
	public void nextRootsList() {
		if(currentRootsStartNumber +10 < allRootsInMailinglist) {
			currentRootsStartNumber = currentRootsStartNumber+10;
		}
		loader.getMailingListRoot(selectedMailinglist.getMailinglist(),currentRootsStartNumber, currentRootsStartNumber+10);
	}
	
	public void previousRootsList() {
		if(currentRootsStartNumber > 0) {
			currentRootsStartNumber = currentRootsStartNumber-10;
		}
		loader.getMailingListRoot(selectedMailinglist.getMailinglist(),currentRootsStartNumber, currentRootsStartNumber + 10);
	}
	

	
	public EmailsResponse getContentEmails() {
		return contentEmails;
	}

	public void setContentEmails(EmailsResponse contentEmails) {
		this.contentEmails = contentEmails;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String value) {
		searchString = value;
	}

	public String getFromString() {
		return fromString;
	}

	public void setFromString(String fromString) {
		this.fromString = fromString;
	}

	public String getTagString() {
		return tagString;
	}

	public void setTagString(String tagString) {
		this.tagString = tagString;
	}
	
	public int getCurrentRootsStartNumber() {
		return currentRootsStartNumber;
	}

	public void setCurrentRootsStartNumber(int currentRootsStartNumber) {
		this.currentRootsStartNumber = currentRootsStartNumber;
	}

	public int getAllRootsInMailinglist() {
		return allRootsInMailinglist;
	}

	public void setAllRootsInMailinglist(int allRootsInMailinglist) {
		this.allRootsInMailinglist = allRootsInMailinglist;
	}
	
	public String getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(String selectedTab) {
		this.selectedTab = selectedTab;
	}

}
