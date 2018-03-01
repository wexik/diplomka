package com.redhat.mailinglistOnline.jsf;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ViewScoped;
import javax.inject.Named;

/**
 * Instance having the information about the currently selected mailinglist.
 * 
 * @author Matej Briškár
 */
@Named("selectedMailinglist")
@ViewScoped
public class CurrentlySelectedMailinglist implements Serializable{

	private String mailinglist;

	public String getMailinglist() {
		return mailinglist;
	}

	public void setMailinglist(String mailinglist) {
		this.mailinglist = mailinglist;
	}
	
}
