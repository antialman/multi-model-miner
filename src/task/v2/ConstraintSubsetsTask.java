package task.v2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import task.DiscoveryResult;
import utils.ConstraintTemplate;
import utils.TransitiveClosureUtils;

public class ConstraintSubsetsTask extends Task<ConstraintSubsets> {

	private DiscoveryResult discoveryTaskResult;

	public ConstraintSubsetsTask(DiscoveryResult discoveryTaskResult) {
		super();
		this.discoveryTaskResult = discoveryTaskResult;
	}


	@Override
	protected ConstraintSubsets call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Discovering constraint subsets started at: " + taskStartTime);

			ConstraintSubsets constraintSubsets = new ConstraintSubsets();


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

			constraintSubsets.setReqActivities(reqActivities);
			constraintSubsets.setNoRepActivities(noRepActivities);
			constraintSubsets.setNoCardActivities(noCardActivities);



			//Filtering constraint subsets
			List<DiscoveredConstraint> sucConstraints = discoveryTaskResult.getConstraints().stream().filter(c -> c.getTemplate() == ConstraintTemplate.Succession).collect(Collectors.toList());
			List<DiscoveredConstraint> resConstraints = discoveryTaskResult.getConstraints().stream().filter(c -> c.getTemplate() == ConstraintTemplate.Response).collect(Collectors.toList());
			List<DiscoveredConstraint> preConstraints = discoveryTaskResult.getConstraints().stream().filter(c -> c.getTemplate() == ConstraintTemplate.Precedence).collect(Collectors.toList());
			List<DiscoveredConstraint> notcoConstraints = discoveryTaskResult.getConstraints().stream().filter(c -> c.getTemplate() == ConstraintTemplate.Not_CoExistence).collect(Collectors.toList());
			constraintSubsets.setAllSucConstraints(new ArrayList<DiscoveredConstraint>(sucConstraints)); //Creating new lists because existing lists will be modified by pruning
			constraintSubsets.setAllResConstraints(new ArrayList<DiscoveredConstraint>(resConstraints));
			constraintSubsets.setAllPreConstraints(new ArrayList<DiscoveredConstraint>(preConstraints));
			constraintSubsets.setAllNotcoConstraints(new ArrayList<DiscoveredConstraint>(notcoConstraints));



			//Pruning the constraint subsets
			TransitiveClosureUtils.pruneSuccessionConstraints(sucConstraints);
			TransitiveClosureUtils.pruneResponseConstraints(resConstraints);
			TransitiveClosureUtils.prunePrecedenceConstraints(preConstraints);
			//notcoConstraints = TransitiveClosureUtils.getTransitiveClosureNotCoexistenceConstraints(notcoConstraints); //Not Co-Existence is not transitive, so pruning wouldn't make any difference here



			//Lists for pruned constraint subsets and corresponding activities (latter used for visualising the corresponding Declare models) 
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
			constraintSubsets.setPrunedSucActivities(new ArrayList<>(sucActivities));
			constraintSubsets.setPrunedPreActivities(new ArrayList<>(preActivities));
			constraintSubsets.setPrunedResActivities(new ArrayList<>(resActivities));
			constraintSubsets.setPrunedNotcoActivities(new ArrayList<>(notcoActivities));
			constraintSubsets.setPrunedSucConstraints(sucConstraints);
			constraintSubsets.setPrunedPreConstraints(preConstraints);
			constraintSubsets.setPrunedResConstraints(resConstraints);
			constraintSubsets.setPrunedNotcoConstraints(notcoConstraints);

			return constraintSubsets;

		} catch (Exception e) {
			System.err.println("Finding constraint subsets failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}
