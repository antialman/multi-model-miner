package task;

import java.util.List;

import data.DiscoveredActivity;

public class InitialRelations {
	
	private List<DiscoveredActivity> reqActivities;
	private List<DiscoveredActivity> noRepActivities;
	private List<DiscoveredActivity> noCardActivities;
	
	public InitialRelations() {
	}
	
	public void setReqActivities(List<DiscoveredActivity> reqActivities) {
		this.reqActivities = reqActivities;
	}
	
	public List<DiscoveredActivity> getReqActivities() {
		return reqActivities;
	}
	
	public void setNoRepActivities(List<DiscoveredActivity> noRepActivities) {
		this.noRepActivities = noRepActivities;
	}
	
	public List<DiscoveredActivity> getNoRepActivities() {
		return noRepActivities;
	}
	
	public void setNoCardActivities(List<DiscoveredActivity> noCardActivities) {
		this.noCardActivities = noCardActivities;
	}
	public List<DiscoveredActivity> getNoCardActivities() {
		return noCardActivities;
	}

}
