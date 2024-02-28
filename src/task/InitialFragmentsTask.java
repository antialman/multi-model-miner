package task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import data.ActivityRelations;
import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import model.PlaceNode;
import model.TransitionNode;
import utils.ModelUtils;

public class InitialFragmentsTask extends Task<InitialFragmentsResult> {

	private List<DiscoveredActivity> discoveredActivities;
	private ConstraintSubsets constraintSubsets;
	private Map<DiscoveredActivity, ActivityRelations> activityRelationsMap;
	private ArrayList<Set<DiscoveredActivity>> notcoCliques;


	public InitialFragmentsTask(List<DiscoveredActivity> discoveredActivities, ConstraintSubsets constraintSubsets) {
		super();
		this.discoveredActivities = discoveredActivities;
		this.constraintSubsets = constraintSubsets;
	}


	@Override
	protected InitialFragmentsResult call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Discovering constraint subsets started at: " + taskStartTime);
			
			InitialFragmentsResult initialFragmentsResult = new InitialFragmentsResult();
			

			//Making it easier to look up which types of relations each activity has to other activities
			activityRelationsMap = new HashMap<DiscoveredActivity, ActivityRelations>();
			discoveredActivities.forEach(da -> {activityRelationsMap.put(da, new ActivityRelations(da));});
			for (DiscoveredConstraint dc : constraintSubsets.getResConstraints()) {
				activityRelationsMap.get(dc.getActivationActivity()).addResponseOut(dc.getTargetActivity());
				activityRelationsMap.get(dc.getTargetActivity()).addResponseIn(dc.getActivationActivity());
			}
			for (DiscoveredConstraint dc : constraintSubsets.getPreConstraints()) {
				activityRelationsMap.get(dc.getTargetActivity()).addPrecedenceOut(dc.getActivationActivity());
				activityRelationsMap.get(dc.getActivationActivity()).addPrecedenceIn(dc.getTargetActivity());
			}
			for (DiscoveredConstraint dc : constraintSubsets.getNotcoConstraints()) {
				activityRelationsMap.get(dc.getActivationActivity()).addMutualExclusion(dc.getTargetActivity());
				activityRelationsMap.get(dc.getTargetActivity()).addMutualExclusion(dc.getActivationActivity());
			}


			//Creating the initial (activity based) fragments
			int nextNodeId = 0;
			for (ActivityRelations activityRelations : activityRelationsMap.values()) {
				DiscoveredActivity mainActivity = activityRelations.getActivity();
				TransitionNode mainTransition = new TransitionNode(nextNodeId++, mainActivity);
				mainTransition.setFragmentMain(true);
				initialFragmentsResult.addFragmentMainTransition(mainTransition);


				if (activityRelations.getResponseIn().isEmpty() && activityRelations.getPrecedenceIn().isEmpty()) {
					//No incoming response nor precedence means no activities require nor enable this activity
					ModelUtils.addInitialPlace(mainTransition, nextNodeId++);
				} else if (activityRelations.getResponseOut().isEmpty() && activityRelations.getPrecedenceOut().isEmpty()) {
					//No outgoing response nor precedence means no activities are required nor enabled by this activity
					ModelUtils.addFinalPlace(mainTransition, nextNodeId++);
				}


				//Outgoing responses from the given activity
				for (DiscoveredActivity resOutAct : activityRelations.getResponseOut()) {
					PlaceNode resOutPlace = new PlaceNode(nextNodeId++);
					TransitionNode resOutTransition = new TransitionNode(nextNodeId++, resOutAct);
					mainTransition.addOutgoingPlace(resOutPlace);
					resOutPlace.addOutgoingTransition(resOutTransition);
					if (activityRelationsMap.get(resOutAct).getPrecedenceOut().isEmpty() && activityRelationsMap.get(resOutAct).getResponseOut().isEmpty()) {
						ModelUtils.addFinalPlace(resOutTransition, nextNodeId++);
					}
				}


				//Incoming precedences to the given activity (mirror of outgoing responses)
				for (DiscoveredActivity preInAct : activityRelations.getPrecedenceIn()) {
					PlaceNode preInPlace = new PlaceNode(nextNodeId++);
					TransitionNode preInTransition = new TransitionNode(nextNodeId++, preInAct);
					mainTransition.addIncomingPlace(preInPlace);
					preInPlace.addIncomingTransition(preInTransition);
					if (activityRelationsMap.get(preInAct).getPrecedenceIn().isEmpty() && activityRelationsMap.get(preInAct).getResponseIn().isEmpty()) {
						ModelUtils.addInitialPlace(preInTransition, nextNodeId++);
					}
				}


				//TODO: Probably need to include coexistence constraints and do a similar clique-based calculation to also find skippable AND splits (e.g., either both A and B are executed in parallel or neither is executed)


				////Outgoing precedences from each activity
				List<DiscoveredActivity> candidates = new ArrayList<DiscoveredActivity>();
				Map<DiscoveredActivity, TransitionNode> activityTransitionMap = new HashMap<DiscoveredActivity, TransitionNode>();

				//1. Create places and transitions for each activity that cannot be in XOR with others, prepare for processing potential XORs
				for (DiscoveredActivity preOutAct : activityRelations.getPrecedenceOut()) {
					if (activityRelations.getResponseOut().contains(preOutAct)) { //If there is also an outgoing response to the same activity, then that activity is already processed
						continue;
					} else if (activityRelationsMap.get(preOutAct).getMutualExclusion().isEmpty()) { //If there are no mutual exclusions and no outgoing response to the same activity, then that activity must be optional
						PlaceNode preOutPlace = new PlaceNode(nextNodeId++);
						TransitionNode preOutTransition = new TransitionNode(nextNodeId++, preOutAct);
						mainTransition.addOutgoingPlace(preOutPlace);
						preOutPlace.addOutgoingTransition(preOutTransition);
						TransitionNode skipTransition = new TransitionNode(nextNodeId++, null);
						preOutPlace.addOutgoingTransition(skipTransition);
						if (activityRelationsMap.get(preOutAct).getPrecedenceOut().isEmpty() && activityRelationsMap.get(preOutAct).getResponseOut().isEmpty()) {
							//No outgoing constraints means no further activities are enabled by this activity 
							ModelUtils.addFinalPlace(preOutTransition, nextNodeId++);
							ModelUtils.addFinalPlace(skipTransition, nextNodeId++);
						}
					} else {
						candidates.add(preOutAct); //Add the activity to a list for finding notcoCliques
						TransitionNode preOutTransition = new TransitionNode(nextNodeId++, preOutAct);
						activityTransitionMap.put(preOutAct, preOutTransition); //Outgoing transitions from notco clique places
					}
				}
				//Adding final places after transitions that are associated to notco cliques
				for (TransitionNode preOutTransition : activityTransitionMap.values()) {
					if (activityRelationsMap.get(preOutTransition.getDiscoveredActivity()).getPrecedenceOut().isEmpty() && activityRelationsMap.get(preOutTransition.getDiscoveredActivity()).getResponseOut().isEmpty()) {
						//No outgoing constraints means no further activities are enabled by this activity 
						ModelUtils.addFinalPlace(preOutTransition, nextNodeId++);
					}
				}

				//2. Find all maximal notco cliques
				notcoCliques = new ArrayList<Set<DiscoveredActivity>>();
				findCliques(new ArrayList<DiscoveredActivity>(), candidates, new ArrayList<DiscoveredActivity>()); //Fills the notcoCliques list

				//3. Create a place for each notco clique and connect it to the corresponding transitions
				for (Set<DiscoveredActivity> notcoClique : notcoCliques) {
					PlaceNode cliquePlace = new PlaceNode(nextNodeId++);
					mainTransition.addOutgoingPlace(cliquePlace); //One outgoing place per each notco clique
					float cliqueSupport = 0;
					for (DiscoveredActivity notcoActivity : notcoClique) {
						cliquePlace.addOutgoingTransition(activityTransitionMap.get(notcoActivity)); //Firing any activity of a notco click consumes the token of that clique (disabling other activities of that clique)
						cliqueSupport = cliqueSupport + notcoActivity.getActivitySupport(); //For detecting if executing an activity from this clique is required 
					}
					if (mainActivity.getActivitySupport() > cliqueSupport) { //TODO: This check can probably break if there is looping behaviour (e.g., B is always preceded by A, but A can also occur after B)
						TransitionNode skipTransition = new TransitionNode(nextNodeId++, null);
						cliquePlace.addOutgoingTransition(skipTransition);
					}
				}


				////Incoming responses to each activity (mirror of outgoing precedences)
				candidates = new ArrayList<DiscoveredActivity>();
				activityTransitionMap = new HashMap<DiscoveredActivity, TransitionNode>();

				//1. Create places and transitions for each activity that cannot be in XOR with others, prepare for processing potential XORs
				for (DiscoveredActivity resInAct : activityRelations.getResponseIn()) {
					if (activityRelations.getPrecedenceIn().contains(resInAct)) { //If there is also an incoming precedence to the same activity, then that activity is already processed
						continue;
					} else if (activityRelationsMap.get(resInAct).getMutualExclusion().isEmpty()) { //If there are no mutual exclusions and no incoming precedence to the same activity, then that activity must be optional
						PlaceNode resInPlace = new PlaceNode(nextNodeId++);
						TransitionNode resInTransition = new TransitionNode(nextNodeId++, resInAct);
						mainTransition.addIncomingPlace(resInPlace);
						resInPlace.addIncomingTransition(resInTransition);
						TransitionNode skipTransition = new TransitionNode(nextNodeId++, null);
						resInPlace.addIncomingTransition(skipTransition);
						if (activityRelationsMap.get(resInAct).getPrecedenceIn().isEmpty() && activityRelationsMap.get(resInAct).getResponseIn().isEmpty()) {
							//No incoming constraints means no activities enable this activity 
							ModelUtils.addInitialPlace(resInTransition, nextNodeId++);
							ModelUtils.addInitialPlace(skipTransition, nextNodeId++);
						}
					} else {
						candidates.add(resInAct); //Add the activity to a list for finding notcoCliques
						TransitionNode resInTransition = new TransitionNode(nextNodeId++, resInAct);
						activityTransitionMap.put(resInAct, resInTransition); //Incoming transitions from notco clique places
					}
				}
				//Adding initial places before transitions that are associated to notco cliques
				for (TransitionNode resInTransition : activityTransitionMap.values()) {
					if (activityRelationsMap.get(resInTransition.getDiscoveredActivity()).getPrecedenceIn().isEmpty() && activityRelationsMap.get(resInTransition.getDiscoveredActivity()).getResponseIn().isEmpty()) {
						//No outgoing constraints means no further activities are enabled by this activity 
						ModelUtils.addInitialPlace(resInTransition, nextNodeId++);
					}
				}

				//2. Find all maximal notco cliques
				notcoCliques = new ArrayList<Set<DiscoveredActivity>>();
				findCliques(new ArrayList<DiscoveredActivity>(), candidates, new ArrayList<DiscoveredActivity>()); //Fills the notcoCliques list

				//3. Create a place for each notco clique and connect it to the corresponding transitions
				for (Set<DiscoveredActivity> notcoClique : notcoCliques) {
					PlaceNode cliquePlace = new PlaceNode(nextNodeId++);
					mainTransition.addIncomingPlace(cliquePlace); //One incoming place per each notco clique
					float cliqueSupport = 0;
					for (DiscoveredActivity notcoActivity : notcoClique) {
						cliquePlace.addIncomingTransition(activityTransitionMap.get(notcoActivity)); //Firing any activity of a notco click produces the token of that clique (only one activity per clique should be fired)
						cliqueSupport = cliqueSupport + notcoActivity.getActivitySupport(); //For detecting if executing an activity from this clique is required 
					}
					if (mainActivity.getActivitySupport() > cliqueSupport) { //TODO: This check can probably break if there is looping behaviour (e.g., B is always preceded by A, but A can also occur after B)
						TransitionNode skipTransition = new TransitionNode(nextNodeId++, null);
						cliquePlace.addIncomingTransition(skipTransition);
					}
				}
			}
			
			initialFragmentsResult.setNodeCount(nextNodeId);

			return initialFragmentsResult;

		} catch (Exception e) {
			System.err.println("Finding constraint subsets failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}


	public List<Set<TransitionNode>> findCliques(List<DiscoveredActivity> potentialClique, List<DiscoveredActivity> candidates, List<DiscoveredActivity> alreadyFound) {
		//Based on the following Bron–Kerbosch algorithm implementation: https://github.com/liziliao/Bron-Kerbosch/blob/master/Bron-Kerbosch.java

		List<DiscoveredActivity> candidatesArray = new ArrayList<DiscoveredActivity>(candidates);
		if (!end(candidates, alreadyFound)) {
			for (DiscoveredActivity candidate : candidatesArray) {
				List<DiscoveredActivity> newCandidates  = new ArrayList<DiscoveredActivity>();
				List<DiscoveredActivity> newAlreadyFound  = new ArrayList<DiscoveredActivity>();

				potentialClique.add(candidate);
				candidates.remove(candidate);

				for (DiscoveredActivity newCandidate : candidates) {
					if (isNotcoNeighbor(candidate, newCandidate)) {
						newCandidates.add(newCandidate);
					}
				}

				for (DiscoveredActivity newFound : alreadyFound) {
					if (isNotcoNeighbor(candidate, newFound)) {
						newAlreadyFound.add(newFound);
					}
				}

				if (newCandidates.isEmpty() && newAlreadyFound.isEmpty()) {
					notcoCliques.add(new HashSet<DiscoveredActivity>(potentialClique));
				} else {
					findCliques(potentialClique, newCandidates, newAlreadyFound);
				}

				alreadyFound.add(candidate);
				potentialClique.remove(candidate);
			}
		}



		return null; 

	}

	private boolean end(List<DiscoveredActivity> candidates, List<DiscoveredActivity> alreadyFound) {
		boolean end = false;
		int edgecounter;

		for (DiscoveredActivity found : alreadyFound) {
			edgecounter = 0;
			for (DiscoveredActivity candidate : candidates) {
				if (isNotcoNeighbor(found, candidate)) {
					edgecounter++;
				}
			}
			if (edgecounter == candidates.size()) {
				end = true;
			}
		}
		return end;
	}

	private boolean isNotcoNeighbor(DiscoveredActivity activityA, DiscoveredActivity activityB) {
		return activityRelationsMap.get(activityA).getMutualExclusion().contains(activityB);
	}

}
