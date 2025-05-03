package task.v3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import data.v3.ActivityRelationsContainer;

public class DeclarePostprocessingResult {
	
	private Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelationsMap = new HashMap<DiscoveredActivity, ActivityRelationsContainer>();
	
	public void addActivityRelationsContainer(DiscoveredActivity discoveredActivity, ActivityRelationsContainer activityRelationsContainer) {
		activityToRelationsMap.put(discoveredActivity, activityRelationsContainer);
	}
	
	//Related activities
	public Set<DiscoveredActivity> getAllFollowerActivities(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getAllFollowerActivities();
	}
	public Set<DiscoveredActivity> getAllPrecederActivities(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getAllPrecederActivities();
	}
	public List<DiscoveredActivity> getDirectlyRelatedActivities(DiscoveredActivity discoveredActivity) {
		List<DiscoveredActivity> directlyRelatedConstraints = new ArrayList<DiscoveredActivity>(getAllFollowerActivities(discoveredActivity));
		directlyRelatedConstraints.addAll(getAllPrecederActivities(discoveredActivity));
		return directlyRelatedConstraints;
	}
	
	//Related constraints
	public Set<DiscoveredConstraint> getConstraintsToFollowers(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getConstraintsToFollowers();
	}
	public Set<DiscoveredConstraint> getConstraintsFromPreceders(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getConstraintsFromPreceders();
	}
	public List<DiscoveredConstraint> getDirectlyRelatedConstraints(DiscoveredActivity discoveredActivity) {
		List<DiscoveredConstraint> directlyRelatedConstraints = new ArrayList<DiscoveredConstraint>(getConstraintsToFollowers(discoveredActivity));
		directlyRelatedConstraints.addAll(getConstraintsFromPreceders(discoveredActivity));
		return directlyRelatedConstraints;
	}
	public Set<DiscoveredConstraint> getConstraintsAmongFollowers(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getConstraintsAmongFollowers();
	}
	public Set<DiscoveredConstraint> getConstraintsAmongPreceders(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getConstraintsAmongPreceders();
	}
	
	//Potential closest followers/preceders
	public Set<DiscoveredActivity> getPotentialClosestFollowers(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getPotentialClosestFollowers();
	}
	public Set<DiscoveredActivity> getPotentialClosestPreceders(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getPotentialClosestPreceders();
	}

}
