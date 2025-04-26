package task.v2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;
import data.v1v2.ActivityRelationsContainer;
import javafx.concurrent.Task;
import model.v2.ModelFactory;
import utils.CliqueUtilsV2;

public class InitialPetriNetTask_Old extends Task<InitialPetriNetResult> {

	private DeclarePostprocessingResult declarePostprocessingResult;
	private Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelationsMap;


	private ModelFactory modelFactory = new ModelFactory();
	private Set<DiscoveredActivity> processedActivities = new HashSet<DiscoveredActivity>();
	private Set<DiscoveredActivity> unProcessedActivities = new HashSet<DiscoveredActivity>();

	public InitialPetriNetTask_Old(DeclarePostprocessingResult declarePostprocessingResult) {
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

			//Setting artificial start and end activities, former of which will serve as the entry point for the Petri net construction
			modelFactory.setArtificialStart(declarePostprocessingResult.getArtificialStart());
			modelFactory.setArtificialEnd(declarePostprocessingResult.getArtificialEnd());

			//while (true) {
				while (modelFactory.hasUnProcessedActivities()) {
					processActivity(modelFactory.getUnProcessedActivities().iterator().next());
				}
				
				System.out.println("Unprocessed activities: " + unProcessedActivities);
				
//				Set<DiscoveredActivity> earliestUnprocessedActivities = getImmediateOutActivities(new ArrayList<DiscoveredActivity>(unProcessedActivities));
//				Set<DiscoveredActivity> latestProcessedActivities = getImmediateInActivities(new ArrayList<DiscoveredActivity>(processedActivities));
				
				
				
//				if (!unProcessedActivities.isEmpty()) {
//					Set<DiscoveredActivity> earliestUnprocessedActivities = getImmediateOutActivities(new ArrayList<DiscoveredActivity>(unProcessedActivities));
//					Set<DiscoveredActivity> latestUnprocessedActivities = getImmediateInActivities(new ArrayList<DiscoveredActivity>(unProcessedActivities));
//					System.out.println("Earliest of these" + earliestUnprocessedActivities);
//					System.out.println("Latest of these" + latestUnprocessedActivities);
//					
//					earliestUnprocessedActivities.forEach(act -> {modelFactory.addUnprocessedActivity(act);});
//					latestUnprocessedActivities.forEach(act -> {modelFactory.addUnprocessedActivity(act);});
//				} else {
//					break;
//				}	
//			}
			
			
			

			System.out.println("Unprocessed activities: " + unProcessedActivities);
			System.out.println("Earliest of these" + getImmediateOutActivities(new ArrayList<DiscoveredActivity>(unProcessedActivities)));
			System.out.println("Latest of these" + getImmediateInActivities(new ArrayList<DiscoveredActivity>(unProcessedActivities)));

			//modelFactory.connectArtificialEnd(declarePostprocessingResult.getArtificialEnd());


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

		//Immediate outgoing activities (i.e., activities that are not reached through intermediary XOR branches) 
		Set<DiscoveredActivity> outActivities = new HashSet<DiscoveredActivity>(currActivityRelations.getPrunedOutActivities());
		Set<DiscoveredActivity> immediateOutActivities = getImmediateOutActivities(new ArrayList<DiscoveredActivity>(outActivities));
		System.out.println("Immediate out activities of " + currActivity.getActivityName() + ": " + immediateOutActivities);

		//Immediate incoming activities (i.e., activities that are not reached through intermediary XOR branches)
		Set<DiscoveredActivity> inActivities = new HashSet<DiscoveredActivity>(currActivityRelations.getPrunedInActivities());
		Set<DiscoveredActivity> immediateInActivities = getImmediateInActivities(new ArrayList<DiscoveredActivity>(inActivities));
		System.out.println("Immediate in activities of " + currActivity.getActivityName() + ": " + immediateInActivities);


		//Outgoing successions (simple followers, mandatory AND-splits, and mandatory AND-joins)
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
		List<Set<DiscoveredActivity>> notcoOutCliques = new ArrayList<Set<DiscoveredActivity>>();
		CliqueUtilsV2.findNotCoCliques(new ArrayList<DiscoveredActivity>(), new ArrayList<DiscoveredActivity>(immediatePrecOutActivities), new ArrayList<DiscoveredActivity>(), activityToRelationsMap, notcoOutCliques);
		for (Set<DiscoveredActivity> notcoOutClique : notcoOutCliques) {
			float cliqueSupport = 0;
			for (DiscoveredActivity cliqueActivity : notcoOutClique) {
				cliqueSupport = cliqueSupport + cliqueActivity.getActivitySupport(); //For detecting if executing an activity from this clique is required 
			}
			boolean required = cliqueSupport >= currActivity.getActivitySupport() ? true : false; 
			modelFactory.addXorSplit(currActivity, notcoOutClique, required);
			immediatePrecOutActivities.removeAll(notcoOutClique);
		}
		//Skippable AND-splits and skippable individual activities
		Set<Set<DiscoveredActivity>> coExOutCliques = CliqueUtilsV2.findCoExCliques(immediatePrecOutActivities, activityToRelationsMap);
		for (Set<DiscoveredActivity> coExOutClique : coExOutCliques) {
			if (coExOutClique.size() > 1) {
				modelFactory.addOptionalAndSplit(currActivity, coExOutClique);
			} else if (coExOutClique.size() == 1) {
				modelFactory.addOptionalNextActivity(currActivity, coExOutClique.iterator().next());
			}
		}



		//Incoming responses
		Set<DiscoveredActivity> immediateRespInActivities = new HashSet<DiscoveredActivity>();
		for (DiscoveredActivity respInActivity : currActivityRelations.getRespPrunedInActivities()) {
			if (immediateInActivities.contains(respInActivity)) {
				immediateRespInActivities.add(respInActivity);
			}
		}
		//XOR-joins
		List<Set<DiscoveredActivity>> notcoInCliques = new ArrayList<Set<DiscoveredActivity>>();
		CliqueUtilsV2.findNotCoCliques(new ArrayList<DiscoveredActivity>(), new ArrayList<DiscoveredActivity>(immediateRespInActivities), new ArrayList<DiscoveredActivity>(), activityToRelationsMap, notcoInCliques);
		for (Set<DiscoveredActivity> notcoInClique : notcoInCliques) {
			float cliqueSupport = 0;
			for (DiscoveredActivity cliqueActivity : notcoInClique) {
				cliqueSupport = cliqueSupport + cliqueActivity.getActivitySupport(); //For detecting if executing an activity from this clique is required 
			}
			boolean required = cliqueSupport >= currActivity.getActivitySupport() ? true : false;
			modelFactory.addXorJoin(currActivity, notcoInClique, required);
			immediateRespInActivities.removeAll(notcoInClique);
		}
		//Skippable AND-joins and skippable individual activities
		Set<Set<DiscoveredActivity>> coExInCliques = CliqueUtilsV2.findCoExCliques(immediateRespInActivities, activityToRelationsMap);
		for (Set<DiscoveredActivity> coExInClique : coExInCliques) {
			if (coExInClique.size() > 1) {
				modelFactory.addOptionalAndJoin(currActivity, coExInClique);
			} else if (coExInClique.size() == 1) {
				modelFactory.addOptionalPreviousActivity(currActivity, coExInClique.iterator().next());
			}
		}


		//Outgoing responses
		for (DiscoveredActivity respOutActivity : currActivityRelations.getRespPrunedOutActivities()) {
			if (immediateOutActivities.contains(respOutActivity)) {
				//Outgoing response activities will be added as unprocessed here, which will lead them to be picked up and processed as incoming responses later
				modelFactory.addUnprocessedActivity(respOutActivity);
			}
		}
		//Incoming precedences
		for (DiscoveredActivity precInActivity : currActivityRelations.getPrecPrunedInActivities()) {
			if (immediateOutActivities.contains(precInActivity)) {
				//Incoming precedence activities will be added as unprocessed here, which will lead them to be picked up and processed as outgoing precedences later
				modelFactory.addUnprocessedActivity(precInActivity);
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
