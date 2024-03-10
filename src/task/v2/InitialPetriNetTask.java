package task.v2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import data.ActivityRelationsContainer;
import data.DiscoveredActivity;
import javafx.concurrent.Task;
import model.v2.ModelFactory;
import utils.CliqueUtilsV2;

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
			initialPetriNetResult.setModelFactory(modelFactory);
			
			//Setting the artificial start activity which will serve as the entry point for the Petri net construction
			modelFactory.setArtificialStart(declarePostprocessingResult.getArtificialStart());
			
			while (modelFactory.hasUnProcessedActivities()) {
				processActivity(modelFactory.getUnProcessedActivities().iterator().next());
			}
			
			System.out.println("Initial Petri net task finished at: " + taskStartTime + " - total time: " + (System.currentTimeMillis() - taskStartTime));
			
			return initialPetriNetResult;
			
		} catch (Exception e) {
			System.err.println("Initial Petri net task failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	
	
	private void processActivity(DiscoveredActivity currActivity) {
		ActivityRelationsContainer currActivityRelations = activityToRelationsMap.get(currActivity);
		
		Set<DiscoveredActivity> outActivities = new HashSet<DiscoveredActivity>(currActivityRelations.getPrunedOutActivities());
		Set<DiscoveredActivity> immediateOutActivities = getImmediateOutActivities(new ArrayList<DiscoveredActivity>(outActivities));
		System.out.println("Immediate out activities of " + currActivity.getActivityName() + ": " + immediateOutActivities);
		
		//Outgoing successions (simple followers and mandatory AND-splits)
		Set<DiscoveredActivity> immediateSuccOutActivities = new HashSet<DiscoveredActivity>();
		for (DiscoveredActivity succOutActivity : currActivityRelations.getSuccPrunedOutActivities()) {
			if (immediateOutActivities.contains(succOutActivity)) {
				immediateSuccOutActivities.add(succOutActivity);
			}
		}
		modelFactory.addMandatoryFollower(currActivity, immediateSuccOutActivities);
		
		
		//Outgoing precedences
		Set<DiscoveredActivity> immediatePrecOutActivities = new HashSet<DiscoveredActivity>();
		for (DiscoveredActivity precOutActivity : currActivityRelations.getPrecPrunedOutActivities()) {
			if (immediateOutActivities.contains(precOutActivity)) {
				immediatePrecOutActivities.add(precOutActivity);
			}
		}
		//XOR-splits
		List<Set<DiscoveredActivity>> notcoCliques = new ArrayList<Set<DiscoveredActivity>>();
		CliqueUtilsV2.findNotCoCliques(new ArrayList<DiscoveredActivity>(), new ArrayList<DiscoveredActivity>(immediatePrecOutActivities), new ArrayList<DiscoveredActivity>(), activityToRelationsMap, notcoCliques);
		for (Set<DiscoveredActivity> notcoClique : notcoCliques) {
			float cliqueSupport = 0;
			for (DiscoveredActivity cliqueActivity : notcoClique) {
				cliqueSupport = cliqueSupport + cliqueActivity.getActivitySupport(); //For detecting if executing an activity from this clique is required 
			}
			boolean required = cliqueSupport >= currActivity.getActivitySupport() ? true : false; 
			modelFactory.addXorSplit(currActivity, notcoClique, required);
			immediatePrecOutActivities.removeAll(notcoClique);
		}
		//Skippable AND-splits and skippable individual activities
		Set<Set<DiscoveredActivity>> coExCliques = CliqueUtilsV2.findCoExCliques(immediatePrecOutActivities, activityToRelationsMap);
		for (Set<DiscoveredActivity> coExClique : coExCliques) {
			if (coExClique.size() > 1) {
				modelFactory.addOptionalAndSplit(currActivity, coExClique);
			} else if (coExClique.size() == 1) {
				modelFactory.addOptionalFollower(currActivity, coExClique.iterator().next());
			}
		}
		
		
		
		
		
		
		modelFactory.markActivityAsProcessed(currActivity);
		
		processedActivities.add(currActivity);
		unProcessedActivities.remove(currActivity);
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
	
	

}
