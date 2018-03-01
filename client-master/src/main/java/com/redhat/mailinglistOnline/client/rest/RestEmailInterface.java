package com.redhat.mailinglistOnline.client.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
//import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redhat.mailinglistOnline.client.entities.Email;
import com.redhat.mailinglistOnline.client.entities.MiniEmail;

/**
 * A rest interface to communicate with the server component about the emails.
 * 
 * @author Matej Briškár
 */
@Path("/emails")
public interface RestEmailInterface {

	@GET
	@Path("/all")
	@Produces("application/json")
	public List<Email> getAllEmails();

	@GET
	@Path("/email/{id}")
	@Produces("application/json")
	public Email getEmailById(@PathParam("id") String id);

	@GET
	@Path("")
	@Produces("application/json")
	public List<Email> getEmails(
			@QueryParam("from") String author,
			@QueryParam("mailinglist") String mailinglist,
			@QueryParam("tags") List<String> tags,
			@QueryParam("count") int count,
			@QueryParam("descending") List<String> descending,
			@QueryParam("ascending") List<String> ascending);

	@GET
	@Path("")
	@Produces("application/json")
	public List<Email> getEmailByAuthor(@QueryParam("from") String author);

	@GET
	@Path("/{mailinglist}/roots/all")
	@Produces("application/json")
	// @Wrapped(element="emails")
	public List<Email> getMailingListRoots(
			@PathParam("mailinglist") String mailinglist);

	@GET
	@Path("/{mailinglist}/roots/")
	@Produces("application/json")
	public List<Email> getMailingListRoots(@QueryParam("from") int fromNumber,
			@QueryParam("to") int toNumber,
			@PathParam("mailinglist") String mailinglist);
	
	@GET
	@Path("/{mailinglist}/roots/count")
	@Produces("application/json")
	public Integer getMailingListRootCount(@PathParam("mailinglist") String mailinglist);

	@GET
	@Path("/thread/")
	@Produces("application/json")
	public List<Email> getEmailPath(@QueryParam("id") String id);
	
	@GET
	@Path("/email/file/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getEmailBinaryAttachment(@PathParam("id") String id);
		
	@GET
	@Path("/email/file_name/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getEmailAttachmentName(@PathParam("id") String id);	

	/* SEARCH */
	@GET
	@Path("/search/content")
	@Produces("application/json")
	// @Wrapped(element="emails")
	public List<MiniEmail> searchEmailByContent(
			@QueryParam("content") String content);
	
	@GET
	@Path("/search/filterByTags")
	@Produces("application/json")
	public List<MiniEmail> searchEmailByContentAndFilterByTags(
			@QueryParam("content") String content, @QueryParam("tag") List<String> tags);

	/* DATA MANIPULATION */
	@POST
	@Path("/email/tag/")
	public void addTag(@QueryParam("id") String id,
			@QueryParam("tag") String tag);

	@DELETE
	@Path("/email/tag/")
	public void removeTag(@QueryParam("id") String id,
			@QueryParam("tag") String tag);

	
}
