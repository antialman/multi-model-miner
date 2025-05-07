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
				Set<DiscoveredActivity> potentialNextActivities = declarePostprocessingResult.getPotentialNextActivities(discoveredActivity);
				
				if (potentialNextActivities.size() > 1) { //The log has to be checked if there multiple potential next activities
					Set<Set<DiscoveredActivity>> firstOcurrenceOrders =  new HashSet<Set<DiscoveredActivity>>();
					Set<List<DiscoveredActivity>> lastOcurrenceOrders =  new HashSet<List<DiscoveredActivity>>(); //Using list here to allow iteration in reverse order
					
					//Finding all first and last occurrence orderings of the activities in the refinementSet
					for (XTrace xTrace : eventLog) {
						Set<DiscoveredActivity> firstOcurrenceOrder = new LinkedHashSet<DiscoveredActivity>();
						Set<DiscoveredActivity> lastOcurrenceOrder = new LinkedHashSet<DiscoveredActivity>();
						for (XEvent xEvent : xTrace) {
							String activityName = XConceptExtension.instance().extractName(xEvent);
							if (potentialNextActivities.contains(activityNameToActivity.get(activityName))) {
								//Repetitions can cause different activities of the same group to appear in the occurrence orders, however they will be removed in the next step
								firstOcurrenceOrder.add(activityNameToActivity.get(activityName));
								lastOcurrenceOrder.remove(activityNameToActivity.get(activityName));
								lastOcurrenceOrder.add(activityNameToActivity.get(activityName));
							}
						}
						firstOcurrenceOrders.add(firstOcurrenceOrder);
						lastOcurrenceOrders.add(new ArrayList<DiscoveredActivity>(lastOcurrenceOrder));
					}
					
					//Finding the ordered groups of activities among the potential next activities (Response relations)
					while (!firstOcurrenceOrders.isEmpty()) {
						Set<DiscoveredActivity> nextFollowersRespGroup = findNextFollowersRespGroup(firstOcurrenceOrders);
						Iterator<Set<DiscoveredActivity>> it = firstOcurrenceOrders.iterator();
						while (it.hasNext()) {
							Set<DiscoveredActivity> firstOcurrenceOrder = it.next();
							nextFollowersRespGroup.forEach(nextFollower -> firstOcurrenceOrder.remove(nextFollower));
							if (firstOcurrenceOrder.isEmpty()) {
								it.remove();
							}
						}
						refinedClosenessTaskResult.addNextFollowerRespGroup(discoveredActivity, nextFollowersRespGroup);
					}
					
					//Finding the ordered groups of activities among the potential next activities (Precedence relations)
					while (!lastOcurrenceOrders.isEmpty()) {
						Set<DiscoveredActivity> nextFollowersPrecGroup = findNextFollowersPrecGroup(lastOcurrenceOrders);
						Iterator<List<DiscoveredActivity>> it = lastOcurrenceOrders.iterator();
						while (it.hasNext()) {
							List<DiscoveredActivity> lastOcurrenceOrder = it.next();
							nextFollowersPrecGroup.forEach(prevPredecessor -> lastOcurrenceOrder.remove(prevPredecessor));
							if (lastOcurrenceOrder.isEmpty()) {
								it.remove();
							}
						}
						refinedClosenessTaskResult.addNextFollowerPrecGroup(discoveredActivity, nextFollowersPrecGroup);
					}
					
					//Finding Succession relations based on Response and Precedence relations
					for (int i = 0; i < refinedClosenessTaskResult.getFollowerRespGroups(discoveredActivity).size(); i++) {
						for (int j = i+1; j < refinedClosenessTaskResult.getFollowerRespGroups(discoveredActivity).size(); j++) {
							Set<DiscoveredActivity> respGroupA = refinedClosenessTaskResult.getFollowerRespGroups(discoveredActivity).get(i);
							Set<DiscoveredActivity> respGroupB = refinedClosenessTaskResult.getFollowerRespGroups(discoveredActivity).get(j);
							
							int groupAPrecIndex = refinedClosenessTaskResult.getFollowerPrecGroups(discoveredActivity).indexOf(respGroupA);
							int groupBPrecIndex = refinedClosenessTaskResult.getFollowerPrecGroups(discoveredActivity).indexOf(respGroupB);
							
							if (groupAPrecIndex != -1 && groupBPrecIndex != -1 && groupAPrecIndex < groupBPrecIndex) {
								refinedClosenessTaskResult.addFollowerSuccRelation(discoveredActivity, respGroupA, respGroupB);
							}
						}
					}
					
				} else {
					refinedClosenessTaskResult.addNextFollowerRespGroup(discoveredActivity, new HashSet<DiscoveredActivity>(potentialNextActivities));
					refinedClosenessTaskResult.addNextFollowerPrecGroup(discoveredActivity, new HashSet<DiscoveredActivity>(potentialNextActivities));
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
	
	private Set<DiscoveredActivity> findNextFollowersRespGroup(Set<Set<DiscoveredActivity>> firstOcurrenceOrders) {
		Set<DiscoveredActivity> nextFollowersRespGroup = new HashSet<DiscoveredActivity>();
		for (Set<DiscoveredActivity> firstOcurrenceOrder : firstOcurrenceOrders) {
			if (firstOcurrenceOrder.iterator().hasNext()) {
				nextFollowersRespGroup.add(firstOcurrenceOrder.iterator().next());
			}
		}
		return nextFollowersRespGroup;
	}
	
	private Set<DiscoveredActivity> findNextFollowersPrecGroup(Set<List<DiscoveredActivity>> lastOcurrenceOrders) {
		Set<DiscoveredActivity> nextFollowersPrecGroup = new HashSet<DiscoveredActivity>();
		
		for (List<DiscoveredActivity> lastOcurrenceOrder : lastOcurrenceOrders) {
			if (lastOcurrenceOrder.listIterator(lastOcurrenceOrder.size()).hasPrevious()) {
				nextFollowersPrecGroup.add(lastOcurrenceOrder.listIterator(lastOcurrenceOrder.size()).previous());
			}
		}
		return nextFollowersPrecGroup;
	}
}
