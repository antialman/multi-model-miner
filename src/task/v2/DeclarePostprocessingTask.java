package task.v2;

import java.util.List;

import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import task.DeclareDiscoveryResult;
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
			
			List<DiscoveredConstraint> prunedConstraints = TransitiveClosureUtils.getPrunedConstraints(declareDiscoveryResult.getConstraints());
			
			
			
			
			DeclarePostprocessingResult declarePostprocessingResult = new DeclarePostprocessingResult();
			declarePostprocessingResult.setActivities(declareDiscoveryResult.getActivities());
			declarePostprocessingResult.setAllConstraints(declareDiscoveryResult.getConstraints());
			declarePostprocessingResult.setPrunedConstraints(prunedConstraints);
			return declarePostprocessingResult;
			
			
			
		} catch (Exception e) {
			System.err.println("Discovering Declare model failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}
