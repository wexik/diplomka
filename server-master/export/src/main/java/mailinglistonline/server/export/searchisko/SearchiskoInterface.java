package mailinglistonline.server.export.searchisko;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientResponse;

import mailinglistonline.server.export.database.entities.Email;

/**
 * Interface to the Searchisko REST API.
 * 
 * @author Matej Briškár
 * @author Július Staššík
 */
@Path("/v2/rest")
public interface SearchiskoInterface {

		@GET
	    @Path("/content/mlonline_email/{sys_content_id}")
	    @Produces("application/json")
	    public Map<String, Object> getEmailById(@PathParam("sys_content_id") String id);
	    
		@DELETE
	    @Path("/content/mlonline_email/{sys_content_id}")
	    @Produces("application/json")
	    public void deleteEmail(@PathParam("sys_content_id") String id, @QueryParam("ignore_missing") boolean ignoreMissing);
		
		@GET
	    @Path("/search")
	    @Produces("application/json")
	    public Map<String, Object> searchEmail(@QueryParam("query") String query, @QueryParam("query_highlight") boolean query_highlight,
	    		@QueryParam("sort_by") String sort_by, @QueryParam("from") String from, @QueryParam("size") String size,
	    		@QueryParam("facet") String facet,@QueryParam("field") String field,@QueryParam("content_provider") String content_provider,
	    		@QueryParam("type") String type, @QueryParam("sys_type") String sysType, @QueryParam("tag") String tag,
	    		@QueryParam("project") String project, @QueryParam("activity_date_interval") String activityDateInterval, @QueryParam("activity_date_from") String activityDateFrom,
	    		@QueryParam("activity_date_to") String activityDateTo, @QueryParam("contributor") String contributor);
		
		@GET
	    @Path("/search")
	    @Produces("application/json")
	    public Map<String, Object> searchEmailByContent(@QueryParam("query") String query, 
	    												@QueryParam("query_highlight") Boolean highlight,
	    												@QueryParam("field") String field);
		
		@GET
	    @Path("/search")
	    @Produces("application/json")
	    public Map<String, Object> searchEmailByContentFilterByTags
	    							(@QueryParam("query") String query, 
	    							 @QueryParam("query_highlight") Boolean highlight,
	    							 @QueryParam("field") String field,
	    							 @QueryParam("tag") List<String> tags);
	    
		@POST
	    @Path("/content/mlonline_email/{sys_content_id}")
		@Consumes("application/json")
	    public Response sendEmail(Email email, @PathParam("sys_content_id") String id);
		
	    
	    
}
