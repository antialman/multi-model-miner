package task.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.ActivityRelationsContainer;
import data.DiscoveredActivity;
import data.DiscoveredConstraint;

public class DeclarePostprocessingResult {
	
	private List<DiscoveredActivity> allActivities; //Pruning keeps all activities
	private List<DiscoveredConstraint> allConstraints; //As returned by DeclareDiscoveryTask
	private List<DiscoveredConstraint> prunedConstraints; //Transitive closure from Declare miner, followed by basic hierarchy-based pruning
	
	//Activity cardinalities
	private List<DiscoveredActivity> reqActivities;
	private List<DiscoveredActivity> noRepActivities;
	private List<DiscoveredActivity> noCardActivities;
	
	//For showing Declare models of constraint subsets 
	private List<DiscoveredActivity> succActivities;
	private List<DiscoveredActivity> precActivities;
	private List<DiscoveredActivity> respActivities;
	private List<DiscoveredActivity> notcoActivities;
	private List<DiscoveredConstraint> succPrunedConstraints;
	private List<DiscoveredConstraint> precPrunedConstraints;
	private List<DiscoveredConstraint> respPrunedConstraints;
	private List<DiscoveredConstraint> notcoAllConstraints;
	
	//Data structure for building the Petri nets
	private Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelationsMap = new HashMap<DiscoveredActivity, ActivityRelationsContainer>();

	
	
	public DeclarePostprocessingResult() {
	}
	
	public Map<DiscoveredActivity, ActivityRelationsContainer> getActivityToRelationsMap() {
		return activityToRelationsMap;
	}
	public void setActivityToRelationsMap(Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelationsMap) {
		this.activityToRelationsMap = activityToRelationsMap;
	}

	
	
	public List<DiscoveredActivity> getAllActivities() {
		return allActivities;
	}
	public void setAllActivities(List<DiscoveredActivity> allActivities) {
		this.allActivities = allActivities;
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

	public List<DiscoveredActivity> getReqActivities() {
		return reqActivities;
	}
	public void setReqActivities(List<DiscoveredActivity> reqActivities) {
		this.reqActivities = reqActivities;
	}

	public List<DiscoveredActivity> getNoRepActivities() {
		return noRepActivities;
	}
	public void setNoRepActivities(List<DiscoveredActivity> noRepActivities) {
		this.noRepActivities = noRepActivities;
	}

	public List<DiscoveredActivity> getNoCardActivities() {
		return noCardActivities;
	}
	public void setNoCardActivities(List<DiscoveredActivity> noCardActivities) {
		this.noCardActivities = noCardActivities;
	}

	public List<DiscoveredActivity> getSuccActivities() {
		return succActivities;
	}
	public void setSuccActivities(List<DiscoveredActivity> succActivities) {
		this.succActivities = succActivities;
	}

	public List<DiscoveredActivity> getPrecActivities() {
		return precActivities;
	}
	public void setPrecActivities(List<DiscoveredActivity> precActivities) {
		this.precActivities = precActivities;
	}

	public List<DiscoveredActivity> getRespActivities() {
		return respActivities;
	}
	public void setRespActivities(List<DiscoveredActivity> respActivities) {
		this.respActivities = respActivities;
	}

	public List<DiscoveredActivity> getNotcoActivities() {
		return notcoActivities;
	}
	public void setNotcoActivities(List<DiscoveredActivity> notcoActivities) {
		this.notcoActivities = notcoActivities;
	}

	public List<DiscoveredConstraint> getSuccPrunedConstraints() {
		return succPrunedConstraints;
	}
	public void setSuccPrunedConstraints(List<DiscoveredConstraint> succPrunedConstraints) {
		this.succPrunedConstraints = succPrunedConstraints;
	}

	public List<DiscoveredConstraint> getPrecPrunedConstraints() {
		return precPrunedConstraints;
	}
	public void setPrecPrunedConstraints(List<DiscoveredConstraint> precPrunedConstraints) {
		this.precPrunedConstraints = precPrunedConstraints;
	}

	public List<DiscoveredConstraint> getRespPrunedConstraints() {
		return respPrunedConstraints;
	}
	public void setRespPrunedConstraints(List<DiscoveredConstraint> respPrunedConstraints) {
		this.respPrunedConstraints = respPrunedConstraints;
	}

	public List<DiscoveredConstraint> getNotcoAllConstraints() {
		return notcoAllConstraints;
	}
	public void setNotcoAllConstraints(List<DiscoveredConstraint> notcoAllConstraints) {
		this.notcoAllConstraints = notcoAllConstraints;
	}
	
}
