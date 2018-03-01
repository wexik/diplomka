package com.redhat.mailinglistOnline.client.responses;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.redhat.mailinglistOnline.client.entities.Email;
import com.redhat.mailinglistOnline.client.entities.MiniEmail;
import com.redhat.mailinglistOnline.client.rest.RestClient;


/**
 * Object responsible to load the response from the server. The responses are {@link EmailsResponse}, {@link DetailedEmailResponse}, {@link SearchiskoResponse}.
 * Class is needed as there is no other way to load the response (other options are passing the object directly from restClient or having a logic in entities..).
 * 
 * @author Matej Briškár
 * @author Július Staššík
 */
@RequestScoped
@Named("loader")
public class ResponseLoader {

	@Inject
	EmailsResponse response;
	
	@Inject
	DetailedEmailResponse detailedResponse;
	
	@Inject
	SearchiskoResponse searchResponse;
	
	@Inject 
	RestClient client;
	private static final Logger log = Logger.getLogger(ResponseLoader.class);
	
	public void searchEmailsByContent(String searchString,String selectedMailinglist,
			String fromString, List<String> tags) {
		if(searchString != null && !searchString.equals("")) {
			List<MiniEmail> miniEmails=client.searchEmailsByContent(searchString);			
			searchResponse.addEmails(miniEmails);			
			searchResponse.filter(selectedMailinglist, fromString, null);			
		} else {
			return;
		}
		
	}
	
	public void searchEmailsAndFilterByTags(String searchString,String selectedMailinglist,
											List<String> tags ) {
		if(searchString != null && !searchString.equals("")) {
			List<MiniEmail> miniEmails=client.searchEmailsByContentAndFilterByTags(searchString, tags);			
			searchResponse.addEmails(miniEmails);			
			searchResponse.filter(selectedMailinglist, null, null);			
		} else {
			return;
		}
	}

	public void getMailinglistLatest(String selectedMailinglist,
			int number) {
		if((selectedMailinglist == null) || (selectedMailinglist.equals(""))) {
			return;
		}		
		List<Email> emails=client.getMailinglistLatest(selectedMailinglist, number);
		response.addEmails(emails);
	}

	public void getMailingListRoot(String selectedMailinglist) {
		List<Email> emails=client.getMailingListRoot(selectedMailinglist);
		response.addEmails(emails);
	}
	
	public void getMailingListRoot(String selectedMailinglist, int from, int to) {
		List<Email> emails=client.getMailingListRoot(selectedMailinglist, from,  to);
		response.addEmails(emails);
	}
	
	public void getDetailedMessage(String id){
		Email email =client.getEmailById(id);
		detailedResponse.setEmail(email);
	}
	
	public void downloadAttachment(String id) {		
		Response response = client.downloadAttachment(id);
		String fileName = response.getHeaderString("content-disposition").substring(21);				
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		ec.responseReset();
		ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");		
		try {
			OutputStream output = ec.getResponseOutputStream();
			InputStream input = response.readEntity(InputStream.class);						
			int length;
			byte[] buffer = new byte[10240];
			while((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}			
		} catch (IOException e) {
			log.error(e);
		} 		
		fc.responseComplete();		
	}
	
	public String getFileName(String id) {
		if(id == null || id.equals("")) return null;		
		String name = client.getAttachmentName(id);		
		return name;
	}
	
}
