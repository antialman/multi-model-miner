package task.v1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;
import javafx.concurrent.Task;
import model.v1.PlaceNode;
import model.v1.TransitionNode;
import utils.ModelUtils;

public class MergeStep1Task extends Task<MergeStep1Result> {

	private InitialFragmentsResult initialFragmentsResult;

	public MergeStep1Task(InitialFragmentsResult initialFragmentsResult) {
		this.initialFragmentsResult = initialFragmentsResult;
	}


	@Override
	protected MergeStep1Result call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Step 1 of merge started at: " + taskStartTime);

			MergeStep1Result mergeStep1Result = new MergeStep1Result();


			//Cloning each initial fragment
			Map<DiscoveredActivity, TransitionNode> initialFragmentsMap = new HashMap<DiscoveredActivity, TransitionNode>();
			for (TransitionNode initialFragment : initialFragmentsResult.getFragmentMainTransitions()) {
				mergeStep1Result.addStep1MainTransition(ModelUtils.cloneFragment(initialFragment)); //Each clone will be modified during merge
				initialFragmentsMap.put(initialFragment.getDiscoveredActivity(), initialFragment); //For looking up initial fragments based on the main activity of the fragment
			}

			//For setting the identifiers of each new node created during a merge
			int nextNodeId = initialFragmentsResult.getNodeCount();

			//Extending each copy of the initial fragments, only one "extension pass" for now, just to see how it should work
			for (TransitionNode mergeFragment : mergeStep1Result.getStep1MainTransitions()) {

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

			mergeStep1Result.setNodeCount(nextNodeId);
			
			return mergeStep1Result;

		} catch (Exception e) {
			System.err.println("Step 1 of merge failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}
