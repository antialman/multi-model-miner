package utils;

import java.util.HashMap;
import java.util.Map;
import model.v1.Node;
import model.v1.PlaceNode;
import model.v1.TransitionNode;

public class ModelUtils {
	// private constructor to avoid unnecessary instantiation of the class
	private ModelUtils() {
	}
	
	
	public static TransitionNode cloneFragment(TransitionNode mainTransition) {
		Map<Node, Node> oldToNewNodeMap = new HashMap<Node, Node>();
		return cloneTransitionNode(mainTransition, oldToNewNodeMap);
	}
	
	private static TransitionNode cloneTransitionNode(TransitionNode oldTransitionNode, Map<Node, Node> oldToNewNodeMap) {
		TransitionNode newTransitionNode = new TransitionNode(oldTransitionNode.getNodeId(), oldTransitionNode.getDiscoveredActivity());
		newTransitionNode.setFragmentMain(oldTransitionNode.isFragmentMain());
		oldToNewNodeMap.put(oldTransitionNode, newTransitionNode);
		
		for (PlaceNode oldPlaceNode : oldTransitionNode.getIncomingPlaces()) {
			if (oldToNewNodeMap.containsKey(oldPlaceNode)) {
				newTransitionNode.addIncomingPlace((PlaceNode) oldToNewNodeMap.get(oldPlaceNode));
			} else {
				newTransitionNode.addIncomingPlace(clonePlaceNode(oldPlaceNode, oldToNewNodeMap));
			}
		}
		
		for (PlaceNode oldPlaceNode : oldTransitionNode.getOutgoingPlaces()) {
			if (oldToNewNodeMap.containsKey(oldPlaceNode)) {
				newTransitionNode.addOutgoingPlace((PlaceNode) oldToNewNodeMap.get(oldPlaceNode));
			} else {
				newTransitionNode.addOutgoingPlace(clonePlaceNode(oldPlaceNode, oldToNewNodeMap));
			}
		}
		
		return newTransitionNode;
	}
	
	private static PlaceNode clonePlaceNode(PlaceNode oldPlaceNode, Map<Node, Node> oldToNewNodeMap) {
		PlaceNode newPlaceNode = new PlaceNode(oldPlaceNode.getNodeId());
		newPlaceNode.setInitial(oldPlaceNode.isInitial());
		newPlaceNode.setFinal(oldPlaceNode.isFinal());
		oldToNewNodeMap.put(oldPlaceNode, newPlaceNode);
		
		for (TransitionNode oldTransitionNode : oldPlaceNode.getIncomingTransitions()) {
			if (oldToNewNodeMap.containsKey(oldTransitionNode)) {
				newPlaceNode.addIncomingTransition((TransitionNode) oldToNewNodeMap.get(oldTransitionNode));
			} else {
				newPlaceNode.addIncomingTransition(cloneTransitionNode(oldTransitionNode, oldToNewNodeMap));
			}
		}
		
		for (TransitionNode oldTransitionNode : oldPlaceNode.getOutgoingTransitions()) {
			if (oldToNewNodeMap.containsKey(oldTransitionNode)) {
				newPlaceNode.addOutgoingTransition((TransitionNode) oldToNewNodeMap.get(oldTransitionNode));
			} else {
				newPlaceNode.addOutgoingTransition(cloneTransitionNode(oldTransitionNode, oldToNewNodeMap));
			}
		}
		return newPlaceNode;
	}

	
	public static void addInitialPlace(TransitionNode transitionNode, int nextNodeId) {
		PlaceNode initialPlace = new PlaceNode(nextNodeId);
		initialPlace.setInitial(true);
		transitionNode.addIncomingPlace(initialPlace);
	}

	public static void addFinalPlace(TransitionNode transitionNode, int nextNodeId) {
		PlaceNode finalPlace = new PlaceNode(nextNodeId);
		finalPlace.setFinal(true);
		transitionNode.addOutgoingPlace(finalPlace);
	}
	
}
