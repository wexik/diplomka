package com.redhat.mailinglistOnline.client.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

//import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import com.redhat.mailinglistOnline.client.entities.Mailinglist;

/**
 * A rest interface to communicate with the server component about the processed mailinglists.
 * 
 * @author Matej Briškár
 * @author Július Staššík
 */
@Path("/mailinglists")
public interface RestMailingListInterface {
		@GET
	    @Path("/all")
	    @Produces("application/json")
	    public List<Mailinglist> getAllMailingLists();
		
		@POST
	    @Path("/mailinglist")
	    @Consumes("application/json")
	    public void addMailinglist(Mailinglist mailinglist);
}

 
