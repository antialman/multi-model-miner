package model.v2;

import data.DiscoveredActivity;

public class ModelFactory {
	 
	private int nextNodeId;
	
	public ModelFactory() {
		this.nextNodeId = 0;
	}
	
	
	public TransitionNode getNewActivityTransition(DiscoveredActivity da) {
		return new TransitionNode(nextNodeId++, da);
	}
	
	public TransitionNode getNewSilentTransition() {
		return new TransitionNode(nextNodeId++, null);
	}
	
	
	public PlaceNode getNewIntermediatePlace() {
		return new PlaceNode(nextNodeId++, false, false);
	}
	
	public PlaceNode getNewInitialPlace() {
		return new PlaceNode(nextNodeId++, true, false);
	}
	
	public PlaceNode getNewFinalPlace() {
		return new PlaceNode(nextNodeId++, false, true);
	}
	
}
