package mailinglistonline.server.export;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import mailinglistonline.server.export.database.DbClient;
import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.Mailinglist;
import mailinglistonline.server.export.database.entities.MiniEmail;
import mailinglistonline.server.export.searchisko.SearchClient;

import org.apache.log4j.Logger;
import com.mongodb.client.gridfs.model.GridFSFile;

/**
 * The REST interface to provide the emails saved in the database according to the specified information in the URL.
 * 
 * @author Matej Briškár
 * @author Július Staššík
 */
@ApplicationScoped
@Path("/emails")
public class EmailResource {

	private static final String ALL_REGEX = ".*";
	private static final String MAILINGLIST_REPRESENTING_ALL = "all";	
	final static Logger logger = Logger.getLogger(EmailResource.class);
	@Inject
	DbClient dbClient;
	@Inject
	SearchClient searchClient;	

	public EmailResource() throws UnknownHostException, IOException {

	}		

	@RolesAllowed("admin")
	@GET
	@Path("/all")
	@Produces("application/json")
	public List<Email> getAllEmails() {
		return dbClient.getAllEmails();
	}

	@RolesAllowed("admin")
	@GET
	@Path("/email/{id}")
	@Produces("application/json")
	public Email getEmailById(@PathParam("id") String id) {
		Email email = dbClient.getEmailWithId(id);
		if (email == null) {
			return null;
		}
		return email;

	}

	@RolesAllowed("admin")
	@GET
	@Path("")
	@Produces("application/json")
	public List<Email> getEmails(@QueryParam("from") String author,
			@QueryParam("mailinglist") String mailinglist,
			@QueryParam("tags") List<String> tags,
			@QueryParam("count") int count,
			@QueryParam("descending") List<String> descending,
			@QueryParam("ascending") List<String> ascending) {

		Object mailinglistObject;
		if (MAILINGLIST_REPRESENTING_ALL.equals(mailinglist)) {
			mailinglistObject = Pattern.compile(ALL_REGEX);
		} else {
			mailinglistObject = mailinglist;
		}
		return dbClient.getEmails(author, mailinglistObject, tags,
				count, ascending, descending);
	}

	@RolesAllowed("admin")
	@GET
	@Path("/{mailinglist}/roots/all")
	@Produces("application/json")
	public List<Email> getMailingListRoots(
			@PathParam("mailinglist") String mailinglist) {
		if (MAILINGLIST_REPRESENTING_ALL.equals(mailinglist)) {
			return dbClient.getMailinglistRoot(Pattern
					.compile(ALL_REGEX));
		} else {
			return dbClient.getMailinglistRoot(mailinglist);
		}

	}

	@RolesAllowed("admin")
	@GET
	@Path("/{mailinglist}/roots/")
	@Produces("application/json")
	public List<Email> getMailingListRoots(@QueryParam("from") int fromNumber,
			@QueryParam("to") int toNumber,
			@PathParam("mailinglist") String mailinglist) {
		if (toNumber == 0) {
			return getMailingListRoots(mailinglist);
		}
		if (fromNumber > toNumber || fromNumber < 0 || toNumber<0) {
			return new ArrayList<Email>();
		}
		List<Email> list;
		if (MAILINGLIST_REPRESENTING_ALL.equals(mailinglist)) {
			list = dbClient
					.getMailinglistRoot(Pattern
							.compile(ALL_REGEX),fromNumber,toNumber);
		} else {
				list = dbClient.getMailinglistRoot(mailinglist,fromNumber,toNumber);
		}
		return list;
	}
	
	@RolesAllowed("admin")
	@GET
	@Path("/{mailinglist}/roots/count")
	@Produces("application/json")
	public Integer getMailingListRootCount(@PathParam("mailinglist") String mailinglist) {
		
		if (MAILINGLIST_REPRESENTING_ALL.equals(mailinglist)) {
			int i = 0;
			List<Mailinglist> mls = dbClient.getMailingLists();			
			for(Mailinglist ml : mls) {
				i+= dbClient.getMailinglistRootCount(ml.getName());				
			}
			return i;
			
		}else {
			return dbClient.getMailinglistRootCount(mailinglist);
		}
		
	}

	@RolesAllowed("admin")
	@GET
	@Path("/thread/")
	@Produces("application/json")
	public List<Email> getEmailPath(@QueryParam("id") String id) {
		List<Email> list = dbClient.getWholeThreadWithMessage(id);
		return list;

	}
	
	@RolesAllowed("admin")
	@GET
	@Path("/email/file/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getEmailBinaryAttachment(@PathParam("id") String id) {
		if(id == null || id.equals("")) {
			ResponseBuilder rb = Response.status(404);
			Response response = rb.build();
			return response;
		}
		GridFSFile file = dbClient.findFileById(id);
		if (file != null) {			
		ResponseBuilder rb = Response.ok(dbClient.getBinaryFileById(id));
		rb.header("Content-Disposition", "attachment; filename=" + file.getFilename());
		Response response = rb.build();
		return response;
		} else {
			ResponseBuilder rb = Response.status(404);
			Response response = rb.build();
			return response;
		}
	}

	@RolesAllowed("admin")
	@GET
	@Path("/email/file_name/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getEmailAttachmentName(@PathParam("id") String id) {
		if(id == null || id.equals("")) return "";
		GridFSFile file = dbClient.findFileById(id);
		if(file != null) {				
			return file.getFilename();
		} else {			
			return "";
		}
	}	

	@RolesAllowed("admin")
	@POST
	@Path("/email/tag/")
	public void addTag(@QueryParam("id") String id,
			@QueryParam("tag") String tag) {
		dbClient.addTagToEmail(id, tag);
		searchClient.updateEmail(dbClient.getEmailWithId(id));
	}

	@RolesAllowed("admin")
	@DELETE
	@Path("/email/tag/")
	public void removeTag(@QueryParam("id") String id,
			@QueryParam("tag") String tag) {		
		if(dbClient.removeTagFromEmail(id, tag)) {			
			searchClient.updateEmail(dbClient.getEmailWithId(id));			
		}
	}

	// NOW COMES THE SEARCH METHODS:
	@RolesAllowed("admin")
	@GET
	@Path("/search/content")
	@Produces("application/json")
	public List<MiniEmail> searchEmailByContent(
			@QueryParam("content") String content) {		
		List<MiniEmail> emails = searchClient.searchByContent(content);
		return emails;
	}
	
	@RolesAllowed("admin")
	@GET
	@Path("/search/filterByTags")
	@Produces("application/json")
	public List<MiniEmail> searchEmailByContentAndFilterByTags(
			@QueryParam("content") String content, @QueryParam("tag") List<String> tags) {		
		List<MiniEmail> emails = searchClient.searchByContentFilteredByTags(content, tags);
		return emails;
	}	
}
