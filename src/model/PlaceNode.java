package model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PlaceNode {
	private int nodeId;
	
	private Set<TransitionNode> incomingTransitions = new HashSet<TransitionNode>();
	private Set<TransitionNode> outgoingTransitions = new HashSet<TransitionNode>();
	
	public PlaceNode(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public int getNodeId() {
		return nodeId;
	}
	
	public void addIncomingTransition(TransitionNode incomingTransition) {
		incomingTransitions.add(incomingTransition);
		if (!incomingTransition.getOutgoingPlaces().contains(this)) {
			incomingTransition.addOutgoingPlace(this);
		}
	}
	public void remIncomingTransition(TransitionNode incomingTransition) {
		incomingTransitions.remove(incomingTransition);
		if (incomingTransition.getOutgoingPlaces().contains(this)) {
			incomingTransition.remOutgoingPlace(this);
		}
	}
	public Set<TransitionNode> getIncomingTransitions() {
		return incomingTransitions;
	}
	
	public void addOutgoingTransition(TransitionNode outgoingTransition) {
		outgoingTransitions.add(outgoingTransition);
		if (!outgoingTransition.getIncomingPlaces().contains(this)) {
			outgoingTransition.addIncomingPlace(this);
		}
	}
	public void remOutgoingTransition(TransitionNode outgoingTransition) {
		outgoingTransitions.remove(outgoingTransition);
		if (outgoingTransition.getIncomingPlaces().contains(this)) {
			outgoingTransition.remIncomingPlace(this);
		}
	}
	public Set<TransitionNode> getOutgoingTransitions() {
		return outgoingTransitions;
	}
	
	public void clearAllTransitions() {
		Iterator<TransitionNode> inIterator = incomingTransitions.iterator();
		while (inIterator.hasNext()) {
			TransitionNode t = inIterator.next();
			inIterator.remove();
			t.remOutgoingPlace(this);
		}
		Iterator<TransitionNode> outIterator = outgoingTransitions.iterator();
		while (outIterator.hasNext()) {
			TransitionNode t = outIterator.next();
			outIterator.remove();
			t.remIncomingPlace(this);
		}
	}
	
}
