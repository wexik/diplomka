package test.aqruillian;

import java.util.Collections;
import java.util.List;

import javax.ejb.Singleton;
import javax.enterprise.inject.Alternative;

import mailinglistonline.server.export.database.entities.Email;
import mailinglistonline.server.export.database.entities.MiniEmail;
import mailinglistonline.server.export.searchisko.SearchClient;
import mailinglistonline.server.export.searchisko.SearchiskoInterface;


@Alternative
@Singleton
public class MockSearchClient implements SearchClient{

		SearchiskoInterface emailClient;
		private Email email;

		public MockSearchClient() {
			email=EmailCreator.createSimpleEmail();
		}
		
		public List<MiniEmail> searchByContent(String mainContent) {
			MiniEmail mini = email;
			return Collections.singletonList(mini);
		}
		
		public boolean addEmail(Email email) {
			this.email=email;
			return true;
		}
		
		public boolean removeEmail(String id) {
			return true;
		}

		@Override
		public boolean updateEmail(Email email) {
			return addEmail(email);
		}

		@Override
		public List<MiniEmail> searchByContentFilteredByTags(String content, List<String> tags) {
			return null;
		}
	
}
