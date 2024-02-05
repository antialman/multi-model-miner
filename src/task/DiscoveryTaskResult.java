package task;

import java.util.List;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;

public class DiscoveryTaskResult {
	
	private List<DiscoveredActivity> activities;
	private List<DiscoveredConstraint> constraints;

	public DiscoveryTaskResult() {
	}

	public List<DiscoveredActivity> getActivities() {
		return activities;
	}
	
	public void setActivities(List<DiscoveredActivity> activities) {
		this.activities = activities;
	}

	public List<DiscoveredConstraint> getConstraints() {
		return constraints;
	}
	
	public void setConstraints(List<DiscoveredConstraint> constraints) {
		this.constraints = constraints;
	}
}