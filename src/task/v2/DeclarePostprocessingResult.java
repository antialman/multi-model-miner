package task.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.ActivityRelationsContainer;
import data.DiscoveredActivity;
import data.DiscoveredConstraint;

public class DeclarePostprocessingResult {
	
	private List<DiscoveredActivity> activities; //Pruning keeps all activities
	private List<DiscoveredConstraint> allConstraints; //As returned by DeclareDiscoveryTask
	private List<DiscoveredConstraint> prunedConstraints; //As returned by DeclareDiscoveryTask
	
	//Main data structure for building the Petri nets
	private Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelations = new HashMap<DiscoveredActivity, ActivityRelationsContainer>();

	public DeclarePostprocessingResult() {
	}

	public List<DiscoveredActivity> getActivities() {
		return activities;
	}
	public void setActivities(List<DiscoveredActivity> activities) {
		this.activities = activities;
	}

	public List<DiscoveredConstraint> getAllConstraints() {
		return allConstraints;
	}
	public void setAllConstraints(List<DiscoveredConstraint> allConstraints) {
		this.allConstraints = allConstraints;
	}
	
	public List<DiscoveredConstraint> getPrunedConstraints() {
		return prunedConstraints;
	}
	public void setPrunedConstraints(List<DiscoveredConstraint> prunedConstraints) {
		this.prunedConstraints = prunedConstraints;
	}
	
	public Map<DiscoveredActivity, ActivityRelationsContainer> getActivityToRelations() {
		return activityToRelations;
	}
}