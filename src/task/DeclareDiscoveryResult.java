package task;

import java.util.List;

import org.deckfour.xes.model.XLog;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;

public class DeclareDiscoveryResult {
	
	private List<DiscoveredActivity> activities;
	private List<DiscoveredConstraint> constraints;
	private XLog eventLog;

	public DeclareDiscoveryResult() {
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
	
	public XLog getEventLog() {
		return eventLog;
	}
	public void setEventLog(XLog eventLog) {
		this.eventLog = eventLog;
	}
}
