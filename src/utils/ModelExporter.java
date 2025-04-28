package utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import data.DiscoveredActivity;
import data.DiscoveredConstraint;

public class ModelExporter {

	// Private constructor to avoid unnecessary instantiation of the class
	private ModelExporter() {
		// TODO Auto-generated constructor stub
	}

	public static String getDeclString(List<DiscoveredActivity> filteredActivities, List<DiscoveredConstraint> filteredConstraints) {
		StringBuilder sb = new StringBuilder();

		Set<String> allAttributes = new HashSet<>();
		for (DiscoveredActivity discoveredActivity : filteredActivities) {
			sb.append("activity " + discoveredActivity.getActivityFullName() + "\n");

			if (discoveredActivity.getPayloadAttributes() != null && !discoveredActivity.getPayloadAttributes().isEmpty()) {
				sb.append("bind "+ discoveredActivity.getActivityFullName() + ": " + String.join(", ", discoveredActivity.getPayloadAttributes()) + "\n");
				allAttributes.addAll(discoveredActivity.getPayloadAttributes());
			}
		}

		for (DiscoveredConstraint discoveredConstraint : filteredConstraints) {
			sb.append(ConstraintUtils.getConstraintString(discoveredConstraint) + "\n");	
		}

		return sb.toString();
	}
}
