package task;

import java.util.Map;

import data.ActivityRelations;
import data.DiscoveredActivity;

public class InitialFragments {
	
	private Map<DiscoveredActivity, ActivityRelations> activityRelationsMap;

	public InitialFragments() {
	}
	
	public void setActivityRelationsMap(Map<DiscoveredActivity, ActivityRelations> activityRelationsMap) {
		this.activityRelationsMap = activityRelationsMap;
	}
	public Map<DiscoveredActivity, ActivityRelations> getActivityRelationsMap() {
		return activityRelationsMap;
	}
}
