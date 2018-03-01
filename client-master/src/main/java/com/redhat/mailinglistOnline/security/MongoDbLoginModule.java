package com.redhat.mailinglistOnline.security;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.acl.Group;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.server.embedded.SimplePrincipal;
import org.jboss.security.SimpleGroup;
import org.jboss.security.auth.spi.UsernamePasswordLoginModule;
import org.jboss.security.auth.spi.Util;

import com.redhat.mailinglistOnline.client.DbClient;
import com.redhat.mailinglistOnline.client.DbClientProxy;
import com.redhat.mailinglistOnline.client.HashString;
//https://access.redhat.com/knowledge/docs/en-US/JBoss_Enterprise_Application_Platform/5/html/Security_Guide/sect-Custom_LoginModule_Example.html
//http://java.dzone.com/articles/creating-custom-login-modules
import com.redhat.mailinglistOnline.client.entities.User;


/**
 * The class implementing the login module for the container-based security for MongoDB.
 * 
 * @author Matej Briškár
 */
public class MongoDbLoginModule extends UsernamePasswordLoginModule{

	final static Logger logger = Logger.getLogger(MongoDbLoginModule.class);	
	DbClient dbClient;	
	
	@Override
	 public void initialize(Subject subject, CallbackHandler callbackHandler,
             Map sharedState, Map options)
	{
		super.initialize(subject, callbackHandler, sharedState, options);
		try {
			 DbClientProxy proxy = (DbClientProxy) new InitialContext()
					.lookup("java:module/dbClientProxy");
			 dbClient = proxy.getDbClient();
		} catch (NamingException e) {
			logger.error(e);
		}
		
	}

	@Override
	protected String getUsersPassword() throws LoginException {		
		User user = dbClient.getUserByName(getUsername());
		if(user != null) {			
			return user.getPassword();
		} else {				
			return null;
		}
	}

	@Override
	protected Group[] getRoleSets() throws LoginException {
		SimpleGroup group = new SimpleGroup("Roles");		
        try {
        	User user=dbClient.getUserByName(getUsername());
        	for (String role:user.getRoles()) {        		
        		group.addMember(new SimplePrincipal(role));
        	}
        } catch (Exception e) {     
        	logger.error("Failed to create group member for " + group + " " + e);
            throw new LoginException("Failed to create group member for " + group);
        }         
        return new Group[] { group };
	}
	
	@Override //was protected
	public String createPasswordHash(String username, String password, String digestOption) throws LoginException {		
		String pwd = getUsersPassword();
		String[] parts = pwd.split(":");			
		return HashString.hash(password+username, parts[1]);		
	}
	
	@Override
	protected boolean validatePassword(String inputPassword, String expectedPassword) {			
		boolean result= super.validatePassword(inputPassword, expectedPassword);		
		return result;
	}	
}
