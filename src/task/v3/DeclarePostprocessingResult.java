package task.v3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import data.v3.ActivityRelationsContainer;

public class DeclarePostprocessingResult {
	
	private Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelationsMap = new LinkedHashMap<DiscoveredActivity, ActivityRelationsContainer>();
	
	public void addActivityRelationsContainer(DiscoveredActivity discoveredActivity, ActivityRelationsContainer activityRelationsContainer) {
		activityToRelationsMap.put(discoveredActivity, activityRelationsContainer);
	}
	
	public Set<DiscoveredActivity> getAllActivities() {
		//Returning a new set to avoid accidental modifications of activityToRelationsMap
		return new LinkedHashSet<DiscoveredActivity>(activityToRelationsMap.keySet());
	}
	
	//Related activities
	public Set<DiscoveredActivity> getAllFollowerActivities(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getAllFollowerActivities();
	}
	public Set<DiscoveredActivity> getAllPredecessorActivities(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getAllPredecessorActivities();
	}
	public List<DiscoveredActivity> getDirectlyRelatedActivities(DiscoveredActivity discoveredActivity) {
		List<DiscoveredActivity> directlyRelatedConstraints = new ArrayList<DiscoveredActivity>(getAllFollowerActivities(discoveredActivity));
		directlyRelatedConstraints.addAll(getAllPredecessorActivities(discoveredActivity));
		return directlyRelatedConstraints;
	}
	
	//Related constraints
	public Set<DiscoveredConstraint> getConstraintsToFollowers(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getConstraintsToFollowers();
	}
	public Set<DiscoveredConstraint> getConstraintsFromPredecessors(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getConstraintsFromPredecessors();
	}
	public List<DiscoveredConstraint> getDirectlyRelatedConstraints(DiscoveredActivity discoveredActivity) {
		List<DiscoveredConstraint> directlyRelatedConstraints = new ArrayList<DiscoveredConstraint>(getConstraintsToFollowers(discoveredActivity));
		directlyRelatedConstraints.addAll(getConstraintsFromPredecessors(discoveredActivity));
		return directlyRelatedConstraints;
	}
	public Set<DiscoveredConstraint> getConstraintsAmongFollowers(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getConstraintsAmongFollowers();
	}
	public Set<DiscoveredConstraint> getConstraintsAmongPredecessors(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getConstraintsAmongPredecessors();
	}
	
	//Potential closest followers/predecessors
	public Set<DiscoveredActivity> getPotentialNextActivities(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getPotentialNextActivities();
	}
	public Set<DiscoveredActivity> getPotentialNextDecisions(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getPotentialNextDecisions();
	}
	public Set<DiscoveredActivity> getPotentialPrevActivities(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getPotentialPrevActivities();
	}
	public Set<DiscoveredActivity> getPotentialPrevDecisions(DiscoveredActivity discoveredActivity) {
		return activityToRelationsMap.get(discoveredActivity).getPotentialPrevDecisions();
	}

}
