package task.v1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;
import javafx.concurrent.Task;
import model.PlaceNode;
import model.TransitionNode;
import utils.ModelUtils;

public class MergeStep2Task extends Task<Set<TransitionNode>> {
	
	private InitialFragmentsResult initialFragmentsResult;
	private MergeStep1Result mergeStep1Result;

	public MergeStep2Task(InitialFragmentsResult initialFragmentsResult, MergeStep1Result mergeStep1Result) {
		this.initialFragmentsResult = initialFragmentsResult;
		this.mergeStep1Result = mergeStep1Result;
	}
	
	@Override
	protected Set<TransitionNode> call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Step 2 of merge started at: " + taskStartTime);

			//Cloning each initial fragment
			Set<TransitionNode> mergeFragments = new LinkedHashSet<TransitionNode>();
			Map<DiscoveredActivity, TransitionNode> initialFragmentsMap = new HashMap<DiscoveredActivity, TransitionNode>();
			for (TransitionNode initialFragment : mergeStep1Result.getStep1MainTransitions()) {
				mergeFragments.add(ModelUtils.cloneFragment(initialFragment)); //Each clone will be modified during merge
			}
			for (TransitionNode fragmentMainT : initialFragmentsResult.getFragmentMainTransitions()) {
				initialFragmentsMap.put(fragmentMainT.getDiscoveredActivity(), fragmentMainT); //For looking up initial fragments based on the main activity of the fragment
			}

			//For setting the identifiers of each new node created during a merge
			int nextNodeId = mergeStep1Result.getNodeCount();
			
			
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
				
				Set<PlaceNode> newPlaces = new HashSet<PlaceNode>();
				
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
								} else {
									//Place has an outgoing transition that can't be represented without adding additional transitions (more complex XOR relations)
									mergeP.clearAllTransitions();
									break;
								}
							}
						}
						if (mergeP.getOutgoingTransitions().isEmpty()) {
							mergeTransitions.get(mergeActivity).remOutgoingPlace(mergeP);
						} else {
							newPlaces.add(mergeP);
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
								} else {
									//Place has an incoming transition that can't be represented without adding additional transitions (more complex XOR relations)
									mergeP.clearAllTransitions();
									break;
								}
							}
						}
						if (mergeP.getIncomingTransitions().isEmpty()) {
							mergeTransitions.get(mergeActivity).remIncomingPlace(mergeP);
						} else {
							newPlaces.add(mergeP);
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
					
					
					Set<PlaceNode> uniquePlaces = new HashSet<PlaceNode>();
					for (PlaceNode newPlace : newPlaces) {
						Set<TransitionNode> inTransitions = new HashSet<TransitionNode>();
						Set<TransitionNode> outTransitions = new HashSet<TransitionNode>();
						newPlace.getIncomingTransitions().forEach(inT -> {inTransitions.add(inT);});
						newPlace.getOutgoingTransitions().forEach(outT -> {outTransitions.add(outT);});
						
						boolean isUnique=true;
						for (PlaceNode uniquePlace : uniquePlaces) {
							Set<TransitionNode> inTransitions2 = new HashSet<TransitionNode>();
							Set<TransitionNode> outTransitions2 = new HashSet<TransitionNode>();
							uniquePlace.getIncomingTransitions().forEach(inT -> {inTransitions2.add(inT);});
							uniquePlace.getOutgoingTransitions().forEach(outT -> {outTransitions2.add(outT);});
							
							if (inTransitions.equals(inTransitions2) && outTransitions.equals(outTransitions2)) {
								isUnique=false;
							}
						}
						if (isUnique) {
							uniquePlaces.add(newPlace);
						} else {
							newPlace.clearAllTransitions();
						}
					}
				}
			}
			
			
			return mergeFragments;
			
		} catch (Exception e) {
			System.err.println("Step 2 of merge failed: " + e.getMessage());
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
