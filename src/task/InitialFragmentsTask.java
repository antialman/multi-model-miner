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
					PlaceNode place = new PlaceNode(nextNodeId++);
					mainTransition.addOutgoingPlace(place);
					place.addIncomingTransition(mainTransition);
					TransitionNode transition = new TransitionNode(nextNodeId++, resOutAct, true);
					place.addOutgoingTransition(transition);
					transition.addIncomingPlace(place);
				}
				
				//Incoming precedences to the given activity (mirror of outgoing responses)
				for (DiscoveredActivity preInAct : activityRelations.getPrecedenceIn()) {
					PlaceNode place = new PlaceNode(nextNodeId++);
					mainTransition.addIncomingPlace(place);
					place.addOutgoingTransition(mainTransition);
					TransitionNode transition = new TransitionNode(nextNodeId++, preInAct, true);
					place.addIncomingTransition(transition);
					transition.addIncomingPlace(place);
				}
				
				
				
				
				
				////Outgoing precedences from each activity
				//1. Place each activity that is enabled (but not required) by the mainActivity in parallel after the mainActivity
				Map<DiscoveredActivity, PlaceNode> activityPlaceMap = new HashMap<DiscoveredActivity, PlaceNode>();
				Map<DiscoveredActivity, TransitionNode> activityTransitionMap = new HashMap<DiscoveredActivity, TransitionNode>();
				for (DiscoveredActivity preOutAct : activityRelations.getPrecedenceOut()) {
					if (!activityRelations.getResponseOut().contains(preOutAct)) {
						PlaceNode place = new PlaceNode(nextNodeId++);
						mainTransition.addOutgoingPlace(place);
						place.addIncomingTransition(mainTransition);
						TransitionNode transition = new TransitionNode(nextNodeId++, preOutAct, true);
						place.addOutgoingTransition(transition);
						transition.addIncomingPlace(place);
						activityPlaceMap.put(preOutAct, place);
						activityTransitionMap.put(preOutAct, transition);
					}
				}
				//2. Create mutual exclusion arcs based on not coexistence constraints
				for (DiscoveredActivity preOutAct : activityPlaceMap.keySet()) {
					for (DiscoveredActivity notCoActivity : activityRelationsMap.get(preOutAct).getMutualExclusion()) {
						if (activityTransitionMap.containsKey(notCoActivity)) {
							activityPlaceMap.get(preOutAct).addOutgoingTransition(activityTransitionMap.get(notCoActivity));
							activityTransitionMap.get(notCoActivity).addIncomingPlace(activityPlaceMap.get(preOutAct));
						}
					}
				}
				//3. Remove equivalent places (simplifies XOR-splits)
				for (PlaceNode remPlaceCandidate : activityPlaceMap.values()) {
					for (PlaceNode compareToPlace : activityPlaceMap.values()) {
						if (remPlaceCandidate != compareToPlace && remPlaceCandidate.getOutgoingTransitions().equals(compareToPlace.getOutgoingTransitions())) {
							for (TransitionNode remOutTransition : remPlaceCandidate.getOutgoingTransitions()) {
								remOutTransition.remIncomingPlace(remPlaceCandidate);
							}
							remPlaceCandidate.getOutgoingTransitions().clear();
							for (TransitionNode remInTransition : remPlaceCandidate.getIncomingTransitions()) {
								remInTransition.remOutgoingPlace(remPlaceCandidate);
							}
							remPlaceCandidate.getIncomingTransitions().clear();
						}
					}
				}
				//4. Add silent transitions
				for (PlaceNode skipCandidate : activityPlaceMap.values()) {
					if (skipCandidate.getOutgoingTransitions().size() > 1) {
						float inSup = 0;
						float outSup = 0;
						for (TransitionNode inTransition : skipCandidate.getIncomingTransitions()) {
							inSup=inSup+inTransition.getDiscoveredActivity().getActivitySupport();
						}
						for (TransitionNode outTransition : skipCandidate.getOutgoingTransitions()) {
							outSup=outSup+outTransition.getDiscoveredActivity().getActivitySupport();
						}
						if (inSup != outSup) { //TODO: Should compare activity frequencies instead of support
							TransitionNode skipTransition = new TransitionNode(nextNodeId++, null, false);
							skipCandidate.addOutgoingTransition(skipTransition);
							skipTransition.addIncomingPlace(skipCandidate);
						}
					}
					if (skipCandidate.getOutgoingTransitions().size() == 1) {
						TransitionNode skipTransition = new TransitionNode(nextNodeId++, null, false);
						skipCandidate.addOutgoingTransition(skipTransition);
						skipTransition.addIncomingPlace(skipCandidate);
					}
				}
				
				
				
				////Incoming responses to each activity (mirror of outgoing precedences)
				//1. Place each activity that enables (but is not required) by the mainActivity in parallel before the mainActivity
				activityPlaceMap = new HashMap<DiscoveredActivity, PlaceNode>();
				activityTransitionMap = new HashMap<DiscoveredActivity, TransitionNode>();
				for (DiscoveredActivity resInAct : activityRelations.getResponseIn()) {
					if (!activityRelations.getPrecedenceIn().contains(resInAct)) {
						PlaceNode place = new PlaceNode(nextNodeId++);
						mainTransition.addIncomingPlace(place);
						place.addOutgoingTransition(mainTransition);
						TransitionNode transition = new TransitionNode(nextNodeId++, resInAct, true);
						place.addIncomingTransition(transition);
						transition.addOutgoingPlace(place);
						activityPlaceMap.put(resInAct, place);
						activityTransitionMap.put(resInAct, transition);
					}
				}
				//2. Create mutual exclusion arcs based on not coexistence constraints
				for (DiscoveredActivity resInAct : activityPlaceMap.keySet()) {
					for (DiscoveredActivity notCoActivity : activityRelationsMap.get(resInAct).getMutualExclusion()) {
						if (activityTransitionMap.containsKey(notCoActivity)) {
							activityPlaceMap.get(resInAct).addIncomingTransition(activityTransitionMap.get(notCoActivity));
							activityTransitionMap.get(notCoActivity).addOutgoingPlace(activityPlaceMap.get(resInAct));
						}
					}
				}
				//3. Remove equivalent places (simplifies XOR-splits)
				for (PlaceNode remPlaceCandidate : activityPlaceMap.values()) {
					for (PlaceNode compareToPlace : activityPlaceMap.values()) {
						if (remPlaceCandidate != compareToPlace && remPlaceCandidate.getIncomingTransitions().equals(compareToPlace.getIncomingTransitions())) {
							for (TransitionNode remInTransition : remPlaceCandidate.getIncomingTransitions()) {
								remInTransition.remOutgoingPlace(remPlaceCandidate);
							}
							remPlaceCandidate.getIncomingTransitions().clear();
							for (TransitionNode remOutTransition : remPlaceCandidate.getOutgoingTransitions()) {
								remOutTransition.remIncomingPlace(remPlaceCandidate);
							}
							remPlaceCandidate.getOutgoingTransitions().clear();
						}
					}
				}
				//4. Add silent transitions
				for (PlaceNode skipCandidate : activityPlaceMap.values()) {
					if (skipCandidate.getIncomingTransitions().size() > 1) {
						float outSup = 0;
						float inSup = 0;
						for (TransitionNode outTransition : skipCandidate.getOutgoingTransitions()) {
							outSup=outSup+outTransition.getDiscoveredActivity().getActivitySupport();
						}
						for (TransitionNode inTransition : skipCandidate.getIncomingTransitions()) {
							inSup=inSup+inTransition.getDiscoveredActivity().getActivitySupport();
						}
						if (outSup != inSup) { //TODO: Should compare activity frequencies instead of support
							TransitionNode skipTransition = new TransitionNode(nextNodeId++, null, false);
							skipCandidate.addOutgoingTransition(skipTransition);
							skipTransition.addIncomingPlace(skipCandidate);
						}
					}
					if (skipCandidate.getIncomingTransitions().size() == 1) {
						TransitionNode skipTransition = new TransitionNode(nextNodeId++, null, false);
						skipCandidate.addIncomingTransition(skipTransition);
						skipTransition.addOutgoingPlace(skipCandidate);
					}
				}
				
				
				//TODO: Outgoing precedences and incoming responses
				
				
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
