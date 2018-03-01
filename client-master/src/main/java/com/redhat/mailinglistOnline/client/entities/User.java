package com.redhat.mailinglistOnline.client.entities;


import java.util.List;

import javax.faces.bean.SessionScoped;
import javax.inject.Named;

import com.mongodb.BasicDBObject;

/**
 * Entity used to handle information about the logged user.
 * 
 * @author Matej Briškár
 */
@Named("user")
@SessionScoped
public class User {

	private static final long serialVersionUID = -6858919384286693764L;
	public static final String ROOT_NAME_TAG = "username";
	public static final String ROOT_PASSWORD_TAG = "password";
	public static final String ROOT_ROLES_TAG = "roles";
	
	private String username;
	private String password;
	private List<String> roles;

	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public boolean hasRole(String role) {
		List<String> roles = getRoles();
		if(roles != null) {
			return roles.contains(role);
		} else {
			return false;
		}
	}
}
