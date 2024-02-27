package utils;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import model.PlaceNode;
import model.TransitionNode;

public class ModelUtils {
	// private constructor to avoid unnecessary instantiation of the class
    private ModelUtils() {
    }

    
    
  //Merge should probably be either recursive or loop until there are no changes in the fragments, but this will do for now
	public static Set<TransitionNode> copyInitialFragments(Set<TransitionNode> fragmentMainTransitions) {
		Set<TransitionNode> firstMergeMainTransitions = new LinkedHashSet<TransitionNode>();
		
		int nextNodeId = 0;
		for (TransitionNode fragmentMainTransition : fragmentMainTransitions) {
			Map<TransitionNode, TransitionNode> fragmentToNewInMap = new HashMap<TransitionNode, TransitionNode>();
			Map<TransitionNode, TransitionNode> fragmentToNewOutMap = new HashMap<TransitionNode, TransitionNode>();
			TransitionNode newMainTransition = new TransitionNode(nextNodeId++, fragmentMainTransition.getDiscoveredActivity());
			newMainTransition.setFragmentMain(true);
			firstMergeMainTransitions.add(newMainTransition);
			
			//Adding incoming places and transitions as-is
			for (PlaceNode fragmentInPlace : fragmentMainTransition.getIncomingPlaces()) {
				PlaceNode newInPlace = new PlaceNode(nextNodeId++);
				newMainTransition.addIncomingPlace(newInPlace);
				for (TransitionNode fragmentInTransition : fragmentInPlace.getIncomingTransitions()) {
					if (fragmentToNewInMap.containsKey(fragmentInTransition)) {
						newInPlace.addIncomingTransition(fragmentToNewInMap.get(fragmentInTransition));
					} else {
						TransitionNode newInTransition = new TransitionNode(nextNodeId++, fragmentInTransition.getDiscoveredActivity());
						newInPlace.addIncomingTransition(newInTransition);
						fragmentToNewInMap.put(fragmentInTransition, newInTransition);
					}
				}
			}
			
			
			//Adding outgoing places and transitions as-is
			for (PlaceNode fragmentOutPlace : fragmentMainTransition.getOutgoingPlaces()) {
				PlaceNode newOutPlace = new PlaceNode(nextNodeId++);
				newMainTransition.addOutgoingPlace(newOutPlace);
				for (TransitionNode fragmentOutTransition : fragmentOutPlace.getOutgoingTransitions()) {
					if (fragmentToNewOutMap.containsKey(fragmentOutTransition)) {
						newOutPlace.addOutgoingTransition(fragmentToNewOutMap.get(fragmentOutTransition));
					} else {
						TransitionNode newOutTransition = new TransitionNode(nextNodeId++, fragmentOutTransition.getDiscoveredActivity());
						newOutPlace.addOutgoingTransition(newOutTransition);
						fragmentToNewOutMap.put(fragmentOutTransition, newOutTransition);
					}
				}
			}
		}
		
		return firstMergeMainTransitions;
	}
	
	
	
}
