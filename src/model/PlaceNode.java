package model;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class PlaceNode {
	private int nodeId;
	
	private boolean isInitial;
	private boolean isFinal;
	
	
	private Set<TransitionNode> incomingTransitions = new LinkedHashSet<TransitionNode>();
	private Set<TransitionNode> outgoingTransitions = new LinkedHashSet<TransitionNode>();
	
	public PlaceNode(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public int getNodeId() {
		return nodeId;
	}
	
	public void setInitial(boolean isInitial) {
		this.isInitial = isInitial;
	}
	public boolean isInitial() {
		return isInitial;
	}
	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}
	public boolean isFinal() {
		return isFinal;
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

	@Override
	public String toString() {
		return "PlaceNode [nodeId=" + nodeId + ", incomingTransitions=" + incomingTransitions + ", outgoingTransitions="
				+ outgoingTransitions + "]";
	}
	
}
