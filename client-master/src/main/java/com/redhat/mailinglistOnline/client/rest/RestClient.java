package com.redhat.mailinglistOnline.client.rest;


import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
//import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;


import com.redhat.mailinglistOnline.client.DbClient;
import com.redhat.mailinglistOnline.client.entities.Email;
import com.redhat.mailinglistOnline.client.entities.Mailinglist;
import com.redhat.mailinglistOnline.client.entities.MiniEmail;
import com.redhat.mailinglistOnline.client.responses.EmailsResponse;
import com.redhat.mailinglistOnline.client.responses.ResponseLoader;

/**
 * A rest client handling the communication with the server.
 * 
 * @author Matej Briškár
 * @author Július Staššík
 */
@ApplicationScoped
@Named("client")
public class RestClient implements Serializable{
	
	private static String SERVER_PROPERTIES_FILE_NAME = "server.properties";
	private static String AUTH_FILE_NAME = "auth.properties";
	private String serverUrl;
	private String name;
	private String pwd;
	RestEmailInterface emailClient;
	RestMailingListInterface mailingListsClient;	
	
	@Inject 
	EmailsResponse response;
		

	public RestClient() throws IOException {
		Properties prop = new Properties();
		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
        prop.load(DbClient.class.getClassLoader().getResourceAsStream((SERVER_PROPERTIES_FILE_NAME)));
        serverUrl = prop.getProperty("serverUrl");        
        prop.load(DbClient.class.getClassLoader().getResourceAsStream((AUTH_FILE_NAME)));
        name = prop.getProperty("name");
        pwd = prop.getProperty("password");
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(serverUrl);
        target.register(new BasicAuthentication(name, pwd));
        emailClient = target.proxy(RestEmailInterface.class);
        ResteasyClient client2 = new ResteasyClientBuilder().build();
        ResteasyWebTarget target2 = client2.target(serverUrl);
        target2.register((new BasicAuthentication(name, pwd)));
        mailingListsClient = target2.proxy(RestMailingListInterface.class);
	}
	
	public EmailsResponse getAllEmails() {
		response.addEmails(emailClient.getAllEmails());		
		return response;
	}
	
	public Email getEmailById(String id) {
		return emailClient.getEmailById(id); 
	}
	
	public List<Mailinglist> getAllMailingLists() {					
			return mailingListsClient.getAllMailingLists();
	}
	
	public void addMailinglist(Mailinglist mailinglist) {
		mailingListsClient.addMailinglist(mailinglist);		
	}

	public List<Email> getMailingListRoot(String mailingList) {
		return emailClient.getMailingListRoots(mailingList);
	}
	
	public List<Email> getMailingListRoot(String mailingList, int from, int to) {
		return emailClient.getMailingListRoots(from, to, mailingList);
	}
	
	public int getMailingListRootNumber(String mailingList) {
		return emailClient.getMailingListRootCount(mailingList);
	}
	
	public void addTagToEmail(String id, String tag) {
		emailClient.addTag(id, tag);
	}
	
	public void removeTagFromEmail(String id, String tag) {
		emailClient.removeTag(id, tag);
	}
	
	public List<Email> getMailinglistLatest(String mailinglist, int number) {
		return emailClient.getEmails(null, mailinglist, null, number, Collections.singletonList("date"), null);
	}
	
	public List<MiniEmail> searchEmailsByContent(String content) {
		List<MiniEmail> emails = emailClient.searchEmailByContent(content);		
		return emails;

	}
	
	public List<MiniEmail> searchEmailsByContentAndFilterByTags(String content, List<String> tags){
		List<MiniEmail> emails = emailClient.searchEmailByContentAndFilterByTags(content, tags);
		return emails;
	}
	
	public Response downloadAttachment(String id) {
		Response response = emailClient.getEmailBinaryAttachment(id);
		return response;
	}
	
	public String getAttachmentName(String id) {
		String str = emailClient.getEmailAttachmentName(id);
		return str;
	}

}

