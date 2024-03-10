package task.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import data.ActivityRelationsContainer;
import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import task.DeclareDiscoveryResult;
import utils.ConstraintTemplate;
import utils.TransitiveClosureUtils;

public class DeclarePostprocessingTask extends Task<DeclarePostprocessingResult> {
	private DeclareDiscoveryResult declareDiscoveryResult;
	
	public void setDeclareDiscoveryResult(DeclareDiscoveryResult declareDiscoveryResult) {
		this.declareDiscoveryResult = declareDiscoveryResult;
	}
	
	
	
	@Override
	protected DeclarePostprocessingResult call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Declare post-processing started at: " + taskStartTime);
			
			
			DeclarePostprocessingResult declarePostprocessingResult = new DeclarePostprocessingResult();
			declarePostprocessingResult.setAllActivities(declareDiscoveryResult.getActivities());
			declarePostprocessingResult.setAllConstraints(declareDiscoveryResult.getConstraints());
			
			//Pruning
			List<DiscoveredConstraint> prunedConstraints = TransitiveClosureUtils.getPrunedConstraints(declareDiscoveryResult.getConstraints());
			declarePostprocessingResult.setPrunedConstraints(prunedConstraints);
			
			//Cardinalities (done after pruning as pruning does not remove unary constraints)
			List<DiscoveredActivity> reqActivities = new ArrayList<DiscoveredActivity>();
			List<DiscoveredActivity> noRepActivities = new ArrayList<DiscoveredActivity>();
			for (DiscoveredConstraint discoveredConstraint : prunedConstraints) {
				if (discoveredConstraint.getTemplate() == ConstraintTemplate.Existence) {
					reqActivities.add(discoveredConstraint.getActivationActivity());
				}
				if (discoveredConstraint.getTemplate() == ConstraintTemplate.Absence2) {
					noRepActivities.add(discoveredConstraint.getActivationActivity());
				}
			}
			declarePostprocessingResult.setReqActivities(reqActivities);
			declarePostprocessingResult.setNoRepActivities(noRepActivities);
			List<DiscoveredActivity> noCardActivities = new ArrayList<DiscoveredActivity>();
			for (DiscoveredActivity discoveredActivity : declareDiscoveryResult.getActivities()) {
				if (!reqActivities.contains(discoveredActivity) && !noRepActivities.contains(discoveredActivity)) {
					noCardActivities.add(discoveredActivity);
				}
				if (discoveredActivity.getActivityName().equals("_start_")) {
					declarePostprocessingResult.setArtificialStart(discoveredActivity);
				} else if (discoveredActivity.getActivityName().equals("_end_")) {
					declarePostprocessingResult.setArtificialEnd(discoveredActivity);
				}
			}
			declarePostprocessingResult.setNoCardActivities(noCardActivities);
			
			
			//Filtering constraint subsets (Showing all not co-existence constraints since its easier than deriving all of them from other constraints)
			List<DiscoveredConstraint> succPrunedConstraints = prunedConstraints.stream().filter(c -> c.getTemplate() == ConstraintTemplate.Succession).collect(Collectors.toList());
			List<DiscoveredConstraint> precPrunedConstraints = prunedConstraints.stream().filter(c -> c.getTemplate() == ConstraintTemplate.Precedence).collect(Collectors.toList());
			List<DiscoveredConstraint> respPrunedConstraints = prunedConstraints.stream().filter(c -> c.getTemplate() == ConstraintTemplate.Response).collect(Collectors.toList());
			List<DiscoveredConstraint> notcoAllConstraints = declareDiscoveryResult.getConstraints().stream().filter(c -> c.getTemplate() == ConstraintTemplate.Not_CoExistence).collect(Collectors.toList());
			declarePostprocessingResult.setSuccPrunedConstraints(succPrunedConstraints);
			declarePostprocessingResult.setPrecPrunedConstraints(precPrunedConstraints);
			declarePostprocessingResult.setRespPrunedConstraints(respPrunedConstraints);
			declarePostprocessingResult.setNotcoAllConstraints(notcoAllConstraints);
			
			//Activity lists for the subsets (needed for the Declare model visualization code)
			Set<DiscoveredActivity> succActivities = new HashSet<DiscoveredActivity>();
			Set<DiscoveredActivity> precActivities = new HashSet<DiscoveredActivity>();
			Set<DiscoveredActivity> respActivities = new HashSet<DiscoveredActivity>();
			Set<DiscoveredActivity> notcoActivities = new HashSet<DiscoveredActivity>();
			succPrunedConstraints.forEach(c -> {
				succActivities.add(c.getActivationActivity());
				succActivities.add(c.getTargetActivity());}
			);
			precPrunedConstraints.forEach(c -> {
				precActivities.add(c.getActivationActivity());
				precActivities.add(c.getTargetActivity());}
					);
			respPrunedConstraints.forEach(c -> {
				respActivities.add(c.getActivationActivity());
				respActivities.add(c.getTargetActivity());}
			);
			notcoAllConstraints.forEach(c -> {
				notcoActivities.add(c.getActivationActivity());
				notcoActivities.add(c.getTargetActivity());}
			);
			declarePostprocessingResult.setSuccActivities(new ArrayList<>(succActivities));
			declarePostprocessingResult.setPrecActivities(new ArrayList<>(precActivities));
			declarePostprocessingResult.setRespActivities(new ArrayList<>(respActivities));
			declarePostprocessingResult.setNotcoActivities(new ArrayList<>(notcoActivities));
			
			
			//Creating activityToRelationsMap (main input for building Petri nets) 
			Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelationsMap = new HashMap<DiscoveredActivity, ActivityRelationsContainer>(declareDiscoveryResult.getActivities().size());
			declareDiscoveryResult.getActivities().forEach(da -> {activityToRelationsMap.put(da, new ActivityRelationsContainer(da));});
			//Pruned relations
			for (DiscoveredConstraint dc : succPrunedConstraints) {
				activityToRelationsMap.get(dc.getTargetActivity()).addSuccPrunedIn(dc.getActivationActivity(), dc);
				activityToRelationsMap.get(dc.getActivationActivity()).addSuccPrunedOut(dc.getTargetActivity(), dc);
			}
			for (DiscoveredConstraint dc : precPrunedConstraints) {
				activityToRelationsMap.get(dc.getActivationActivity()).addPrecPrunedIn(dc.getTargetActivity(), dc);
				activityToRelationsMap.get(dc.getTargetActivity()).addPrecPrunedOut(dc.getActivationActivity(), dc);
			}
			for (DiscoveredConstraint dc : respPrunedConstraints) {
				activityToRelationsMap.get(dc.getTargetActivity()).addRespPrunedIn(dc.getActivationActivity(), dc);
				activityToRelationsMap.get(dc.getActivationActivity()).addRespPrunedOut(dc.getTargetActivity(), dc);
			}
			//Non-pruned relations
			for (DiscoveredConstraint dc : declareDiscoveryResult.getConstraints()) {
				if (dc.getTemplate() == ConstraintTemplate.CoExistence) {
					activityToRelationsMap.get(dc.getActivationActivity()).addCoex(dc.getTargetActivity(), dc);
					activityToRelationsMap.get(dc.getTargetActivity()).addCoex(dc.getActivationActivity(), dc);
				} else if (dc.getTemplate() == ConstraintTemplate.Not_CoExistence) {
					activityToRelationsMap.get(dc.getActivationActivity()).addNotCoex(dc.getTargetActivity(), dc);
					activityToRelationsMap.get(dc.getTargetActivity()).addNotCoex(dc.getActivationActivity(), dc);
				} else if (dc.getTemplate() == ConstraintTemplate.Not_Succession) {
					activityToRelationsMap.get(dc.getTargetActivity()).addNotSuccAllIn(dc.getActivationActivity(), dc);
					activityToRelationsMap.get(dc.getActivationActivity()).addNotSuccAllOut(dc.getTargetActivity(), dc);
				}
			}
			declarePostprocessingResult.setActivityToRelationsMap(activityToRelationsMap);
			
			System.out.println("Declare post-processing finished at: " + taskStartTime + " - total time: " + (System.currentTimeMillis() - taskStartTime));
			
			return declarePostprocessingResult;
			
			
			
		} catch (Exception e) {
			System.err.println("Declare post-processing failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}
