package task.v3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import data.DiscoveredActivity;
import javafx.concurrent.Task;

public class RefinedClosenessTask extends Task<RefinedClosenessTaskResult> {
	
	private XLog eventLog;
	private DeclarePostprocessingResult declarePostprocessingResult;
	
	private Map<String, DiscoveredActivity> activityNameToActivity = new HashMap<String, DiscoveredActivity>(); 
	
	
	public void setEventLog(XLog eventLog) {
		this.eventLog = eventLog;
	}
	
	public void setDeclarePostProcessingResult(DeclarePostprocessingResult declarePostprocessingResult) {
		this.declarePostprocessingResult = declarePostprocessingResult;
	}
	
	

	@Override
	protected RefinedClosenessTaskResult call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Refinement of temporal closeness started at: " + taskStartTime);
			
			for (DiscoveredActivity discoveredActivity : declarePostprocessingResult.getAllActivities()) {
				activityNameToActivity.put(discoveredActivity.getActivityName(), discoveredActivity);
			}

			RefinedClosenessTaskResult refinedClosenessTaskResult = new RefinedClosenessTaskResult();
			
			for (DiscoveredActivity discoveredActivity : declarePostprocessingResult.getAllActivities()) {
				Set<DiscoveredActivity> refinementSet = declarePostprocessingResult.getPotentialNextActivities(discoveredActivity);
				
				if (refinementSet.size() > 1) { //The log has to be checked if there multiple potential next activities
					Set<Set<DiscoveredActivity>> firstOcurrenceOrders =  new HashSet<Set<DiscoveredActivity>>();
					Set<Set<DiscoveredActivity>> lastOcurrenceOrders =  new HashSet<Set<DiscoveredActivity>>();
					
					//Finding all first and last occurrence orderings of the activities in the refinementSet
					for (XTrace xTrace : eventLog) {
						Set<DiscoveredActivity> firstOcurrenceOrder = new LinkedHashSet<DiscoveredActivity>();
						Set<DiscoveredActivity> lastOcurrenceOrder = new LinkedHashSet<DiscoveredActivity>();
						for (XEvent xEvent : xTrace) {
							String activityName = XConceptExtension.instance().extractName(xEvent);
							if (refinementSet.contains(activityNameToActivity.get(activityName))) {
								//Repetitions can cause different activities of the same group to appear in the occurrence orders, however they will be removed in the next step
								firstOcurrenceOrder.add(activityNameToActivity.get(activityName));
								lastOcurrenceOrder.remove(activityNameToActivity.get(activityName));
								lastOcurrenceOrder.add(activityNameToActivity.get(activityName));
							}
						}
						firstOcurrenceOrders.add(firstOcurrenceOrder);
						lastOcurrenceOrders.add(lastOcurrenceOrder);
					}
					
					System.out.println(firstOcurrenceOrders);
					System.out.println(lastOcurrenceOrders);
					
					//Finding the first group of activities among the refinementSet
					Set<DiscoveredActivity> currentActivityGroup = new HashSet<DiscoveredActivity>();
					for (Set<DiscoveredActivity> firstOcurrenceOrder : firstOcurrenceOrders) {
						if (firstOcurrenceOrder.iterator().hasNext()) {
							currentActivityGroup.add(firstOcurrenceOrder.iterator().next());
						}
					}
					for (DiscoveredActivity currentActivity : currentActivityGroup) {
						firstOcurrenceOrders.forEach(firstOcurrenceOrder -> {firstOcurrenceOrder.remove(currentActivity);});
					}
					
					System.out.println(currentActivityGroup);
					
					System.out.println(firstOcurrenceOrders);
					System.out.println(lastOcurrenceOrders);
					
					refinedClosenessTaskResult.addFirstFollowerGroup(discoveredActivity, currentActivityGroup);
				} else {
					refinedClosenessTaskResult.addFirstFollowerGroup(discoveredActivity, new HashSet<DiscoveredActivity>(refinementSet));
				}
			}
			
			

			System.out.println("Refinement of temporal closeness finished at: " + taskStartTime + " - total time: " + (System.currentTimeMillis() - taskStartTime));

			return refinedClosenessTaskResult;
		} catch (Exception e) {
			System.err.println("Declare post-processing failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

	}
}
