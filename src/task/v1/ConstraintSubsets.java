package task.v1;

import java.util.List;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;

public class ConstraintSubsets {
	
	private List<DiscoveredActivity> reqActivities;
	private List<DiscoveredActivity> noRepActivities;
	private List<DiscoveredActivity> noCardActivities;
	
	private List<DiscoveredActivity> sucActivities;
	private List<DiscoveredActivity> preActivities;
	private List<DiscoveredActivity> resActivities;
	private List<DiscoveredActivity> notcoActivities;
	
	private List<DiscoveredConstraint> sucConstraints;
	private List<DiscoveredConstraint> preConstraints;
	private List<DiscoveredConstraint> resConstraints;
	private List<DiscoveredConstraint> notcoConstraints;
	
	public ConstraintSubsets() {
	}
	
	public void setReqActivities(List<DiscoveredActivity> reqActivities) {
		this.reqActivities = reqActivities;
	}
	
	public List<DiscoveredActivity> getReqActivities() {
		return reqActivities;
	}
	
	public void setNoRepActivities(List<DiscoveredActivity> noRepActivities) {
		this.noRepActivities = noRepActivities;
	}
	
	public List<DiscoveredActivity> getNoRepActivities() {
		return noRepActivities;
	}
	
	public void setNoCardActivities(List<DiscoveredActivity> noCardActivities) {
		this.noCardActivities = noCardActivities;
	}
	public List<DiscoveredActivity> getNoCardActivities() {
		return noCardActivities;
	}
	
	public void setSucActivities(List<DiscoveredActivity> sucActivities) {
		this.sucActivities = sucActivities;
	}
	public List<DiscoveredActivity> getSucActivities() {
		return sucActivities;
	}
	public void setPreActivities(List<DiscoveredActivity> preActivities) {
		this.preActivities = preActivities;
	}
	public List<DiscoveredActivity> getPreActivities() {
		return preActivities;
	}
	public void setResActivities(List<DiscoveredActivity> resActivities) {
		this.resActivities = resActivities;
	}
	public List<DiscoveredActivity> getResActivities() {
		return resActivities;
	}
	public void setNotcoActivities(List<DiscoveredActivity> notcoActivities) {
		this.notcoActivities = notcoActivities;
	}
	public List<DiscoveredActivity> getNotcoActivities() {
		return notcoActivities;
	}
	
	public void setSucConstraints(List<DiscoveredConstraint> sucConstraints) {
		this.sucConstraints = sucConstraints;
	}
	public List<DiscoveredConstraint> getSucConstraints() {
		return sucConstraints;
	}
	public void setPreConstraints(List<DiscoveredConstraint> preConstraints) {
		this.preConstraints = preConstraints;
	}
	public List<DiscoveredConstraint> getPreConstraints() {
		return preConstraints;
	}
	public void setResConstraints(List<DiscoveredConstraint> resConstraints) {
		this.resConstraints = resConstraints;
	}
	public List<DiscoveredConstraint> getResConstraints() {
		return resConstraints;
	}
	public void setNotcoConstraints(List<DiscoveredConstraint> notcoConstraints) {
		this.notcoConstraints = notcoConstraints;
	}
	public List<DiscoveredConstraint> getNotcoConstraints() {
		return notcoConstraints;
	}

}
