package model.v2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;

public class ModelFactory {
	//Only one possible start and end because both are artificially added to each trace
	 
	private int nextNodeId;
	
	private PlaceNode initialPlace;
	private TransitionNode firstTransition; //Reference for starting Petri net construction and visualisation
	private TransitionNode lastTransition; //Just in case
	private PlaceNode finalPlace;
	
	private Set<DiscoveredActivity> unProcessedActivities = new HashSet<DiscoveredActivity>();
	private Map<DiscoveredActivity, TransitionNode> activityTransitionMap = new HashMap<DiscoveredActivity, TransitionNode>();
	
	private Set<TransitionNode> skipStartTransitions = new HashSet<TransitionNode>();
	private Set<TransitionNode> skipEndTransitions = new HashSet<TransitionNode>();
	
	public ModelFactory() {
		this.nextNodeId = 0;
		this.initialPlace = new PlaceNode(nextNodeId++, true, false); 
		this.finalPlace = new PlaceNode(nextNodeId++, false, true);
	}
	
	public void setArtificialStart(DiscoveredActivity artificialStartActivity) {
		firstTransition = getNewLabeledTransition(artificialStartActivity);
		initialPlace.addOutgoingTransition(firstTransition);
		unProcessedActivities.add(artificialStartActivity);
		activityTransitionMap.put(artificialStartActivity, firstTransition);
	}
	
	public void setArtificialEnd(DiscoveredActivity artificialEndActivity) {
		lastTransition = getNewLabeledTransition(artificialEndActivity);
		finalPlace.addIncomingTransition(lastTransition);
		unProcessedActivities.add(artificialEndActivity);
		activityTransitionMap.put(artificialEndActivity, lastTransition);
	}
	
	public TransitionNode getArtificialStartTransition() {
		return firstTransition;
	}
	
	public boolean hasUnProcessedActivities() {
		return !unProcessedActivities.isEmpty();
	}
	public Set<DiscoveredActivity> getUnProcessedActivities() {
		return unProcessedActivities;
	}
	public void markActivityAsProcessed(DiscoveredActivity discoveredActivity) {
		unProcessedActivities.remove(discoveredActivity);
	}
	
	public boolean hasOutTransitions(DiscoveredActivity discoveredActivity) {
		return !activityTransitionMap.get(discoveredActivity).getOutgoingPlaces().isEmpty();
	}
	
	
	//Handles sequences, AND-splits, AND-joins
	//TODO: Improve the case where AND-join is immediately followed by AND-split 
	public void addMandatoryFollower(DiscoveredActivity fromActivity, Set<DiscoveredActivity> toActivities) {
		TransitionNode fromActivityTransition = activityTransitionMap.get(fromActivity);
		for (DiscoveredActivity toActivity : toActivities) {
			PlaceNode followerOutPlace = getNewPlace();
			fromActivityTransition.addOutgoingPlace(followerOutPlace);
			if (!activityTransitionMap.containsKey(toActivity)) {
				activityTransitionMap.put(toActivity, getNewLabeledTransition(toActivity));
				unProcessedActivities.add(toActivity);
			}
			followerOutPlace.addOutgoingTransition(activityTransitionMap.get(toActivity));
		}
	}
	
	
	public void addXorSplit(DiscoveredActivity fromActivity, Set<DiscoveredActivity> notcoOutClique, boolean required) {
		TransitionNode fromActivityTransition = activityTransitionMap.get(fromActivity);
		PlaceNode cliqueOutPlace = getNewPlace();
		fromActivityTransition.addOutgoingPlace(cliqueOutPlace);
		for (DiscoveredActivity notcoActivity : notcoOutClique) {
			if (!activityTransitionMap.containsKey(notcoActivity)) {
				activityTransitionMap.put(notcoActivity, getNewLabeledTransition(notcoActivity));
				unProcessedActivities.add(notcoActivity);
			}
			cliqueOutPlace.addOutgoingTransition(activityTransitionMap.get(notcoActivity));
		}
		if (!required) {
			TransitionNode notcoSkipStart = getNewSkipStart();
			cliqueOutPlace.addOutgoingTransition(notcoSkipStart);
			skipStartTransitions.add(notcoSkipStart);
		}
	}
	
	public void addXorJoin(DiscoveredActivity toActivity, Set<DiscoveredActivity> notcoInClique, boolean required) {
		TransitionNode toActivityTransition = activityTransitionMap.get(toActivity);
		PlaceNode cliqueInPlace = getNewPlace();
		toActivityTransition.addIncomingPlace(cliqueInPlace);
		for (DiscoveredActivity notcoActivity : notcoInClique) {
			if (!activityTransitionMap.containsKey(notcoActivity)) {
				activityTransitionMap.put(notcoActivity, getNewLabeledTransition(notcoActivity));
				unProcessedActivities.add(notcoActivity);
			}
			cliqueInPlace.addIncomingTransition(activityTransitionMap.get(notcoActivity));
		}
		if (!required) {
			TransitionNode notcoSkipEnd = getNewSkipEnd();
			cliqueInPlace.addIncomingTransition(notcoSkipEnd);
			skipEndTransitions.add(notcoSkipEnd);
		}
	}
	
	
	public void addOptionalAndSplit(DiscoveredActivity fromActivity, Set<DiscoveredActivity> coExOutClique) {
		TransitionNode fromActivityTransition = activityTransitionMap.get(fromActivity);
		PlaceNode cliqueOutPlace = getNewPlace();
		fromActivityTransition.addOutgoingPlace(cliqueOutPlace);
		TransitionNode coexSkipStart = getNewSkipStart();
		cliqueOutPlace.addOutgoingTransition(coexSkipStart);
		skipStartTransitions.add(coexSkipStart);
		TransitionNode routingTransition = getNewRoutingTransition();
		cliqueOutPlace.addOutgoingTransition(routingTransition);
		
		for (DiscoveredActivity coExActivity : coExOutClique) {
			if (!activityTransitionMap.containsKey(coExActivity)) {
				activityTransitionMap.put(coExActivity, getNewLabeledTransition(coExActivity));
				unProcessedActivities.add(coExActivity);
			}
			PlaceNode coExActivityPlace = getNewPlace();
			routingTransition.addOutgoingPlace(coExActivityPlace);
			coExActivityPlace.addOutgoingTransition(activityTransitionMap.get(coExActivity));
		}
	}
	
	public void addOptionalAndJoin(DiscoveredActivity toActivity, Set<DiscoveredActivity> coExInClique) {
		TransitionNode toActivityTransition = activityTransitionMap.get(toActivity);
		PlaceNode cliqueInPlace = getNewPlace();
		toActivityTransition.addIncomingPlace(cliqueInPlace);
		TransitionNode coexSkipEnd = getNewSkipEnd();
		cliqueInPlace.addIncomingTransition(coexSkipEnd);
		skipEndTransitions.add(coexSkipEnd);
		TransitionNode routingTransition = getNewRoutingTransition();
		cliqueInPlace.addIncomingTransition(routingTransition);
		
		for (DiscoveredActivity coExActivity : coExInClique) {
			if (!activityTransitionMap.containsKey(coExActivity)) {
				activityTransitionMap.put(coExActivity, getNewLabeledTransition(coExActivity));
				unProcessedActivities.add(coExActivity);
			}
			PlaceNode coExActivityPlace = getNewPlace();
			routingTransition.addIncomingPlace(coExActivityPlace);
			coExActivityPlace.addIncomingTransition(activityTransitionMap.get(coExActivity));
		}
	}
	
	
	public void addOptionalNextActivity(DiscoveredActivity currActivity, DiscoveredActivity nextActivity) {
		TransitionNode currActivityTransition = activityTransitionMap.get(currActivity);
		PlaceNode nextOutPlace = getNewPlace();
		currActivityTransition.addOutgoingPlace(nextOutPlace);
		TransitionNode nextActivitySkipStart = getNewSkipStart();
		nextOutPlace.addOutgoingTransition(nextActivitySkipStart);
		
		if (!activityTransitionMap.containsKey(nextActivity)) {
			activityTransitionMap.put(nextActivity, getNewLabeledTransition(nextActivity));
			unProcessedActivities.add(nextActivity);
		}
		nextOutPlace.addOutgoingTransition(activityTransitionMap.get(nextActivity));
	}
	
	
	public void addOptionalPreviousActivity(DiscoveredActivity currActivity, DiscoveredActivity previousActivity) {
		TransitionNode currActivityTransition = activityTransitionMap.get(currActivity);
		PlaceNode previousOutPlace = getNewPlace();
		currActivityTransition.addIncomingPlace(previousOutPlace);
		TransitionNode previousActivitySkipEnd = getNewSkipEnd();
		previousOutPlace.addIncomingTransition(previousActivitySkipEnd);
		
		if (!activityTransitionMap.containsKey(previousActivity)) {
			activityTransitionMap.put(previousActivity, getNewLabeledTransition(previousActivity));
			unProcessedActivities.add(previousActivity);
		}
		previousOutPlace.addIncomingTransition(activityTransitionMap.get(previousActivity));
	}
	
	//Handling of special cases where ordering constraints were unable to detect fixed order between XOR structures and/or skippable activities
	public void addFollowersOfIncompleteClique(Set<DiscoveredActivity> notcoClique,	Set<DiscoveredActivity> notcoOutClique, float notcoCliqueSupport, float notcoOutCliqueSupport) {
		PlaceNode outPlace = getNewPlace();
		for (DiscoveredActivity incompleteActivity : notcoClique) {
			activityTransitionMap.get(incompleteActivity).addOutgoingPlace(outPlace);
		}
		
		for (DiscoveredActivity notcoOutActivity : notcoOutClique) {
			if (!activityTransitionMap.containsKey(notcoOutActivity)) {
				activityTransitionMap.put(notcoOutActivity, getNewLabeledTransition(notcoOutActivity));
				unProcessedActivities.add(notcoOutActivity);
			}
			outPlace.addOutgoingTransition(activityTransitionMap.get(notcoOutActivity));
		}
		
		if (notcoCliqueSupport < notcoOutCliqueSupport) {
			TransitionNode skipEndTransition = getNewSkipEnd();
			outPlace.addIncomingTransition(skipEndTransition);
		} else if (notcoCliqueSupport > notcoOutCliqueSupport) {
			TransitionNode skipStartTransition = getNewSkipStart();
			outPlace.addOutgoingTransition(skipStartTransition);
		}
	}
	
	
	public void addUnprocessedActivity(DiscoveredActivity respOutActivity) {
		if (!activityTransitionMap.containsKey(respOutActivity)) {
			activityTransitionMap.put(respOutActivity, getNewLabeledTransition(respOutActivity));
			unProcessedActivities.add(respOutActivity);
		}
	}
	


	public Set<DiscoveredActivity> getIncompleteActivities() {
		//TODO: Improve it by either tracking incomplete activities explicitly, or by finding if there are unprocessed relations to any closest activities
		Set<DiscoveredActivity> incompleteActivities = new HashSet<DiscoveredActivity>();
		activityTransitionMap.forEach((discoveredActivity, transitionNode) -> {
			if (transitionNode.getOutgoingPlaces().isEmpty()) {
				incompleteActivities.add(discoveredActivity);
			}
		});
		return incompleteActivities;
	}
	
	
	
	//Private methods to create new nodes
	private TransitionNode getNewLabeledTransition(DiscoveredActivity da) {
		return new TransitionNode(nextNodeId++, da);
	}
	private TransitionNode getNewRoutingTransition() {
		return new TransitionNode(nextNodeId++);
	}
	private TransitionNode getNewSkipStart() {
		return new TransitionNode(nextNodeId++, true, false);
	}
	private TransitionNode getNewSkipEnd() {
		return new TransitionNode(nextNodeId++, false, true);
	}
	
	private PlaceNode getNewPlace() {
		return new PlaceNode(nextNodeId++, false, false);
	}

}
