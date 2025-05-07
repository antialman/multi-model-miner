package task.v3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;

public class RefinedClosenessTaskResult {
	
	private Map<DiscoveredActivity, List<Set<DiscoveredActivity>>> followerGroupsMap = new HashMap<DiscoveredActivity, List<Set<DiscoveredActivity>>>();
	
	public void addNextFollowerGroup(DiscoveredActivity followerGroupOf, Set<DiscoveredActivity> followerGroup) {
		if (!followerGroupsMap.containsKey(followerGroupOf)) {
			followerGroupsMap.put(followerGroupOf, new ArrayList<Set<DiscoveredActivity>>());
		}
		followerGroupsMap.get(followerGroupOf).add(followerGroup);
	}
	public List<Set<DiscoveredActivity>> getFollowerGroups(DiscoveredActivity followerGroupOf) {
		return followerGroupsMap.get(followerGroupOf);
	}
	

}
