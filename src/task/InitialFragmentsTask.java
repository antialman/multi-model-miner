package task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.ActivityRelations;
import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.concurrent.Task;

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
			
			
			
			
			
			
			
			
			

			return initialFragments;

		} catch (Exception e) {
			System.err.println("Finding constraint subsets failed: " + e.getMessage());
			System.err.println(e);
			throw e;
		}
	}


}
