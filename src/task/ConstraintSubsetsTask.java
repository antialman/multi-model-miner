package task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import utils.ConstraintTemplate;
import utils.TransitiveClosureUtils;

public class ConstraintSubsetsTask extends Task<ConstraintSubsets> {

	private DiscoveryTaskResult discoveryTaskResult;
	private boolean pruneSubsets;

	public ConstraintSubsetsTask(DiscoveryTaskResult discoveryTaskResult, boolean pruneSubsets) {
		super();
		this.discoveryTaskResult = discoveryTaskResult;
		this.pruneSubsets = pruneSubsets;
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


			//Filtering constraint subsets
			List<DiscoveredConstraint> sucConstraints = discoveryTaskResult.getConstraints().stream().filter(c -> c.getTemplate() == ConstraintTemplate.Succession).collect(Collectors.toList());
			List<DiscoveredConstraint> resConstraints = discoveryTaskResult.getConstraints().stream().filter(c -> c.getTemplate() == ConstraintTemplate.Response).collect(Collectors.toList());
			List<DiscoveredConstraint> preConstraints = discoveryTaskResult.getConstraints().stream().filter(c -> c.getTemplate() == ConstraintTemplate.Precedence).collect(Collectors.toList());
			List<DiscoveredConstraint> notcoConstraints = discoveryTaskResult.getConstraints().stream().filter(c -> c.getTemplate() == ConstraintTemplate.Not_CoExistence).collect(Collectors.toList());
			
			//Pruning the constraint subsets
			if (pruneSubsets) {
				sucConstraints = TransitiveClosureUtils.getTransitiveClosureSuccessionConstraints(sucConstraints);
				resConstraints = TransitiveClosureUtils.getTransitiveClosureResponseConstraints(resConstraints);
				preConstraints = TransitiveClosureUtils.getTransitiveClosurePrecedenceConstraints(preConstraints);
				//notcoConstraints = TransitiveClosureUtils.getTransitiveClosureNotCoexistenceConstraints(notcoConstraints); //Not Co-Existence is not transitive, so pruning wouldn't make any difference here
			}

			//Activity lists for the subsets (used by the current visualization script)
			Set<DiscoveredActivity> sucActivities = new HashSet<DiscoveredActivity>();
			Set<DiscoveredActivity> preActivities = new HashSet<DiscoveredActivity>();
			Set<DiscoveredActivity> resActivities = new HashSet<DiscoveredActivity>();
			Set<DiscoveredActivity> notcoActivities = new HashSet<DiscoveredActivity>();
			sucConstraints.forEach(c -> {
				sucActivities.add(c.getActivationActivity());
				sucActivities.add(c.getTargetActivity());}
			);
			resConstraints.forEach(c -> {
				resActivities.add(c.getActivationActivity());
				resActivities.add(c.getTargetActivity());}
			);
			preConstraints.forEach(c -> {
				preActivities.add(c.getActivationActivity());
				preActivities.add(c.getTargetActivity());}
					);
			notcoConstraints.forEach(c -> {
				notcoActivities.add(c.getActivationActivity());
				notcoActivities.add(c.getTargetActivity());}
			);
			
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
			e.printStackTrace();
			throw e;
		}
	}
}
