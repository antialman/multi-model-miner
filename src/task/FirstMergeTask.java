package task;

import java.util.HashMap;
import java.util.HashSet;
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
			
			
			//Extending each copy of the initial fragments, only one "extension pass" for now, just to see how it should work
			for (TransitionNode mainTransition : firstMergeMainTransitions) {
				
				//Extension by incoming transitions
				Set<TransitionNode> inTransitions = new HashSet<TransitionNode>();
				mainTransition.getIncomingPlaces().forEach(inP -> {inP.getIncomingTransitions().forEach(inT -> {inTransitions.add(inT);});});
				for (TransitionNode inTransition : inTransitions) {
					TransitionNode mergeFragment = mainActToFragment.get(inTransition.getDiscoveredActivity());
					
					if (!inTransition.isSilent()) {
						for (PlaceNode mergeInP : mergeFragment.getIncomingPlaces()) {
							PlaceNode newMergeInP = new PlaceNode(nextNodeId++);
							newMergeInP.setInitial(mergeInP.isInitial());
							newMergeInP.setFinal(mergeInP.isFinal());
							inTransition.addIncomingPlace(newMergeInP);
							for (TransitionNode mergeInT : mergeInP.getIncomingTransitions()) {
								TransitionNode newMergeInT = new TransitionNode(nextNodeId++, mergeInT.getDiscoveredActivity());
								newMergeInP.addIncomingTransition(newMergeInT);
								if (mergeInT.getIncomingPlaces().size() == 1 && mergeInT.getIncomingPlaces().iterator().next().isInitial()) {
									addInitialPlace(newMergeInT, nextNodeId++);
								}
							}
						}
					}
				}
				
				//Extension by outgoing transitions
				Set<TransitionNode> outTransitions = new HashSet<TransitionNode>();
				mainTransition.getOutgoingPlaces().forEach(outP -> {outP.getOutgoingTransitions().forEach(outT -> {outTransitions.add(outT);});});
				for (TransitionNode outTransition : outTransitions) {
					TransitionNode mergeFragment = mainActToFragment.get(outTransition.getDiscoveredActivity());
					
					if (!outTransition.isSilent()) {
						for (PlaceNode mergeOutP : mergeFragment.getOutgoingPlaces()) {
							PlaceNode newMergeOutP = new PlaceNode(nextNodeId++);
							newMergeOutP.setInitial(mergeOutP.isInitial());
							newMergeOutP.setFinal(mergeOutP.isFinal());
							outTransition.addOutgoingPlace(newMergeOutP);
							for (TransitionNode mergeOutT : mergeOutP.getOutgoingTransitions()) {
								TransitionNode newMergeOutT = new TransitionNode(nextNodeId++, mergeOutT.getDiscoveredActivity());
								newMergeOutP.addOutgoingTransition(newMergeOutT);
								if (mergeOutT.getOutgoingPlaces().size() == 1 && mergeOutT.getOutgoingPlaces().iterator().next().isFinal()) {
									addFinalPlace(newMergeOutT, nextNodeId++);
								}
							}
						}
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
	
	private void addInitialPlace(TransitionNode transitionNode, int nextNodeId) {
		PlaceNode initialPlace = new PlaceNode(nextNodeId);
		initialPlace.setInitial(true);
		transitionNode.addIncomingPlace(initialPlace);
	}

	private void addFinalPlace(TransitionNode transitionNode, int nextNodeId) {
		PlaceNode finalPlace = new PlaceNode(nextNodeId);
		finalPlace.setFinal(true);
		transitionNode.addOutgoingPlace(finalPlace);
	}
}
