package mailinglistonline.server.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;

import java.util.Date;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;

import com.mongodb.MongoClient;

import mailinglistonline.server.export.database.DbClient;
import mailinglistonline.server.export.importing.MboxImporter;
import mailinglistonline.server.export.searchisko.SearchClient;
/**
 * REST interface handling importing messages in mbox
 * @author Július Staššík
 *
 */
@ApplicationScoped
@Path("/importing")
public class ImportResource {
	
	private static final String IMPORT_FILE_PATH = System.getProperty("user.dir");
	final static Logger logger = Logger.getLogger(ImportResource.class);
	@Inject
	DbClient dbClient;
	@Inject
	SearchClient searchClient;	

	public ImportResource() throws UnknownHostException, IOException {

	}
	
	@RolesAllowed("admin")
	@POST
	@Path("/import")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response importing(InputStream input) {
		Date date = new Date();
		Long milis = date.getTime();
		OutputStream out = null;
		int read = 0;
		byte[] bytes = new byte[1024];
		File file = new File (IMPORT_FILE_PATH + "/mbox");
		if(!file.isDirectory()) {
			file.mkdir();
		}
		try {
						
			out = new FileOutputStream(new File(IMPORT_FILE_PATH 
							+ "/mbox/import" + milis.toString() + ".txt"));
			while ((read = input.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {			
			logger.error(e);
		}		
		try {
			MboxImporter importer = new MboxImporter(dbClient, true, searchClient);			
			importer.importMbox(IMPORT_FILE_PATH
					+ "/mbox/import" + milis.toString() + ".txt");
		} catch (UnknownHostException e) {
			logger.error(e);
		} catch (NoSuchProviderException e) {			
			logger.error(e);
		} catch (MessagingException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		
		new File(IMPORT_FILE_PATH + "/mbox/import" + milis.toString() + ".txt").delete();
		ResponseBuilder rb = Response.ok();
		Response response = rb.build();
		return response;
	}		
}
