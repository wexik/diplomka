package mailinglistonline.server.export.searchisko;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.MiniEmail;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * Parser used to parse the data passed through Searchisko REST API defined in the {@link mailinglistonline.server.export.searchisko.SearchiskoInterface}.
 * 
 * @author Matej Briškár
 * @author Július Staššík
 */
public class SearchiskoResponseParser {
	private static final String SUBJECT_HIGHLIGHT = "subject";
	private static final String MAIN_CONTENT_HIGHLIGHT = "main_content";
	int hits;
	private List<MiniEmail> emails = new ArrayList<MiniEmail>();		
	final static Logger logger = Logger.getLogger(SearchiskoResponseParser.class);
	
	public SearchiskoResponseParser() {

	}

	public void setEmails(List<MiniEmail> emails) {
		this.emails = emails;
	}

	public void addEmail(MiniEmail email) {
		emails.add(email);
	}

	public List<MiniEmail> getEmails() {
		return emails;
	}

	public void parse(Map<String, Object> map) {		
		Map<String, Object> hitsMap = (Map<String, Object>) map.get("hits");
		hits = (Integer) hitsMap.get("total");
		List<Map<Integer, Map<String, Object>>> emails = (List<Map<Integer, Map<String, Object>>>) hitsMap
				.get("hits");
		
		for (Map<Integer, Map<String, Object>> jsonEmailAllInfo : emails) {
			Map<String, Object> jsonEmail = (Map<String, Object>) jsonEmailAllInfo
					.get("_source");			
			ObjectMapper mapper = new ObjectMapper();
			MiniEmail miniEmail = new MiniEmail();
			try {				
				Long date = (Long) jsonEmail.get("date");
				if(date != null) {																			
					miniEmail.setDate(date);
				}
				String subjectt = (String)jsonEmail.get("subject");	
				if(subjectt != null) {
					miniEmail.setSubject(subjectt);
				}
				String from = (String) jsonEmail.get("from");
				if(from != null) {
					miniEmail.setFrom(from);
				}	
				Map<String, Object> id = (Map<String, Object>) jsonEmail.get("id");
				if(id != null) {					
					int process = (int) id.get("processIdentifier");
					miniEmail.setId(new ObjectId((int) id.get("timestamp"),
												 (int) id.get("machineIdentifier"),
												 (short) process,
												 (int) id.get("counter")));
				}
				String mailinglist = (String) jsonEmail.get("mailinglist");
				if(mailinglist != null) {
					miniEmail.setMailinglist(mailinglist);
				}
				List<String> tags = (List<String>) jsonEmail.get("tags");
				if(tags != null) {
					miniEmail.setTags(tags);					
				}
				Map<String, Object> highlightInside = (Map<String, Object>) jsonEmailAllInfo
						.get("highlight");
				List<String> mainContents = null;
				List<String> highLighetSubjects = null;
				if(highlightInside != null) {
				mainContents = (List<String>) highlightInside
						.get("mainContent.text");
				highLighetSubjects = (List<String>) highlightInside
						.get(SUBJECT_HIGHLIGHT);
				}
				if (mainContents != null) {
					for (String highLightMainContent : mainContents) {
						miniEmail.addHighLight(MAIN_CONTENT_HIGHLIGHT,
								highLightMainContent);
					}
				}
				if (highLighetSubjects != null) {
					for (String subject : highLighetSubjects) {
						miniEmail.addHighLight(SUBJECT_HIGHLIGHT,
								subject);
					}
				}
				addEmail(miniEmail);
			} catch (Exception e) {
				logger.error(e);
			}
		}

	}
	
	public Email parseWholeEmail(Map<String, Object> map) {
		ObjectMapper mapper = new ObjectMapper();
		Email email = new Email();
		
		Long date = (Long) map.get("date");
		if(date != null) {																			
			email.setDate(date);
		}
		String subjectt = (String)map.get("subject");	
		if(subjectt != null) {
			email.setSubject(subjectt);
		}
		String from = (String) map.get("from");
		if(from != null) {
			email.setFrom(from);
		}	
		Map<String, Object> id = (Map<String, Object>) map.get("id");
		if(id != null) {					
			int process = (int) id.get("processIdentifier");
			email.setId(new ObjectId((int) id.get("timestamp"),
										 (int) id.get("machineIdentifier"),
										 (short) process,
										 (int) id.get("counter")));
		}
		String mailinglist = (String) map.get("mailinglist");
		if(mailinglist != null) {
			email.setMailinglist(mailinglist);
		}
		List<String> tags = (List<String>) map.get("tags");
		if(tags != null) {
			email.setTags(tags);					
		}
		
		return email;
	}
}
