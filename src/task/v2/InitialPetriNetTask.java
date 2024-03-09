package task.v2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
				Set<DiscoveredActivity> immediateInActivities = getImmediateInActivities(new ArrayList<DiscoveredActivity>(activityToRelationsMap.get(da).getPrunedInActivities()));
				Set<DiscoveredActivity> immediateOutActivities = getImmediateOutActivities(new ArrayList<DiscoveredActivity>(activityToRelationsMap.get(da).getPrunedOutActivities()));
				//System.out.println("Immediate in activities of " + da.getActivityName() + ": " + immediateInActivities);
				//System.out.println("Immediate out activities of " + da.getActivityName() + ": " + immediateOutActivities);
			}
			
			
			
			System.out.println("Initial Petri net task finished at: " + taskStartTime + " - total time: " + (System.currentTimeMillis() - taskStartTime));
			
			return null;
			
		} catch (Exception e) {
			System.err.println("Initial Petri net task failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	
	private Set<DiscoveredActivity> getImmediateInActivities(List<DiscoveredActivity> activitiesInList) {
		Set<DiscoveredActivity> immediateInActivities = new HashSet<DiscoveredActivity>(activitiesInList);
		
		for (int i = 0; i < activitiesInList.size(); i++) {
			for (int j = i+1; j < activitiesInList.size(); j++) {
				ActivityRelationsContainer act1Relations = activityToRelationsMap.get(activitiesInList.get(i));
				ActivityRelationsContainer act2Relations = activityToRelationsMap.get(activitiesInList.get(j));
				
				if (!act1Relations.notCoexAllContains(act2Relations.getActivity())) { //If there is no not co-existence relation, then the activities can appear in the same trace
					if (act1Relations.notSuccAllOutContains(act2Relations.getActivity())) { //If act1 has outgoing not succession from act2, then there is fixed order act2 -> act1
						immediateInActivities.remove(act2Relations.getActivity());
						//System.out.println(act2Relations.getActivity().getActivityName() + "-> " + act1Relations.getActivity().getActivityName());
					} else if (act2Relations.notSuccAllOutContains(act1Relations.getActivity())) { //If act2 has outgoing not succession from act1, then there is fixed order act1 -> act2
						immediateInActivities.remove(act1Relations.getActivity());
						//System.out.println(act1Relations.getActivity().getActivityName() + "-> " + act2Relations.getActivity().getActivityName());
					}
				}
			}
		}
		return immediateInActivities;
	}
	
	private Set<DiscoveredActivity> getImmediateOutActivities(List<DiscoveredActivity> activitiesOutList) {
		Set<DiscoveredActivity> immediateOutActivities = new HashSet<DiscoveredActivity>(activitiesOutList);
		
		for (int i = 0; i < activitiesOutList.size(); i++) {
			for (int j = i+1; j < activitiesOutList.size(); j++) {
				ActivityRelationsContainer act1Relations = activityToRelationsMap.get(activitiesOutList.get(i));
				ActivityRelationsContainer act2Relations = activityToRelationsMap.get(activitiesOutList.get(j));
				
				if (!act1Relations.notCoexAllContains(act2Relations.getActivity())) { //If there is no not co-existence relation, then the activities can appear in the same trace
					if (act1Relations.notSuccAllOutContains(act2Relations.getActivity())) { //If act1 has outgoing not succession from act2, then there is fixed order act2 -> act1
						immediateOutActivities.remove(act1Relations.getActivity());
						//System.out.println(act2Relations.getActivity().getActivityName() + "-> " + act1Relations.getActivity().getActivityName());
					} else if (act2Relations.notSuccAllOutContains(act1Relations.getActivity())) { //If act2 has outgoing not succession from act1, then there is fixed order act1 -> act2
						immediateOutActivities.remove(act2Relations.getActivity());
						//System.out.println(act1Relations.getActivity().getActivityName() + "-> " + act2Relations.getActivity().getActivityName());
					}
				}
			}
		}
		return immediateOutActivities;
	}

}
