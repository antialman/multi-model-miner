package model.v2;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import data.DiscoveredActivity;

public class TransitionNode extends Node {
	private DiscoveredActivity discoveredActivity;
	private String transitionLabel;
	private boolean isSilent;
	private boolean isSkipStart;
	private boolean isSkipEnd;
	
	private Set<PlaceNode> incomingPlaces = new TreeSet<PlaceNode>();
	private Set<PlaceNode> outgoingPlaces = new TreeSet<PlaceNode>();
	
	TransitionNode(int nodeId, DiscoveredActivity discoveredActivity) {
		super(nodeId);
		this.discoveredActivity = discoveredActivity;
		this.transitionLabel = discoveredActivity.getActivityName();
		this.isSilent = false;
	}
	
	TransitionNode(int nodeId) {
		super(nodeId);
		this.isSilent = true;
	}
	
	TransitionNode(int nodeId, boolean isSkipStart, boolean isSkipEnd) {
		super(nodeId);
		this.isSilent = true;
		this.isSkipStart = isSkipStart;
		this.isSkipEnd = isSkipEnd;
	}
	
	public DiscoveredActivity getDiscoveredActivity() {
		return discoveredActivity;
	}
	public boolean isSilent() {
		return isSilent;
	}
	public boolean isSkipStart() {
		return isSkipStart;
	}
	public boolean isSkipEnd() {
		return isSkipEnd;
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
	
	
	public void clearAllPlaces() {
		Iterator<PlaceNode> inIterator = incomingPlaces.iterator();
		while (inIterator.hasNext()) {
			PlaceNode p = inIterator.next();
			inIterator.remove();
			p.remOutgoingTransition(this);
		}
		Iterator<PlaceNode> outIterator = outgoingPlaces.iterator();
		while (outIterator.hasNext()) {
			PlaceNode p = outIterator.next();
			outIterator.remove();
			p.remIncomingTransition(this);
		}
	}

	@Override
	public String toString() {
		return "TransitionNode [discoveredActivity=" + discoveredActivity + ", transitionLabel=" + transitionLabel
				+ ", isSilent=" + isSilent + ", isSkipStart=" + isSkipStart + ", isSkipEnd=" + isSkipEnd
				+ ", incomingPlaces: " + incomingPlaces.size() + ", outgoingPlaces: " + outgoingPlaces.size() + "]";
	}
	
	

}
