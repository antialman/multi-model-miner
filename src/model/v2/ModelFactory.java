package model.v2;

import data.DiscoveredActivity;

public class ModelFactory {
	 
	private int nextNodeId;
	private PlaceNode initialPlace; //Only one possible because of a fixed artificial start activity is added to all traces
	private PlaceNode finalPlace; //Only one possible because of a fixed artificial end activity is added to all traces
	
	public ModelFactory() {
		this.nextNodeId = 0;
		this.initialPlace = new PlaceNode(nextNodeId++, true, false); 
		this.finalPlace = new PlaceNode(nextNodeId++, false, true);
	}
	
	
	public TransitionNode getNewLabeledTransition(DiscoveredActivity da) {
		return new TransitionNode(nextNodeId++, da);
	}
	public TransitionNode getNewRoutingTransition() {
		return new TransitionNode(nextNodeId++, null);
	}
	public TransitionNode getNewSkipStart() {
		return new TransitionNode(nextNodeId++, null);
	}
	public TransitionNode getNewSkipEnd() {
		return new TransitionNode(nextNodeId++, null);
	}
	
	public PlaceNode getNewPlace() {
		return new PlaceNode(nextNodeId++, false, false);
	}
	public PlaceNode getInitialPlace() {
		return initialPlace;
	}
	public PlaceNode getFinalPlace() {
		return finalPlace;
	}
	
}
