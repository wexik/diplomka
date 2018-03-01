package mailinglistonline.server.export;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.util.Base64;

import mailinglistonline.server.export.database.entities.Client;
import mailinglistonline.server.export.util.PropertiesParser;

/**
 * This filter verify users permissions 
 * based on username and passowrd provided in header
 * @author Július Staššík
 * */
@Provider
public class SecurityFilter implements ContainerRequestFilter{
	private static final String AUTHORIZATION_PROPERTY = "Authorization";
	private static final String AUTHENTICATION_SCHEME = "Basic";
	private static final ServerResponse ACCESS_DENIED = new ServerResponse("Access denied for this resource", 401, new Headers<Object>());
	private static final ServerResponse ACCESS_FORBIDDEN = new ServerResponse("Nobody can access this resource", 403, new Headers<Object>());
	private static final ServerResponse SERVER_ERROR = new ServerResponse("INTERNAL SERVER ERROR", 500, new Headers<Object>());
	private static final String CLIENTS_FILE_NAME = "/clients.properties";	
	@Context
	private ResourceInfo resourceInfo;
	
	private List<Client> clients;	
		
	public SecurityFilter() {
		clients = PropertiesParser.parseClientsProperties(SecurityFilter.class
				  .getResourceAsStream(CLIENTS_FILE_NAME));
	}
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {							
		
		Method method = resourceInfo.getResourceMethod();
		
		if( ! method.isAnnotationPresent(PermitAll.class)){      
            if(method.isAnnotationPresent(DenyAll.class)){
                requestContext.abortWith(ACCESS_FORBIDDEN);
                return;
            }
                         
            final MultivaluedMap<String, String> headers = requestContext.getHeaders();                        
            final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);
                         
            if(authorization == null || authorization.isEmpty()){
                requestContext.abortWith(ACCESS_DENIED);
                return;
            }
                         
            final String encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");                        
            String usernameAndPassword = null;
            try {
                usernameAndPassword = new String(Base64.decode(encodedUserPassword));
            } catch (IOException e) {
                requestContext.abortWith(SERVER_ERROR);
                return;
            }
             
            final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
            final String username = tokenizer.nextToken();
            final String password = tokenizer.nextToken();                        
                         
            if(method.isAnnotationPresent(RolesAllowed.class)){
                RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));
                                 
                if( ! isUserAllowed(username, password, rolesSet)){
                    requestContext.abortWith(ACCESS_DENIED);
                    return;
                }
            }
        }
	}
		

	private boolean isUserAllowed(final String username, final String password,	final Set<String> rolesSet) 
	{
		boolean isAllowed = false;
		boolean foundName = false;
		Client foundClient = new Client();
		
		for(Client client : clients) {
			if(client.getName().equals(username)) {
				foundName = true;
				foundClient = client;
				break;
			}
		}
		if(!foundName) return false;
		
		if(!password.equals(foundClient.getPwd())) {
			return false;					
		}
				
		if(rolesSet.contains(foundClient.getRole())){
			isAllowed = true;
		}
		return isAllowed;
	}
	
}