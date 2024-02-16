package task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import data.ActivityRelations;
import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import model.PlaceNode;
import model.TransitionNode;

public class InitialFragmentsTask extends Task<InitialFragments> {

	private List<DiscoveredActivity> discoveredActivities;
	private ConstraintSubsets constraintSubsets;

	public InitialFragmentsTask(List<DiscoveredActivity> discoveredActivities, ConstraintSubsets constraintSubsets) {
		super();
		this.discoveredActivities = discoveredActivities;
		this.constraintSubsets = constraintSubsets;
	}


	@Override
	protected InitialFragments call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Discovering constraint subsets started at: " + taskStartTime);

			InitialFragments initialFragments = new InitialFragments();
			
			//Making it easier to look up which types of relations each activity has to other activities
			Map<DiscoveredActivity, ActivityRelations> activityRelationsMap = new HashMap<DiscoveredActivity, ActivityRelations>();
			discoveredActivities.forEach(da -> {activityRelationsMap.put(da, new ActivityRelations(da));});
			for (DiscoveredConstraint dc : constraintSubsets.getResConstraints()) {
				activityRelationsMap.get(dc.getActivationActivity()).addResponseOut(dc.getTargetActivity());
				activityRelationsMap.get(dc.getTargetActivity()).addResponseIn(dc.getActivationActivity());
			}
			for (DiscoveredConstraint dc : constraintSubsets.getPreConstraints()) {
				activityRelationsMap.get(dc.getTargetActivity()).addPrecedenceOut(dc.getActivationActivity());
				activityRelationsMap.get(dc.getActivationActivity()).addPrecedenceIn(dc.getTargetActivity());
			}
			for (DiscoveredConstraint dc : constraintSubsets.getNotcoConstraints()) {
				activityRelationsMap.get(dc.getActivationActivity()).addMutualExclusion(dc.getTargetActivity());
				activityRelationsMap.get(dc.getTargetActivity()).addMutualExclusion(dc.getActivationActivity());
			}
			initialFragments.setActivityRelationsMap(activityRelationsMap);
			
			
			//Creating the initial (activity based) fragments
			int nextNodeId = 0;
			List<TransitionNode> fragmentMainTransitions = new ArrayList<TransitionNode>();
			for (ActivityRelations activityRelations : activityRelationsMap.values()) {
				DiscoveredActivity mainActivity = activityRelations.getActivity();
				TransitionNode mainTransition = new TransitionNode(nextNodeId++, mainActivity, true);
				fragmentMainTransitions.add(mainTransition);
				
				//Outgoing responses from the given activity
				for (DiscoveredActivity resOutAct : activityRelations.getResponseOut()) {
					PlaceNode resOutPlace = new PlaceNode(nextNodeId++);
					TransitionNode resOutTransition = new TransitionNode(nextNodeId++, resOutAct, true);
					mainTransition.addOutgoingPlace(resOutPlace);
					resOutPlace.addOutgoingTransition(resOutTransition);
				}
				
				//Incoming precedences to the given activity (mirror of outgoing responses)
				for (DiscoveredActivity preInAct : activityRelations.getPrecedenceIn()) {
					PlaceNode preInPlace = new PlaceNode(nextNodeId++);
					TransitionNode preInTransition = new TransitionNode(nextNodeId++, preInAct, true);
					mainTransition.addIncomingPlace(preInPlace);
					preInPlace.addIncomingTransition(preInTransition);
				}
				
				
				
				
				
				////Outgoing precedences from each activity
				//1. Place each activity that is enabled (but not required) by the mainActivity in parallel after the mainActivity
				Map<DiscoveredActivity, PlaceNode> activityPlaceMap = new HashMap<DiscoveredActivity, PlaceNode>();
				Map<DiscoveredActivity, TransitionNode> activityTransitionMap = new HashMap<DiscoveredActivity, TransitionNode>();
				for (DiscoveredActivity preOutAct : activityRelations.getPrecedenceOut()) {
					if (!activityRelations.getResponseOut().contains(preOutAct)) {
						PlaceNode preOutPlace = new PlaceNode(nextNodeId++);
						TransitionNode preOutTransition = new TransitionNode(nextNodeId++, preOutAct, true);
						mainTransition.addOutgoingPlace(preOutPlace);
						preOutPlace.addOutgoingTransition(preOutTransition);
						activityPlaceMap.put(preOutAct, preOutPlace);
						activityTransitionMap.put(preOutAct, preOutTransition);
					}
				}
				//2. Create mutual exclusion arcs based on not coexistence constraints
				for (DiscoveredActivity preOutAct : activityPlaceMap.keySet()) {
					for (DiscoveredActivity notCoActivity : activityRelationsMap.get(preOutAct).getMutualExclusion()) {
						if (activityTransitionMap.containsKey(notCoActivity)) {
							activityPlaceMap.get(preOutAct).addOutgoingTransition(activityTransitionMap.get(notCoActivity));
						}
					}
				}
				//3. Remove equivalent places (simplifies XOR-splits)
				for (PlaceNode remPlaceCandidate : activityPlaceMap.values()) {
					for (PlaceNode compareToPlace : activityPlaceMap.values()) {
						if (remPlaceCandidate != compareToPlace && remPlaceCandidate.getOutgoingTransitions().equals(compareToPlace.getOutgoingTransitions())) {
							remPlaceCandidate.clearAllTransitions();
						}
					}
				}
				//4. Add silent transitions
				for (PlaceNode skipCandidatePlace : activityPlaceMap.values()) {
					if (skipCandidatePlace.getOutgoingTransitions().size() > 1) {
						float inSup = 0;
						float outSup = 0;
						for (TransitionNode inTransition : skipCandidatePlace.getIncomingTransitions()) {
							inSup=inSup+inTransition.getDiscoveredActivity().getActivitySupport();
						}
						for (TransitionNode outTransition : skipCandidatePlace.getOutgoingTransitions()) {
							outSup=outSup+outTransition.getDiscoveredActivity().getActivitySupport();
						}
						if (inSup != outSup) { //TODO: Should compare activity frequencies instead of support
							TransitionNode skipTransition = new TransitionNode(nextNodeId++, null, false);
							skipCandidatePlace.addOutgoingTransition(skipTransition);
						}
					}
					if (skipCandidatePlace.getOutgoingTransitions().size() == 1) {
						TransitionNode skipTransition = new TransitionNode(nextNodeId++, null, false);
						skipCandidatePlace.addOutgoingTransition(skipTransition);
					}
				}
				
				
				
				////Incoming responses to each activity (mirror of outgoing precedences)
				//1. Place each activity that enables (but is not required) by the mainActivity in parallel before the mainActivity
				activityPlaceMap = new HashMap<DiscoveredActivity, PlaceNode>();
				activityTransitionMap = new HashMap<DiscoveredActivity, TransitionNode>();
				for (DiscoveredActivity resInAct : activityRelations.getResponseIn()) {
					if (!activityRelations.getPrecedenceIn().contains(resInAct)) {
						PlaceNode resInPlace = new PlaceNode(nextNodeId++);
						TransitionNode resInTransition = new TransitionNode(nextNodeId++, resInAct, true);
						mainTransition.addIncomingPlace(resInPlace);
						resInPlace.addIncomingTransition(resInTransition);
						activityPlaceMap.put(resInAct, resInPlace);
						activityTransitionMap.put(resInAct, resInTransition);
					}
				}
				//2. Create mutual exclusion arcs based on not coexistence constraints
				for (DiscoveredActivity resInAct : activityPlaceMap.keySet()) {
					for (DiscoveredActivity notCoActivity : activityRelationsMap.get(resInAct).getMutualExclusion()) {
						if (activityTransitionMap.containsKey(notCoActivity)) {
							activityPlaceMap.get(resInAct).addIncomingTransition(activityTransitionMap.get(notCoActivity));
						}
					}
				}
				//3. Remove equivalent places (simplifies XOR-splits)
				for (PlaceNode remPlaceCandidate : activityPlaceMap.values()) {
					for (PlaceNode compareToPlace : activityPlaceMap.values()) {
						if (remPlaceCandidate != compareToPlace && remPlaceCandidate.getIncomingTransitions().equals(compareToPlace.getIncomingTransitions())) {
							remPlaceCandidate.clearAllTransitions();
						}
					}
				}
				//4. Add silent transitions
				for (PlaceNode skipCandidatePlace : activityPlaceMap.values()) {
					if (skipCandidatePlace.getIncomingTransitions().size() > 1) {
						float outSup = 0;
						float inSup = 0;
						for (TransitionNode outTransition : skipCandidatePlace.getOutgoingTransitions()) {
							outSup=outSup+outTransition.getDiscoveredActivity().getActivitySupport();
						}
						for (TransitionNode inTransition : skipCandidatePlace.getIncomingTransitions()) {
							inSup=inSup+inTransition.getDiscoveredActivity().getActivitySupport();
						}
						if (outSup != inSup) { //TODO: Should compare activity frequencies instead of support
							TransitionNode skipTransition = new TransitionNode(nextNodeId++, null, false);
							skipCandidatePlace.addIncomingTransition(skipTransition);
						}
					}
					if (skipCandidatePlace.getIncomingTransitions().size() == 1) {
						TransitionNode skipTransition = new TransitionNode(nextNodeId++, null, false);
						skipCandidatePlace.addIncomingTransition(skipTransition);
					}
				}
				
				initialFragments.setFragmentMainTransitions(fragmentMainTransitions);
			}
			
			

			return initialFragments;

		} catch (Exception e) {
			System.err.println("Finding constraint subsets failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}


}
