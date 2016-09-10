package com.contribly.reference.android.example.activities.views;

import com.contribly.reference.android.example.utils.DateTimeHelper;

import org.joda.time.DateTime;

import com.contribly.client.model.Assignment;

public class AssignmentDescriptionBuilder {

	public StringBuilder composeMetaDataDescription(Assignment assignment) {
		StringBuilder metaData = new StringBuilder();
		if (assignment.getEnds() != null) {
			metaData.append("Ends " + DateTimeHelper.calculateTimeTaken(DateTime.now().toDate(), assignment.getEnds().toDate()) + " from now\n");
		}
		return metaData;
	}

}
