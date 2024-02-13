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
			
			
			//Creating the initial fragments
			int nextNodeId = 0;
			List<TransitionNode> fragmentMainTransitions = new ArrayList<TransitionNode>();
			for (ActivityRelations activityRelations : activityRelationsMap.values()) {
				DiscoveredActivity mainActivity = activityRelations.getActivity();
				TransitionNode mainTransition = new TransitionNode(nextNodeId++, mainActivity, true);
				fragmentMainTransitions.add(mainTransition);
				
				for (DiscoveredActivity resOutAct : activityRelations.getResponseOut()) {
					PlaceNode place = new PlaceNode(nextNodeId++);
					mainTransition.addOutgoingPlace(place);
					TransitionNode transition = new TransitionNode(nextNodeId++, resOutAct, true);
					place.addOutgoingTransition(transition);
				}
				
				
				for (DiscoveredActivity preInAct : activityRelations.getPrecedenceIn()) {
					PlaceNode place = new PlaceNode(nextNodeId++);
					mainTransition.addIncomingPlace(place);
					TransitionNode transition = new TransitionNode(nextNodeId++, preInAct, true);
					place.addIncomingTransition(transition);
				}
				
				
				//TODO: Outgoing precedences and incoming responses
				
				
				initialFragments.setFragmentMainTransitions(fragmentMainTransitions);
			}
			
			

			return initialFragments;

		} catch (Exception e) {
			System.err.println("Finding constraint subsets failed: " + e.getMessage());
			System.err.println(e);
			throw e;
		}
	}


}
