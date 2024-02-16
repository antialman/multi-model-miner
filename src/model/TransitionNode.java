package model;

import java.util.HashSet;
import java.util.Set;

import data.DiscoveredActivity;

public class TransitionNode {
	private int nodeId;
	private DiscoveredActivity discoveredActivity;
	private String transitionLabel;
	private boolean isSilent;
	private boolean isFragmentMain;
	
	private Set<PlaceNode> incomingPlaces = new HashSet<PlaceNode>();
	private Set<PlaceNode> outgoingPlaces = new HashSet<PlaceNode>();
	
	public TransitionNode(int nodeId, DiscoveredActivity discoveredActivity) {
		this.nodeId = nodeId;
		this.discoveredActivity = discoveredActivity;
		if (discoveredActivity != null) {
			this.transitionLabel = discoveredActivity.getActivityName();
			this.isSilent = false;
		} else {
			this.isSilent = true;
		}
		this.isFragmentMain = false;
	}
	
	
	public int getNodeId() {
		return nodeId;
	}
	public DiscoveredActivity getDiscoveredActivity() {
		return discoveredActivity;
	}
	public boolean isSilent() {
		return isSilent;
	}
	public String getTransitionLabel() {
		return transitionLabel;
	}
	
	public void addIncomingPlace(PlaceNode incomingPlace) {
		incomingPlaces.add(incomingPlace);
		if (!incomingPlace.getOutgoingTransitions().contains(this)) {
			incomingPlace.addOutgoingTransition(this);
		}
	}
	public void remIncomingPlace(PlaceNode incomingPlace) {
		incomingPlaces.remove(incomingPlace);
		if (incomingPlace.getOutgoingTransitions().contains(this)) {
			incomingPlace.remOutgoingTransition(this);
		}
	}
	public Set<PlaceNode> getIncomingPlaces() {
		return incomingPlaces;
	}
	
	public void addOutgoingPlace(PlaceNode outgoingPlace) {
		outgoingPlaces.add(outgoingPlace);
		if (!outgoingPlace.getIncomingTransitions().contains(this)) {
			outgoingPlace.addIncomingTransition(this);
		}
	}
	public void remOutgoingPlace(PlaceNode outgoingPlace) {
		outgoingPlaces.remove(outgoingPlace);
		if (outgoingPlace.getIncomingTransitions().contains(this)) {
			outgoingPlace.remIncomingTransition(this);
		}
	}
	public Set<PlaceNode> getOutgoingPlaces() {
		return outgoingPlaces;
	}
	
	public void setFragmentMain(boolean isFragmentMain) {
		this.isFragmentMain = isFragmentMain;
	}
	public boolean isFragmentMain() {
		return isFragmentMain;
	}

}
