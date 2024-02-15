package model;

import java.util.HashSet;
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
	}
	public void remIncomingTransition(TransitionNode incomingTransition) {
		incomingTransitions.remove(incomingTransition);
	}
	public Set<TransitionNode> getIncomingTransitions() {
		return incomingTransitions;
	}
	
	public void addOutgoingTransition(TransitionNode outgoingTransition) {
		outgoingTransitions.add(outgoingTransition);
	}
	public void remOutgoingTransition(TransitionNode outgoingTransition) {
		outgoingTransitions.remove(outgoingTransition);
	}
	public Set<TransitionNode> getOutgoingTransitions() {
		return outgoingTransitions;
	}
	
}
