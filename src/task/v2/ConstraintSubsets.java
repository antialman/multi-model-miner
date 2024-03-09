package task.v2;

import java.util.List;
import data.DiscoveredActivity;
import data.DiscoveredConstraint;

public class ConstraintSubsets {
	
	//Activity cardinalities
	private List<DiscoveredActivity> reqActivities;
	private List<DiscoveredActivity> noRepActivities;
	private List<DiscoveredActivity> noCardActivities;
	
	//Activity lists based on pruned constraint subsets (used for visualising pruned constraint subsets)
	private List<DiscoveredActivity> prunedSucActivities;
	private List<DiscoveredActivity> prunedPreActivities;
	private List<DiscoveredActivity> prunedResActivities;
	private List<DiscoveredActivity> prunedNotcoActivities;
	
	//Constraint subsets pruned based on constraint type
	private List<DiscoveredConstraint> prunedSucConstraints;
	private List<DiscoveredConstraint> prunedPreConstraints;
	private List<DiscoveredConstraint> prunedResConstraints;
	private List<DiscoveredConstraint> prunedNotcoConstraints;
	
	//Constraint subsets containing all discovered constraints
	private List<DiscoveredConstraint> allSucConstraints;
	private List<DiscoveredConstraint> allPreConstraints;
	private List<DiscoveredConstraint> allResConstraints;
	private List<DiscoveredConstraint> allNotcoConstraints;
	
	
	public ConstraintSubsets() {
	}
	
	
	//Getters and setters for cardinality lists
	public List<DiscoveredActivity> getReqActivities() {
		return reqActivities;
	}
	public List<DiscoveredActivity> getNoRepActivities() {
		return noRepActivities;
	}
	public List<DiscoveredActivity> getNoCardActivities() {
		return noCardActivities;
	}
	public void setReqActivities(List<DiscoveredActivity> reqActivities) {
		this.reqActivities = reqActivities;
	}
	public void setNoRepActivities(List<DiscoveredActivity> noRepActivities) {
		this.noRepActivities = noRepActivities;
	}
	public void setNoCardActivities(List<DiscoveredActivity> noCardActivities) {
		this.noCardActivities = noCardActivities;
	}
	
	
	//Getters and setters for activity lists based on pruned constraint subsets
	public List<DiscoveredActivity> getPrunedSucActivities() {
		return prunedSucActivities;
	}
	public List<DiscoveredActivity> getPrunedPreActivities() {
		return prunedPreActivities;
	}
	public List<DiscoveredActivity> getPrunedResActivities() {
		return prunedResActivities;
	}
	public List<DiscoveredActivity> getPrunedNotcoActivities() {
		return prunedNotcoActivities;
	}
	public void setPrunedSucActivities(List<DiscoveredActivity> prunedSucActivities) {
		this.prunedSucActivities = prunedSucActivities;
	}
	public void setPrunedPreActivities(List<DiscoveredActivity> prunedPreActivities) {
		this.prunedPreActivities = prunedPreActivities;
	}
	public void setPrunedResActivities(List<DiscoveredActivity> prunedResActivities) {
		this.prunedResActivities = prunedResActivities;
	}
	public void setPrunedNotcoActivities(List<DiscoveredActivity> prunedNotcoActivities) {
		this.prunedNotcoActivities = prunedNotcoActivities;
	}
	
	
	//Getters and setters for constraint subsets pruned based on constraint type
	public List<DiscoveredConstraint> getPrunedSucConstraints() {
		return prunedSucConstraints;
	}
	public List<DiscoveredConstraint> getPrunedPreConstraints() {
		return prunedPreConstraints;
	}
	public List<DiscoveredConstraint> getPrunedResConstraints() {
		return prunedResConstraints;
	}
	public List<DiscoveredConstraint> getPrunedNotcoConstraints() {
		return prunedNotcoConstraints;
	}
	public void setPrunedSucConstraints(List<DiscoveredConstraint> prunedSucConstraints) {
		this.prunedSucConstraints = prunedSucConstraints;
	}
	public void setPrunedPreConstraints(List<DiscoveredConstraint> prunedPreConstraints) {
		this.prunedPreConstraints = prunedPreConstraints;
	}
	public void setPrunedResConstraints(List<DiscoveredConstraint> prunedResConstraints) {
		this.prunedResConstraints = prunedResConstraints;
	}
	public void setPrunedNotcoConstraints(List<DiscoveredConstraint> prunedNotcoConstraints) {
		this.prunedNotcoConstraints = prunedNotcoConstraints;
	}
	
	
	//Getters and setters for constraint subsets containing all discovered constraints
	public List<DiscoveredConstraint> getAllSucConstraints() {
		return allSucConstraints;
	}
	public List<DiscoveredConstraint> getAllPreConstraints() {
		return allPreConstraints;
	}
	public List<DiscoveredConstraint> getAllResConstraints() {
		return allResConstraints;
	}
	public List<DiscoveredConstraint> getAllNotcoConstraints() {
		return allNotcoConstraints;
	}
	public void setAllSucConstraints(List<DiscoveredConstraint> allSucConstraints) {
		this.allSucConstraints = allSucConstraints;
	}
	public void setAllPreConstraints(List<DiscoveredConstraint> allPreConstraints) {
		this.allPreConstraints = allPreConstraints;
	}
	public void setAllResConstraints(List<DiscoveredConstraint> allResConstraints) {
		this.allResConstraints = allResConstraints;
	}
	public void setAllNotcoConstraints(List<DiscoveredConstraint> allNotcoConstraints) {
		this.allNotcoConstraints = allNotcoConstraints;
	}

}
