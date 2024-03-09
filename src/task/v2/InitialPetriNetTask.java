package task.v2;

import java.util.Map;

import data.ActivityRelationsContainer;
import data.DiscoveredActivity;
import javafx.concurrent.Task;

public class InitialPetriNetTask extends Task<InitialPetriNetResult> {
	
	private Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelationsMap;
	
	public InitialPetriNetTask(DeclarePostprocessingResult declarePostprocessingResult) {
		this.activityToRelationsMap = declarePostprocessingResult.getActivityToRelationsMap();
	}
	
	@Override
	protected InitialPetriNetResult call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Initial Petri net task started at: " + taskStartTime);
			
			
			for (DiscoveredActivity da : activityToRelationsMap.keySet()) {
				
			}
			
			
			
			System.out.println("Initial Petri net task finished at: " + taskStartTime + " - total time: " + (System.currentTimeMillis() - taskStartTime));
			
			return null;
			
		} catch (Exception e) {
			System.err.println("Initial Petri net task failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

}
