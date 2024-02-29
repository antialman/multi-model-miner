package task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;
import javafx.concurrent.Task;
import model.PlaceNode;
import model.TransitionNode;
import utils.ModelUtils;

public class FirstMergeTask extends Task<Set<TransitionNode>> {

	private InitialFragmentsResult initialFragmentsResult;

	public FirstMergeTask(InitialFragmentsResult initialFragmentsResult) {
		this.initialFragmentsResult = initialFragmentsResult;
	}


	@Override
	protected Set<TransitionNode> call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("First merge started at: " + taskStartTime);

			//Cloning each initial fragment
			Set<TransitionNode> mergeFragments = new LinkedHashSet<TransitionNode>();
			Map<DiscoveredActivity, TransitionNode> initialFragmentsMap = new HashMap<DiscoveredActivity, TransitionNode>();
			for (TransitionNode initialFragment : initialFragmentsResult.getFragmentMainTransitions()) {
				mergeFragments.add(ModelUtils.cloneFragment(initialFragment)); //Each clone will be modified during merge
				initialFragmentsMap.put(initialFragment.getDiscoveredActivity(), initialFragment); //For looking up initial fragments based on the main activity of the fragment
			}

			//For setting the identifiers of each new node created during a merge
			int nextNodeId = initialFragmentsResult.getNodeCount();

			//Extending each copy of the initial fragments, only one "extension pass" for now, just to see how it should work
			for (TransitionNode mergeFragment : mergeFragments) {

				//Initial extension by incoming transitions
				Set<TransitionNode> mergeInTransitions = new HashSet<TransitionNode>();
				mergeFragment.getIncomingPlaces().forEach(inP -> {inP.getIncomingTransitions().forEach(inT -> {mergeInTransitions.add(inT);});});
				for (TransitionNode mergeInTransition : mergeInTransitions) {
					if (!mergeInTransition.isSilent() && mergeInTransition.getIncomingPlaces().isEmpty()) {
						TransitionNode initialFragment = initialFragmentsMap.get(mergeInTransition.getDiscoveredActivity());
						for (PlaceNode initialInP : initialFragment.getIncomingPlaces()) {
							PlaceNode newInP = new PlaceNode(nextNodeId++);
							newInP.setInitial(initialInP.isInitial());
							newInP.setFinal(initialInP.isFinal());
							mergeInTransition.addIncomingPlace(newInP);
							
							for (TransitionNode initialInT : initialInP.getIncomingTransitions()) {
								TransitionNode newInT = new TransitionNode(nextNodeId++, initialInT.getDiscoveredActivity());
								newInP.addIncomingTransition(newInT);
								if (initialInT.getIncomingPlaces().size() == 1 && initialInT.getIncomingPlaces().iterator().next().isInitial()) {
									ModelUtils.addInitialPlace(newInT, nextNodeId++);
								}
							}
						}
					}
				}

				//Initial extension by outgoing transitions
				Set<TransitionNode> mergeOutTransitions = new HashSet<TransitionNode>();
				mergeFragment.getOutgoingPlaces().forEach(outP -> {outP.getOutgoingTransitions().forEach(outT -> {mergeOutTransitions.add(outT);});});
				for (TransitionNode mergeOutTransition : mergeOutTransitions) {
					if (!mergeOutTransition.isSilent() && mergeOutTransition.getOutgoingPlaces().isEmpty()) {
						TransitionNode initialFragment = initialFragmentsMap.get(mergeOutTransition.getDiscoveredActivity());
						for (PlaceNode initialOutP : initialFragment.getOutgoingPlaces()) {
							PlaceNode newOutP = new PlaceNode(nextNodeId++);
							newOutP.setInitial(initialOutP.isInitial());
							newOutP.setFinal(initialOutP.isFinal());
							mergeOutTransition.addOutgoingPlace(newOutP);
							
							for (TransitionNode initialOutT : initialOutP.getOutgoingTransitions()) {
								TransitionNode newOutT = new TransitionNode(nextNodeId++, initialOutT.getDiscoveredActivity());
								newOutP.addOutgoingTransition(newOutT);
								if (initialOutT.getOutgoingPlaces().size() == 1 && initialOutT.getOutgoingPlaces().iterator().next().isFinal()) {
									ModelUtils.addFinalPlace(newOutT, nextNodeId++);
								}
							}
						}
					}
				}
			}
			
			
			//Processing duplicate activities created by merge
			for (TransitionNode mergeFragment : mergeFragments) {
				Map<DiscoveredActivity, Set<TransitionNode>> actTransitionsMap = new HashMap<DiscoveredActivity, Set<TransitionNode>>();
				Set<Integer> visited = new HashSet<Integer>();
				addToActTransitionsMap(mergeFragment, actTransitionsMap, visited);
				Map<DiscoveredActivity, TransitionNode> mergeTransitions = new HashMap<DiscoveredActivity, TransitionNode>();
				
				//Creating merge transitions to replace all transitions that were duplicated during the merge
				for (DiscoveredActivity discoveredActivity : actTransitionsMap.keySet()) {
					if (actTransitionsMap.get(discoveredActivity).size()>1) {
						mergeTransitions.put(discoveredActivity, new TransitionNode(nextNodeId++, discoveredActivity));
					}
				}
				
				//Replacing all duplicated transitions with a single merge transition
				for (DiscoveredActivity mergeActivity : mergeTransitions.keySet()) {
					TransitionNode initialFragment = initialFragmentsMap.get(mergeActivity);
					
					for (PlaceNode initialOutPlace : initialFragment.getOutgoingPlaces()) {
						PlaceNode mergeP = new PlaceNode(nextNodeId++);
						mergeTransitions.get(mergeActivity).addOutgoingPlace(mergeP);
						for (TransitionNode initialOutTransition : initialOutPlace.getOutgoingTransitions()) {
							if (!initialOutTransition.isSilent()) {	
								DiscoveredActivity initialOutActivity = initialOutTransition.getDiscoveredActivity();
								if (mergeTransitions.containsKey(initialOutActivity)) {
									mergeP.addOutgoingTransition(mergeTransitions.get(initialOutActivity));
								} else if (actTransitionsMap.containsKey(initialOutTransition.getDiscoveredActivity())) {
									//If the set corresponding to mergeActivity in actTransitionsMap has more than 1 element, then it is covered by the previous if block
									mergeP.addOutgoingTransition(actTransitionsMap.get(initialOutActivity).iterator().next());
								}
							}
						}
						if (mergeP.getOutgoingTransitions().isEmpty()) {
							mergeTransitions.get(mergeActivity).remOutgoingPlace(mergeP);
						}
					}
					
					for (PlaceNode initialInPlace : initialFragment.getIncomingPlaces()) {
						PlaceNode mergeP = new PlaceNode(nextNodeId++);
						mergeTransitions.get(mergeActivity).addIncomingPlace(mergeP);
						for (TransitionNode initialInTransition : initialInPlace.getIncomingTransitions()) {
							if (!initialInTransition.isSilent()) {
								DiscoveredActivity initialInActivity = initialInTransition.getDiscoveredActivity();
								if (mergeTransitions.containsKey(initialInActivity)) {
									mergeP.addIncomingTransition(mergeTransitions.get(initialInActivity));
								} else if (actTransitionsMap.containsKey(initialInActivity)) {
									//If the set corresponding to mergeActivity in actTransitionsMap has more than 1 element, then it is covered by the previous if block
									mergeP.addIncomingTransition(actTransitionsMap.get(initialInActivity).iterator().next());
								}
							}
						}
						if (mergeP.getIncomingTransitions().isEmpty()) {
							mergeTransitions.get(mergeActivity).remIncomingPlace(mergeP);
						}
					}
					
					for (TransitionNode duplicateTransition : actTransitionsMap.get(mergeActivity)) {
						Set<PlaceNode> dupInPlaces = new HashSet<PlaceNode>();
						Set<PlaceNode> dupOutPlaces = new HashSet<PlaceNode>();
						duplicateTransition.getIncomingPlaces().forEach(dupInP -> {dupInPlaces.add(dupInP);});
						duplicateTransition.getOutgoingPlaces().forEach(dupOutP -> {dupOutPlaces.add(dupOutP);});
						
						dupInPlaces.forEach(dupInP -> {dupInP.clearAllTransitions();});
						dupOutPlaces.forEach(dupOutP -> {dupOutP.clearAllTransitions();});
					}
					
				}
			}


			return mergeFragments;

		} catch (Exception e) {
			System.err.println("First merge failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	private void addToActTransitionsMap(TransitionNode mergeFragment, Map<DiscoveredActivity, Set<TransitionNode>> actTransitionsMap, Set<Integer> visited) {
		if (!actTransitionsMap.containsKey(mergeFragment.getDiscoveredActivity())) {
			actTransitionsMap.put(mergeFragment.getDiscoveredActivity(), new HashSet<TransitionNode>());
		}
		actTransitionsMap.get(mergeFragment.getDiscoveredActivity()).add(mergeFragment);
		visited.add(mergeFragment.getNodeId());
		
		mergeFragment.getIncomingPlaces().forEach(inP -> {inP.getIncomingTransitions().forEach(inT -> {
			if (!visited.contains(inT.getNodeId()) && !inT.isSilent()) {
				addToActTransitionsMap(inT, actTransitionsMap, visited);
			}
		});});
		
		mergeFragment.getOutgoingPlaces().forEach(outP -> {outP.getOutgoingTransitions().forEach(outT -> {
			if (!visited.contains(outT.getNodeId()) && !outT.isSilent()) {
				addToActTransitionsMap(outT, actTransitionsMap, visited);
			}
		});});
	}
}
