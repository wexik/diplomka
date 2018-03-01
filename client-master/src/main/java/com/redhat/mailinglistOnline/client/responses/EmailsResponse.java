package com.redhat.mailinglistOnline.client.responses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import com.google.common.collect.Lists;
import com.redhat.mailinglistOnline.client.entities.Email;
import com.redhat.mailinglistOnline.client.entities.MiniEmail;


/**
 * A response from the server containing the list of emails as the result for the given query (latest, search etc).
 * 
 * @author Matej Briškár
 * @author Július Staššík
 */
@Named("contentResponse")
@RequestScoped
public class EmailsResponse implements Serializable {

	private List<Email> emails;

	
	public void addEmails(List<Email> emails) {
		this.emails = emails;
	}

	public List<? extends MiniEmail> getEmails() {
		return emails;
	}

	public void filter(String selectedMailinglist, String fromString,
			List<String> tags) {
		if ((tags != null && tags.size() == 1 && tags.get(0).equals(""))) {
			tags = null;
		}
		if (emails == null || emails.size() == 0) {
			return;
		}
		List<Email> filtered = Lists.newArrayList();
		for (Email e : emails) {
			if (selectedMailinglist == null
					|| selectedMailinglist
							.equals(MailingListsResponse.ALL_MAILINGLISTS)
					|| selectedMailinglist.equals(e.getMailinglist())) {
				if (fromString == null || fromString.equals("")
						|| fromString.equals(e.getFrom())) {
					if (tags == null || tags.size() == 0) {
						filtered.add(e);
					} else if (e.getTags() != null) {
						List<String> tagsMatch = new ArrayList<String>(tags);
						tagsMatch.retainAll(e.getTags());
						if (tagsMatch.size() > 0) {
							filtered.add(e);
						}
					}
				}
			}
		}
		emails = filtered;
	}

}
