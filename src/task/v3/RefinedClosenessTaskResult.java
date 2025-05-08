package task.v3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;

public class RefinedClosenessTaskResult {

	private Map<DiscoveredActivity, List<Set<DiscoveredActivity>>> followerRespGroupsMap = new HashMap<DiscoveredActivity, List<Set<DiscoveredActivity>>>();
	private Map<DiscoveredActivity, List<Set<DiscoveredActivity>>> followerPrecGroupsMap = new HashMap<DiscoveredActivity, List<Set<DiscoveredActivity>>>();
	private Map<DiscoveredActivity, List<List<Set<DiscoveredActivity>>>> followerSuccRelationsMap = new HashMap<DiscoveredActivity, List<List<Set<DiscoveredActivity>>>>();
	private Map<DiscoveredActivity, List<Set<DiscoveredActivity>>> predecessorRespGroupsMap = new HashMap<DiscoveredActivity, List<Set<DiscoveredActivity>>>();
	private Map<DiscoveredActivity, List<Set<DiscoveredActivity>>> predecessorPrecGroupsMap = new HashMap<DiscoveredActivity, List<Set<DiscoveredActivity>>>();
	private Map<DiscoveredActivity, List<List<Set<DiscoveredActivity>>>> predecessorSuccRelationsMap = new HashMap<DiscoveredActivity, List<List<Set<DiscoveredActivity>>>>();

	//Follower methods
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

	//TODO: Needs refactoring
	public void addFollowerSuccRelation(DiscoveredActivity succRelationOf, Set<DiscoveredActivity> activityGroupA, Set<DiscoveredActivity> activityGroupB) {
		if (!followerSuccRelationsMap.containsKey(succRelationOf)) {
			followerSuccRelationsMap.put(succRelationOf, new ArrayList<List<Set<DiscoveredActivity>>>());
		}
		List<Set<DiscoveredActivity>> succList = new ArrayList<Set<DiscoveredActivity>>();
		succList.add(activityGroupA);
		succList.add(activityGroupB);
		followerSuccRelationsMap.get(succRelationOf).add(succList);
	}
	public List<List<Set<DiscoveredActivity>>> getFollowerSuccRelations(DiscoveredActivity succRelationsOf) {
		return followerSuccRelationsMap.get(succRelationsOf);
	}



	//Predecessor methods
	public void addNextPredecessorRespGroup(DiscoveredActivity predecessorGroupOf, Set<DiscoveredActivity> predecessorRespGroup) {
		if (!predecessorRespGroupsMap.containsKey(predecessorGroupOf)) {
			predecessorRespGroupsMap.put(predecessorGroupOf, new ArrayList<Set<DiscoveredActivity>>());
		}
		predecessorRespGroupsMap.get(predecessorGroupOf).add(predecessorRespGroup);
	}
	public List<Set<DiscoveredActivity>> getPredecessorRespGroups(DiscoveredActivity predecessorGroupOf) {
		return predecessorRespGroupsMap.get(predecessorGroupOf);
	}

	public void addNextPredecessorPrecGroup(DiscoveredActivity predecessorGroupOf, Set<DiscoveredActivity> predecessorPrecGroup) {
		if (!predecessorPrecGroupsMap.containsKey(predecessorGroupOf)) {
			predecessorPrecGroupsMap.put(predecessorGroupOf, new ArrayList<Set<DiscoveredActivity>>());
		}
		predecessorPrecGroupsMap.get(predecessorGroupOf).add(0, predecessorPrecGroup); //Adding to the front of the list keeps the groups in the execution order
	}
	public List<Set<DiscoveredActivity>> getPredecessorPrecGroups(DiscoveredActivity predecessorGroupOf) {
		return predecessorPrecGroupsMap.get(predecessorGroupOf);
	}

	//TODO: Needs refactoring
	public void addPredecessorSuccRelation(DiscoveredActivity succRelationOf, Set<DiscoveredActivity> activityGroupA, Set<DiscoveredActivity> activityGroupB) {
		if (!predecessorSuccRelationsMap.containsKey(succRelationOf)) {
			predecessorSuccRelationsMap.put(succRelationOf, new ArrayList<List<Set<DiscoveredActivity>>>());
		}
		List<Set<DiscoveredActivity>> succList = new ArrayList<Set<DiscoveredActivity>>();
		succList.add(activityGroupA);
		succList.add(activityGroupB);
		predecessorSuccRelationsMap.get(succRelationOf).add(succList);
	}
	public List<List<Set<DiscoveredActivity>>> getPredecessorSuccRelations(DiscoveredActivity succRelationsOf) {
		return predecessorSuccRelationsMap.get(succRelationsOf);
	}
}
