package com.redhat.mailinglistOnline.jsf;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.auth.login.LoginException;
import javax.validation.constraints.Size;

import org.apache.log4j.Logger;
import org.jboss.security.auth.spi.Util;

import com.redhat.mailinglistOnline.client.DbClient;
import com.redhat.mailinglistOnline.client.HashString;
import com.redhat.mailinglistOnline.client.entities.User;
import com.redhat.mailinglistOnline.security.MongoDbLoginModule;

/**
 * The bean handling the registration and storing the registration data of the user.
 * 
 * @author Matej Briškár
 */
@Named("reg")
@ViewScoped
public class RegisterUser implements Serializable{

	@Size(min=3, message="Username must have at least 3 characters")
	private String username;
	@Size(min=5, message="Password must have at least 5 characters")
	private String password;
	@Size(min=5, message="Password must have at least 5 characters")
	private String passwordAgain;
	
	private String name;
	
	@Inject
	DbClient dbClient;
	
	//@Inject
	HashString hashFactory;
	
	MongoDbLoginModule module;
	
	public RegisterUser() {
		module = new MongoDbLoginModule();
	}

	public String register() throws LoginException {
		FacesMessage facesMessage;
		if (password.equals(passwordAgain)) {
			if (dbClient.getUserByName(username) == null) {
				User user = new User();
				user.setUsername(username);				
				String hashedPassword = HashString.hash(password+username, null);				
				user.setPassword(hashedPassword);
				List<String> roles = new ArrayList<String>();
				roles.add("user");
				user.setRoles(roles);
				dbClient.saveUser(user);
				facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Successfully registered", null);
			} else {
				facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "User already exists", null);
			}
		} else {
			facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Passwords are not equal", null);
		}
		FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage("topMessages", facesMessage);
		return "home";
	}
	
	public void grantPermission() {
		FacesMessage facesMessage;
		User user = dbClient.getUserByName(name);
		if(user == null) {
			facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "User doesn't exist", null);
			FacesContext context = FacesContext.getCurrentInstance();
	        context.addMessage("myForm:message", facesMessage);
			return;
		}
		List<String> roles = user.getRoles();
		if(roles.contains("admin")) return;
		roles.add("admin");
		user.setRoles(roles);		
		dbClient.updateUser(user);	
		facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Permission has been granted", null);
		FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage("myForm:message", facesMessage);
	}		

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

	public String getPasswordAgain() {
		return passwordAgain;
	}

	public void setPasswordAgain(String passwordAgain) {
		this.passwordAgain = passwordAgain;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}