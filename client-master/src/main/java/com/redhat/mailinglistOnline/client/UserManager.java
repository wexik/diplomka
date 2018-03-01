package com.redhat.mailinglistOnline.client;



import javax.ejb.Stateful;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import com.redhat.mailinglistOnline.client.entities.User;

/**
 * The object saved in the session handling the user creation and logging out..
 * 
 * @author Matej Briškár
 */
@Named("userSession")
@Stateful()
public class UserManager {
	
	@Inject
	private DbClient dbClient;
	
	
	public UserManager() {
	}
		
	public boolean createUser(User user) {
		if(dbClient.getUserByName(user.getUsername()) != null) {
			dbClient.saveUser(user);
			return true;
		} else {
			return false;
		}
	}
	
	public String logout() {
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		return "/index.xhtml?faces-redirect=true";
	}
	
	public boolean hasRole(String role) {
		if(role == null) return false;
		String user = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		if(user != null) {
			User u = dbClient.getUserByName(user);
			return u.hasRole(role);
		} else {
			return false;
		}
	}
	
}
	


