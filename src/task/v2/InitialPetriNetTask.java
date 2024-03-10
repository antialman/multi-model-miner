package task.v2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.ActivityRelationsContainer;
import data.DiscoveredActivity;
import javafx.concurrent.Task;
import model.v2.ModelFactory;
import model.v2.TransitionNode;

public class InitialPetriNetTask extends Task<InitialPetriNetResult> {
	
	private DeclarePostprocessingResult declarePostprocessingResult;
	private Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelationsMap;
	
	
	private ModelFactory modelFactory = new ModelFactory();
	private Set<DiscoveredActivity> processedActivities = new HashSet<DiscoveredActivity>();
	private Set<DiscoveredActivity> unProcessedActivities = new HashSet<DiscoveredActivity>();
	
	public InitialPetriNetTask(DeclarePostprocessingResult declarePostprocessingResult) {
		this.declarePostprocessingResult = declarePostprocessingResult;
		this.activityToRelationsMap = declarePostprocessingResult.getActivityToRelationsMap(); //For easier reference
		unProcessedActivities.addAll(declarePostprocessingResult.getAllActivities());
	}
	
	@Override
	protected InitialPetriNetResult call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Initial Petri net task started at: " + taskStartTime);
			
			InitialPetriNetResult initialPetriNetResult = new InitialPetriNetResult();
			
			//Processing from artificial start to end
			TransitionNode artificialStartTransition = processActivity(declarePostprocessingResult.getArtificialStart());
			modelFactory.getInitialPlace().addOutgoingTransition(artificialStartTransition);
			
			initialPetriNetResult.setModelFactory(modelFactory);
			
			
			for (DiscoveredActivity da : activityToRelationsMap.keySet()) {
				Set<DiscoveredActivity> immediateInActivities = getImmediateInActivities(new ArrayList<DiscoveredActivity>(activityToRelationsMap.get(da).getPrunedInActivities()));
				Set<DiscoveredActivity> immediateOutActivities = getImmediateOutActivities(new ArrayList<DiscoveredActivity>(activityToRelationsMap.get(da).getPrunedOutActivities()));
				//System.out.println("Immediate in activities of " + da.getActivityName() + ": " + immediateInActivities);
				//System.out.println("Immediate out activities of " + da.getActivityName() + ": " + immediateOutActivities);
				
				
				
				
				
			}
			
			
			
			System.out.println("Initial Petri net task finished at: " + taskStartTime + " - total time: " + (System.currentTimeMillis() - taskStartTime));
			
			return initialPetriNetResult;
			
		} catch (Exception e) {
			System.err.println("Initial Petri net task failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	
	
	private TransitionNode processActivity(DiscoveredActivity da) {
		ActivityRelationsContainer daRelations = activityToRelationsMap.get(da);
		Set<DiscoveredActivity> outActivities = new HashSet<DiscoveredActivity>(daRelations.getPrunedOutActivities());
		Set<DiscoveredActivity> immediateOutActivities = getImmediateOutActivities(new ArrayList<DiscoveredActivity>(outActivities));
		System.out.println("Immediate out activities of " + da.getActivityName() + ": " + immediateOutActivities);
		
		TransitionNode daTransition = modelFactory.getNewLabeledTransition(da);
		
		
		
		
		
		processedActivities.add(da);
		unProcessedActivities.remove(da);
		return daTransition;
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
