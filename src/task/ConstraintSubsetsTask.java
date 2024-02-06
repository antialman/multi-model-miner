package task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import utils.ConstraintTemplate;

public class ConstraintSubsetsTask extends Task<ConstraintSubsets> {

	private DiscoveryTaskResult discoveryTaskResult;

	public ConstraintSubsetsTask(DiscoveryTaskResult discoveryTaskResult) {
		super();
		this.discoveryTaskResult = discoveryTaskResult;
	}

	public void setDiscoveryTaskResult(DiscoveryTaskResult discoveryTaskResult) {
		this.discoveryTaskResult = discoveryTaskResult;
	}


	@Override
	protected ConstraintSubsets call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Discovering constraint subsets started at: " + taskStartTime);

			//Cardinalities
			List<DiscoveredActivity> reqActivities = new ArrayList<DiscoveredActivity>();
			List<DiscoveredActivity> noRepActivities = new ArrayList<DiscoveredActivity>();
			for (DiscoveredConstraint discoveredConstraint : discoveryTaskResult.getConstraints()) {
				if (discoveredConstraint.getTemplate() == ConstraintTemplate.Existence) {
					reqActivities.add(discoveredConstraint.getActivationActivity());
				}
				if (discoveredConstraint.getTemplate() == ConstraintTemplate.Absence2) {
					noRepActivities.add(discoveredConstraint.getActivationActivity());
				}
			}

			List<DiscoveredActivity> noCardActivities = new ArrayList<DiscoveredActivity>();
			for (DiscoveredActivity discoveredActivity : discoveryTaskResult.getActivities()) {
				if (!reqActivities.contains(discoveredActivity) && !noRepActivities.contains(discoveredActivity)) {
					noCardActivities.add(discoveredActivity);
				}
			}


			//Successions, Precedences, Responses
			Set<DiscoveredActivity> sucActivities = new HashSet<DiscoveredActivity>();
			Set<DiscoveredActivity> preActivities = new HashSet<DiscoveredActivity>();
			Set<DiscoveredActivity> resActivities = new HashSet<DiscoveredActivity>();
			Set<DiscoveredActivity> notcoActivities = new HashSet<DiscoveredActivity>();
			List<DiscoveredConstraint> sucConstraints = new ArrayList<DiscoveredConstraint>();
			List<DiscoveredConstraint> preConstraints = new ArrayList<DiscoveredConstraint>();
			List<DiscoveredConstraint> resConstraints = new ArrayList<DiscoveredConstraint>();
			List<DiscoveredConstraint> notcoConstraints = new ArrayList<DiscoveredConstraint>();
			for (DiscoveredConstraint discoveredConstraint : discoveryTaskResult.getConstraints()) {
				if (discoveredConstraint.getTemplate() == ConstraintTemplate.Succession) {
					sucActivities.add(discoveredConstraint.getActivationActivity());
					sucActivities.add(discoveredConstraint.getTargetActivity());
					sucConstraints.add(discoveredConstraint);
				} else if (discoveredConstraint.getTemplate() == ConstraintTemplate.Precedence) {
					preActivities.add(discoveredConstraint.getActivationActivity());
					preActivities.add(discoveredConstraint.getTargetActivity());
					preConstraints.add(discoveredConstraint);
				} else if (discoveredConstraint.getTemplate() == ConstraintTemplate.Response) {
					resActivities.add(discoveredConstraint.getActivationActivity());
					resActivities.add(discoveredConstraint.getTargetActivity());
					resConstraints.add(discoveredConstraint);
				} else if (discoveredConstraint.getTemplate() == ConstraintTemplate.Not_CoExistence) {
					notcoActivities.add(discoveredConstraint.getActivationActivity());
					notcoActivities.add(discoveredConstraint.getTargetActivity());
					notcoConstraints.add(discoveredConstraint);
				}
			}

			
			//Result object
			ConstraintSubsets constraintSubsets = new ConstraintSubsets();
			constraintSubsets.setReqActivities(reqActivities);
			constraintSubsets.setNoRepActivities(noRepActivities);
			constraintSubsets.setNoCardActivities(noCardActivities);
			constraintSubsets.setSucActivities(new ArrayList<>(sucActivities));
			constraintSubsets.setPreActivities(new ArrayList<>(preActivities));
			constraintSubsets.setResActivities(new ArrayList<>(resActivities));
			constraintSubsets.setNotcoActivities(new ArrayList<>(notcoActivities));
			constraintSubsets.setSucConstraints(sucConstraints);
			constraintSubsets.setPreConstraints(preConstraints);
			constraintSubsets.setResConstraints(resConstraints);
			constraintSubsets.setNotcoConstraints(notcoConstraints);
			
			return constraintSubsets;

		} catch (Exception e) {
			System.err.println("Finding constraint subsets failed: " + e.getMessage());
			System.err.println(e);
			throw e;
		}
	}
}
