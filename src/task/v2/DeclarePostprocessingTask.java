package task.v2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
			System.out.println("Discovering Declare model started at: " + taskStartTime);
			DeclarePostprocessingResult declarePostprocessingResult = new DeclarePostprocessingResult();
			declarePostprocessingResult.setAllActivities(declareDiscoveryResult.getActivities());
			declarePostprocessingResult.setAllConstraints(declareDiscoveryResult.getConstraints());
			
			
			
			
			
			
			
			
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
			
			
			
			
			
			
			
			
			
			return declarePostprocessingResult;
			
			
			
		} catch (Exception e) {
			System.err.println("Discovering Declare model failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}
