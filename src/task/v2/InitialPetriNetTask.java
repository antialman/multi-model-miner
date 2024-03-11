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
import utils.CliqueUtilsV2;

public class InitialPetriNetTask extends Task<InitialPetriNetResult> {

	private DeclarePostprocessingResult declarePostprocessingResult;
	private Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelationsMap;


	private ModelFactory modelFactory = new ModelFactory();
	private Set<DiscoveredActivity> processedActivities = new HashSet<DiscoveredActivity>();
	private Set<DiscoveredActivity> unprocessedActivities = new HashSet<DiscoveredActivity>();

	public InitialPetriNetTask(DeclarePostprocessingResult declarePostprocessingResult) {
		this.declarePostprocessingResult = declarePostprocessingResult;
		this.activityToRelationsMap = declarePostprocessingResult.getActivityToRelationsMap(); //For easier reference
		unprocessedActivities.addAll(declarePostprocessingResult.getAllActivities());
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

			while (true) {
				while (modelFactory.hasUnProcessedActivities()) {
					processActivity(modelFactory.getUnProcessedActivities().iterator().next());
				}
				
				Set<DiscoveredActivity> incompleteActivities = modelFactory.getIncompleteActivities();
				if (!incompleteActivities.isEmpty()) {
					System.out.println("Processing incomplete activities: " + incompleteActivities);
					processIncompleteActivities(incompleteActivities);
				} else {
					break;
				}
			}
			
			

			
			System.out.println("All unprocessed activities: " + unprocessedActivities);
			Set<DiscoveredActivity> earliestUnprocessedActivities = getEarliestOutActivities(new ArrayList<DiscoveredActivity>(unprocessedActivities));
			System.out.println("Earliest unprocessed activities: " + earliestUnprocessedActivities);
			
			for (DiscoveredActivity unprocessedActivity : earliestUnprocessedActivities) {
				System.out.println("\tClosest processed in activities to " + unprocessedActivity.getActivityName() + " are: " + getProcessedInActivities(unprocessedActivity));
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

		//Immediate outgoing activities (i.e., activities that are not reached through intermediary XOR branches) 
		Set<DiscoveredActivity> outActivities = new HashSet<DiscoveredActivity>(currActivityRelations.getPrunedOutActivities());
		Set<DiscoveredActivity> immediateOutActivities = getEarliestOutActivities(new ArrayList<DiscoveredActivity>(outActivities));
		//Set<DiscoveredActivity> immediateOutActivities = outActivities;
		System.out.println("Immediate out activities of " + currActivity.getActivityName() + ": " + immediateOutActivities);

		//Immediate incoming activities (i.e., activities that are not reached through intermediary XOR branches)
		Set<DiscoveredActivity> inActivities = new HashSet<DiscoveredActivity>(currActivityRelations.getPrunedInActivities());
		Set<DiscoveredActivity> immediateInActivities = getEarliestInActivities(new ArrayList<DiscoveredActivity>(inActivities));
		//Set<DiscoveredActivity> immediateInActivities = inActivities;
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
		unprocessedActivities.remove(currActivity);
	}
	
	
	private void processIncompleteActivities(Set<DiscoveredActivity> incompleteActivities) {
		//Processing of activities that have no ordering constraints to their nearest next activities (e.g., XOR-join immediately followed by XOR-split)
		List<Set<DiscoveredActivity>> notcoCliques = new ArrayList<Set<DiscoveredActivity>>();
		CliqueUtilsV2.findNotCoCliques(new ArrayList<DiscoveredActivity>(), new ArrayList<DiscoveredActivity>(incompleteActivities), new ArrayList<DiscoveredActivity>(), activityToRelationsMap, notcoCliques);
		
		for (Set<DiscoveredActivity> notcoClique : notcoCliques) { //Each incomplete XOR
			Set<DiscoveredActivity> closestOutActivities = new HashSet<DiscoveredActivity>();
			float notcoCliqueSupport = 0; //Support of the incomplete XOR
			for (DiscoveredActivity notcoActivity : notcoClique) {
				closestOutActivities.addAll(getClosestOutActivities(notcoActivity)); //Closest out activities of the incomplete XOR
				notcoCliqueSupport = notcoCliqueSupport + notcoActivity.getActivitySupport();
			}
			
			List<Set<DiscoveredActivity>> notcoOutCliques = new ArrayList<Set<DiscoveredActivity>>();
			CliqueUtilsV2.findNotCoCliques(new ArrayList<DiscoveredActivity>(), new ArrayList<DiscoveredActivity>(closestOutActivities), new ArrayList<DiscoveredActivity>(), activityToRelationsMap, notcoOutCliques);
			
			for (Set<DiscoveredActivity> notcoOutClique : notcoOutCliques) { //Each clique among the closest out activities of the incomplete XOR
				float notcoOutCliqueSupport = 0; //Support out clique
				for (DiscoveredActivity notcoOutActivity : notcoOutClique) {
					notcoOutCliqueSupport = notcoOutCliqueSupport + notcoOutActivity.getActivitySupport();
				}
				
				modelFactory.addFollowersOfIncompleteClique(notcoClique, notcoOutClique, notcoCliqueSupport, notcoOutCliqueSupport);
			}
		}
	}

	private Set<DiscoveredActivity> getEarliestOutActivities(List<DiscoveredActivity> activitiesOutList) {
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

	private Set<DiscoveredActivity> getEarliestInActivities(List<DiscoveredActivity> activitiesInList) {
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
	
	private Set<DiscoveredActivity> getClosestOutActivities(DiscoveredActivity currActivity) {
		ActivityRelationsContainer currActivityRelations = activityToRelationsMap.get(currActivity);
		List<DiscoveredActivity> candidatesList = new ArrayList<DiscoveredActivity>();
		for (DiscoveredActivity discoveredActivity : declarePostprocessingResult.getAllActivities()) { //Could maybe check only unprocessed activities?
			if (!currActivityRelations.getNotCoexActivities().contains(discoveredActivity) && currActivityRelations.getNotSuccAllInActivities().contains(discoveredActivity)) {
				candidatesList.add(discoveredActivity);
			}
		}
		return getEarliestOutActivities(candidatesList);
	}
	
	
	private Set<DiscoveredActivity> getProcessedInActivities(DiscoveredActivity unprocessedActivity) {
		ActivityRelationsContainer unprocessedActRelations = activityToRelationsMap.get(unprocessedActivity);
		List<DiscoveredActivity> candidatesList = new ArrayList<DiscoveredActivity>();
		for (DiscoveredActivity processedActivity : processedActivities) {
			if (!unprocessedActRelations.getNotCoexActivities().contains(processedActivity) && unprocessedActRelations.getNotSuccAllOutActivities().contains(processedActivity)) {
				candidatesList.add(processedActivity);
			}
		}
		return getEarliestInActivities(candidatesList);
	}
}
