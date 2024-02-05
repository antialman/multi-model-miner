package task;

import java.util.ArrayList;
import java.util.List;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import utils.ConstraintTemplate;

public class InitialRelationsTask extends Task<InitialRelations> {
	
	private DiscoveryTaskResult discoveryTaskResult;
	
	public InitialRelationsTask(DiscoveryTaskResult discoveryTaskResult) {
		super();
		this.discoveryTaskResult = discoveryTaskResult;
	}
	
	public void setDiscoveryTaskResult(DiscoveryTaskResult discoveryTaskResult) {
		this.discoveryTaskResult = discoveryTaskResult;
	}
	

	@Override
	protected InitialRelations call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Discovering initial relations started at: " + taskStartTime);
			
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
			
			InitialRelations initialRelations = new InitialRelations();
			initialRelations.setReqActivities(reqActivities);
			initialRelations.setNoRepActivities(noRepActivities);
			initialRelations.setNoCardActivities(noCardActivities);
			
			return initialRelations;
			
		} catch (Exception e) {
			System.err.println("Discovering initial relations failed: " + e.getMessage());
			System.err.println(e);
			throw e;
		}
	}
}
