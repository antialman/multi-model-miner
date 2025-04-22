package utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import data.DiscoveredActivity;


public final class ConstraintUtils {
	
	
	//Private constructor to avoid unnecessary instantiation of the class
	private ConstraintUtils() {
	}
	
	public static Set<DiscoveredActivity> getAllActivitiesFromLog(XLog log, boolean considerLifecycle) {
		Map<DiscoveredActivity, Integer> activityFreqs = new HashMap<>();
		
		for (XTrace trace : log) {
			Set<DiscoveredActivity> temp = new HashSet<>();
			
			for (XEvent evt : trace) {
				String name = XConceptExtension.instance().extractName(evt);
				String transition = XLifecycleExtension.instance().extractTransition(evt);
				
				DiscoveredActivity a;
				if (considerLifecycle)
					a = new DiscoveredActivity(name, transition, 0); 
				else
					a = new DiscoveredActivity(name, 0);
				
				a.setPayloadAttributes(
					evt.getAttributes().values().stream()
						.filter(att -> !att.getKey().equals(XConceptExtension.KEY_NAME) 
								&& !att.getKey().equals(XLifecycleExtension.KEY_TRANSITION)
								&& !att.getKey().equals(XTimeExtension.KEY_TIMESTAMP))
						.map(att -> att.getKey())
						.collect(Collectors.toSet())
				);
				
				temp.add(a);
			}
			
			for (DiscoveredActivity act : temp) {
				if (activityFreqs.containsKey(act)) {
					DiscoveredActivity key = activityFreqs.keySet().stream().filter(k -> k.equals(act)).findFirst().get();
					key.getPayloadAttributes().addAll(act.getPayloadAttributes());
					
					activityFreqs.replace(act, activityFreqs.get(act)+1);
						
					} else {
						act.setPayloadAttributes(act.getPayloadAttributes());
						activityFreqs.put(act, 1);
				}
			}
			
		}
		
		Set<DiscoveredActivity> allActivities = new HashSet<>();
		for (Map.Entry<DiscoveredActivity, Integer> entry : activityFreqs.entrySet()) {
			DiscoveredActivity tempAct = entry.getKey();
			int frequency = entry.getValue();
			
			DiscoveredActivity act = new DiscoveredActivity(tempAct.getActivityName(), tempAct.getActivityTransition(), (float) frequency / log.size() );
			act.setPayloadAttributes(tempAct.getPayloadAttributes());
			
			allActivities.add(act);
		}
		
		return allActivities;
	
	}
}
