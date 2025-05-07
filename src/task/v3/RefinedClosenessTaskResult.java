package task.v3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;

public class RefinedClosenessTaskResult {
	
	private Map<DiscoveredActivity, List<Set<DiscoveredActivity>>> followerRespGroupsMap = new HashMap<DiscoveredActivity, List<Set<DiscoveredActivity>>>();
	private Map<DiscoveredActivity, List<Set<DiscoveredActivity>>> followerPrecGroupsMap = new HashMap<DiscoveredActivity, List<Set<DiscoveredActivity>>>();
	
	public void addNextFollowerRespGroup(DiscoveredActivity followerGroupOf, Set<DiscoveredActivity> followerRespGroup) {
		if (!followerRespGroupsMap.containsKey(followerGroupOf)) {
			followerRespGroupsMap.put(followerGroupOf, new ArrayList<Set<DiscoveredActivity>>());
		}
		followerRespGroupsMap.get(followerGroupOf).add(followerRespGroup);
	}
	public List<Set<DiscoveredActivity>> getFollowerRespGroups(DiscoveredActivity followerGroupOf) {
		return followerRespGroupsMap.get(followerGroupOf);
	}
	
	public void addNextFollowerPrecGroup(DiscoveredActivity followerGroupOf, Set<DiscoveredActivity> followerPrecGroup) {
		if (!followerPrecGroupsMap.containsKey(followerGroupOf)) {
			followerPrecGroupsMap.put(followerGroupOf, new ArrayList<Set<DiscoveredActivity>>());
		}
		followerPrecGroupsMap.get(followerGroupOf).add(0, followerPrecGroup); //Adding to the front of the list keeps the groups in the execution order
	}
	public List<Set<DiscoveredActivity>> getFollowerPrecGroups(DiscoveredActivity followerGroupOf) {
		return followerPrecGroupsMap.get(followerGroupOf);
	}
}
