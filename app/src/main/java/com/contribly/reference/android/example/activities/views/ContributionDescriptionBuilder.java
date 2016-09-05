package com.contribly.reference.android.example.activities.views;

import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.contribly.reference.android.example.utils.DateTimeHelper;

import io.swagger.client.model.Contribution;

public class ContributionDescriptionBuilder {

	public StringBuilder renderDescription(Contribution contribution) {
		final StringBuilder description = new StringBuilder();

		description.append(DateTimeHelper.calculateTimeTaken(contribution.getCreated().toDate(), DateTime.now().toDate()) + " ago");

		if(contribution.getVia().getAuthority().getUser() != null) {
			description.append(" by " + contribution.getVia().getAuthority().getUser() .getUsername());
		}

		if (contribution.getPlace() != null && !Strings.isNullOrEmpty(contribution.getPlace().getName())) {
			description.append("\n" + contribution.getPlace().getName());
		}
		
		if (contribution.getBody() != null) {
			description.append("\n\n" + contribution.getBody());
		}

		return description;
	}
	
}
