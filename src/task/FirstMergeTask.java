package task;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;
import javafx.concurrent.Task;
import model.PlaceNode;
import model.TransitionNode;

public class FirstMergeTask extends Task<Set<TransitionNode>> {
	
	private Set<TransitionNode> fragmentMainTransitions;

	public FirstMergeTask(Set<TransitionNode> fragmentMainTransitions) {
		this.fragmentMainTransitions = fragmentMainTransitions;
	}
	
	@Override
	protected Set<TransitionNode> call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Discovering constraint subsets started at: " + taskStartTime);
			
			//It should be ok to modify the input fragments directly, but creating new ones for now just to be safe and in case I need the input fragments
			//Merge should probably be either recursive or loop until there are no changes in the fragments, but this will do for now
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
					newInPlace.setInitial(fragmentInPlace.isInitial());
					newInPlace.setFinal(fragmentInPlace.isFinal());
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
					newOutPlace.setInitial(fragmentOutPlace.isInitial());
					newOutPlace.setFinal(fragmentOutPlace.isFinal());
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
			
			
			//Map for looking up fragments based on their main activity
			Map<DiscoveredActivity, TransitionNode> mainActToFragment = new HashMap<DiscoveredActivity, TransitionNode>();
			for (TransitionNode mainTransition : firstMergeMainTransitions) {
				mainActToFragment.put(mainTransition.getDiscoveredActivity(), mainTransition);
			}
			
			
			//TODO: Extending each copy of the initial fragments, only one "extension pass" for now, just to see how it should work
			
			
			
			
			return firstMergeMainTransitions;
			
		} catch (Exception e) {
			System.err.println("Finding constraint subsets failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}
