package task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.concurrent.Task;
import model.PlaceNode;
import model.TransitionNode;

public class FirstMergeTask extends Task<List<TransitionNode>> {
	
	private List<TransitionNode> fragmentMainTransitions;

	public FirstMergeTask(List<TransitionNode> fragmentMainTransitions) {
		this.fragmentMainTransitions = fragmentMainTransitions;
	}
	
	@Override
	protected List<TransitionNode> call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Discovering constraint subsets started at: " + taskStartTime);
			
			//It should be ok to modify the input fragments directly, but creating new ones for now just to be safe and in case I need the input fragments
			//Merge should probably be either recursive or loop until there are no changes in the fragments
			//Implementing only a single pass for now just to see how it would work
			
			
			List<TransitionNode> firstMergeMainTransitions = new ArrayList<TransitionNode>();
			
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
						
						//TODO: For each transition, find the corresponding fragment and add it's incoming places and transitions
						
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
						
						
						
						//TODO: For each transition, find the corresponding fragment and add it's outgoing places and transitions
						
					}
				}
			}
			
			
			
			return firstMergeMainTransitions;
			
		} catch (Exception e) {
			System.err.println("Finding constraint subsets failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}
