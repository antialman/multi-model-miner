package model;

import java.util.HashSet;
import java.util.Set;

import data.DiscoveredActivity;

public class TransitionNode {
	private int nodeId;
	private DiscoveredActivity discoveredActivity;
	private boolean isVisible;
	private String transitionLabel;
	
	private Set<PlaceNode> incomingPlaces = new HashSet<PlaceNode>();
	private Set<PlaceNode> outgoingPlaces = new HashSet<PlaceNode>();
	
	public TransitionNode(int nodeId, DiscoveredActivity discoveredActivity, boolean isVisible) {
		this.nodeId = nodeId;
		this.discoveredActivity = discoveredActivity;
		this.isVisible = isVisible;
		if (isVisible) {
			this.transitionLabel = discoveredActivity.getActivityName();
		}
	}
	
	
	public int getNodeId() {
		return nodeId;
	}
	public DiscoveredActivity getDiscoveredActivity() {
		return discoveredActivity;
	}
	public boolean isVisible() {
		return isVisible;
	}
	public String getTransitionLabel() {
		return transitionLabel;
	}
	
	public void addIncomingPlace(PlaceNode incomingPlace) {
		incomingPlaces.add(incomingPlace);
	}
	public void remIncomingPlace(PlaceNode incomingPlace) {
		incomingPlaces.remove(incomingPlace);
	}
	public Set<PlaceNode> getIncomingPlaces() {
		return incomingPlaces;
	}
	
	public void addOutgoingPlace(PlaceNode outgoingPlace) {
		outgoingPlaces.add(outgoingPlace);
	}
	public void remOutgoingPlace(PlaceNode outgoingPlace) {
		outgoingPlaces.remove(outgoingPlace);
	}
	public Set<PlaceNode> getOutgoingPlaces() {
		return outgoingPlaces;
	}

}
