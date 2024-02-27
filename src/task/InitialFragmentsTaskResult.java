package task;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import model.PlaceNode;
import model.TransitionNode;

public class InitialFragmentsTaskResult {
	
	private Set<TransitionNode> fragmentMainTransitions = new LinkedHashSet<TransitionNode>();
	private Set<TransitionNode> transitionNodes = new HashSet<TransitionNode>();
	private Set<PlaceNode> placeNodes = new HashSet<PlaceNode>();

	public InitialFragmentsTaskResult() {
	}
	
	public Set<TransitionNode> getFragmentMainTransitions() {
		return fragmentMainTransitions;
	}
	public void addFragmentMainTransition(TransitionNode fragmentMainTransition) {
		fragmentMainTransitions.add(fragmentMainTransition);
		transitionNodes.add(fragmentMainTransition);
	}
	
	public Set<TransitionNode> getTransitionNodes() {
		return transitionNodes;
	}
	public void addTransitionNode(TransitionNode transitionNode) {
		transitionNodes.add(transitionNode);
	}
	
	public Set<PlaceNode> getPlaceNodes() {
		return placeNodes;
	}
	public void addPlaceNode(PlaceNode placeNode) {
		placeNodes.add(placeNode);
	}
}
