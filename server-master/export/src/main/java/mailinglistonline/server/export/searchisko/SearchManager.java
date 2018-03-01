package mailinglistonline.server.export.searchisko;

import java.util.List;

import javax.ejb.Singleton;
import javax.ws.rs.core.Response;

import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.MiniEmail;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * Manager implementing all the methods in {@link SearchClient} interface to access the Searchisko instance.
 * 
 * @author Matej Briškár
 * @author Július Staššík
 */
@Singleton
public class SearchManager implements SearchClient{

	    private static String SERVER_PROPERTIES_FILE_NAME = "/searchisko.properties";
		private SearchiskoConfiguration configuration;
		SearchiskoInterface emailClient;		
		

		public SearchManager() {
			this(new SearchiskoConfiguration().readFromPropertyFile(
					SearchManager.class.getResourceAsStream((SERVER_PROPERTIES_FILE_NAME))),true);
		}
		
		public SearchManager(SearchiskoConfiguration configuration, boolean authentication) {
			this.configuration=configuration;
			RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
			if(authentication) {
				emailClient =createSearchiskoProxyConnectionWithCredentials();
			} else {
				emailClient =createSearchiskoProxyConnectionWithoutCredentials();
			}
		}
		
		public List<MiniEmail> searchByContentFilteredByTags(String mainContent, List<String> tags){
			SearchiskoResponseParser parser= new SearchiskoResponseParser();
			parser.parse(emailClient.searchEmailByContentFilterByTags
										(mainContent, true, "_source", tags));
			return parser.getEmails();
		}
		
		public List<MiniEmail> searchByContent(String mainContent) {
			SearchiskoResponseParser parser= new SearchiskoResponseParser();
			parser.parse(emailClient.searchEmailByContent(mainContent, true, "_source"));			
			return parser.getEmails();
		}
		
		private SearchiskoInterface createSearchiskoProxyConnectionWithCredentials() {
			DefaultHttpClient httpClient = new DefaultHttpClient();
	        Credentials credentials = new UsernamePasswordCredentials(configuration.getUsername(),configuration.getPassword());
	        httpClient.getCredentialsProvider().setCredentials(
	                org.apache.http.auth.AuthScope.ANY, credentials);
	        ClientExecutor clientExecutor = new ApacheHttpClient4Executor(httpClient);
	        return ProxyFactory.create(SearchiskoInterface.class, configuration.getSearchiskoUrl(), clientExecutor);
		}
		
		private SearchiskoInterface createSearchiskoProxyConnectionWithoutCredentials() {
			return  ProxyFactory.create(SearchiskoInterface.class, configuration.getSearchiskoUrl());
		}
		
		
		public boolean addEmail(Email email) {
			ObjectId id = email.getId();			
			Response response = emailClient.sendEmail(email, "mlonline_email-" + id.toString());
			if( response.getStatus() == 200) {
				response.close();
				return true;
			} else {
				response.close();
				return false;
			}
		}
		
		public boolean removeEmail(String id) {
			emailClient.deleteEmail(id, true);
			return true;
		}

		@Override
		public boolean updateEmail(Email email) {
			return addEmail(email);
		}
		
		public Email getEmailFromSearchisko(String id) {
			Email email;
			SearchiskoResponseParser parser= new SearchiskoResponseParser();
			email = parser.parseWholeEmail(emailClient.getEmailById(id));
			return email;
		}
	
}
