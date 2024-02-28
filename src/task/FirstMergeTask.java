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

				//Extension by incoming transitions
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

				//Extension by outgoing transitions
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


			return mergeFragments;

		} catch (Exception e) {
			System.err.println("First merge failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}
