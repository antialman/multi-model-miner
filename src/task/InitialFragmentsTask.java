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


			Map<DiscoveredActivity, ActivityRelations> activityRelationsMap = new HashMap<DiscoveredActivity, ActivityRelations>();

			discoveredActivities.forEach(da -> {activityRelationsMap.put(da, new ActivityRelations(da));});

			for (DiscoveredConstraint discoveredConstraint : constraintSubsets.getResConstraints()) {
				activityRelationsMap.get(discoveredConstraint.getActivationActivity()).addResponseOut(discoveredConstraint.getTargetActivity());
				activityRelationsMap.get(discoveredConstraint.getTargetActivity()).addResponseIn(discoveredConstraint.getActivationActivity());
			}
			for (DiscoveredConstraint discoveredConstraint : constraintSubsets.getPreConstraints()) {
				activityRelationsMap.get(discoveredConstraint.getTargetActivity()).addPrecedenceOut(discoveredConstraint.getActivationActivity());
				activityRelationsMap.get(discoveredConstraint.getActivationActivity()).addPrecedenceIn(discoveredConstraint.getTargetActivity());
			}
			for (DiscoveredConstraint discoveredConstraint : constraintSubsets.getNotcoConstraints()) {
				activityRelationsMap.get(discoveredConstraint.getActivationActivity()).addMutualExclusion(discoveredConstraint.getTargetActivity());
				activityRelationsMap.get(discoveredConstraint.getTargetActivity()).addMutualExclusion(discoveredConstraint.getActivationActivity());
			}



			InitialFragments initialFragments = new InitialFragments();
			initialFragments.setActivityRelationsMap(activityRelationsMap);

			return initialFragments;

		} catch (Exception e) {
			System.err.println("Finding constraint subsets failed: " + e.getMessage());
			System.err.println(e);
			throw e;
		}
	}


}
