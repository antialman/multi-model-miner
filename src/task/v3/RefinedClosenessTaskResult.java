package task.v3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;

public class RefinedClosenessTaskResult {
	
	private Map<DiscoveredActivity, Set<DiscoveredActivity>> firstFollowerGroupMap = new HashMap<DiscoveredActivity, Set<DiscoveredActivity>>();
	
	public void addFirstFollowerGroup(DiscoveredActivity followerGroupOf, Set<DiscoveredActivity> followerGroup) {
		firstFollowerGroupMap.put(followerGroupOf, followerGroup);
	}
	public Set<DiscoveredActivity> getFirstFollowerGroup(DiscoveredActivity followerGroupOf) {
		return firstFollowerGroupMap.get(followerGroupOf);
	}
	

}
