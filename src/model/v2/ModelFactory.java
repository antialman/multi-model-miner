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
	private PlaceNode finalPlace;
	
	private Set<DiscoveredActivity> unProcessedActivities = new HashSet<DiscoveredActivity>();
	private Map<DiscoveredActivity, TransitionNode> activityTransitionsMap = new HashMap<DiscoveredActivity, TransitionNode>();
	
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
		activityTransitionsMap.put(artificialStartActivity, firstTransition);
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
	
	
	//Handles sequences, AND-splits, AND-joins
	//TODO: Improve the case where AND-join is immediately followed by AND-split 
	public void addMandatoryFollower(DiscoveredActivity fromActivity, Set<DiscoveredActivity> toActivities) {
		TransitionNode fromActivityTransition = activityTransitionsMap.get(fromActivity);
		for (DiscoveredActivity toActivity : toActivities) {
			PlaceNode followerOutPlace = getNewPlace();
			fromActivityTransition.addOutgoingPlace(followerOutPlace);
			if (!activityTransitionsMap.containsKey(toActivity)) {
				activityTransitionsMap.put(toActivity, getNewLabeledTransition(toActivity));
				unProcessedActivities.add(toActivity);
			}
			followerOutPlace.addOutgoingTransition(activityTransitionsMap.get(toActivity));
		}
	}
	
	
	public void addXorSplit(DiscoveredActivity fromActivity, Set<DiscoveredActivity> notcoOutClique, boolean required) {
		TransitionNode fromActivityTransition = activityTransitionsMap.get(fromActivity);
		PlaceNode cliqueOutPlace = getNewPlace();
		fromActivityTransition.addOutgoingPlace(cliqueOutPlace);
		for (DiscoveredActivity notcoActivity : notcoOutClique) {
			if (!activityTransitionsMap.containsKey(notcoActivity)) {
				activityTransitionsMap.put(notcoActivity, getNewLabeledTransition(notcoActivity));
				unProcessedActivities.add(notcoActivity);
			}
			cliqueOutPlace.addOutgoingTransition(activityTransitionsMap.get(notcoActivity));
		}
		if (!required) {
			TransitionNode notcoSkipStart = getNewSkipStart();
			cliqueOutPlace.addOutgoingTransition(notcoSkipStart);
			skipStartTransitions.add(notcoSkipStart);
		}
	}
	
	public void addXorJoin(DiscoveredActivity toActivity, Set<DiscoveredActivity> notcoInClique, boolean required) {
		TransitionNode toActivityTransition = activityTransitionsMap.get(toActivity);
		PlaceNode cliqueInPlace = getNewPlace();
		toActivityTransition.addIncomingPlace(cliqueInPlace);
		for (DiscoveredActivity notcoActivity : notcoInClique) {
			if (!activityTransitionsMap.containsKey(notcoActivity)) {
				activityTransitionsMap.put(notcoActivity, getNewLabeledTransition(notcoActivity));
				unProcessedActivities.add(notcoActivity);
			}
			cliqueInPlace.addIncomingTransition(activityTransitionsMap.get(notcoActivity));
		}
		if (!required) {
			TransitionNode notcoSkipEnd = getNewSkipEnd();
			cliqueInPlace.addIncomingTransition(notcoSkipEnd);
			skipEndTransitions.add(notcoSkipEnd);
		}
	}
	
	
	public void addOptionalAndSplit(DiscoveredActivity fromActivity, Set<DiscoveredActivity> coExOutClique) {
		TransitionNode fromActivityTransition = activityTransitionsMap.get(fromActivity);
		PlaceNode cliqueOutPlace = getNewPlace();
		fromActivityTransition.addOutgoingPlace(cliqueOutPlace);
		TransitionNode coexSkipStart = getNewSkipStart();
		cliqueOutPlace.addOutgoingTransition(coexSkipStart);
		skipStartTransitions.add(coexSkipStart);
		TransitionNode routingTransition = getNewRoutingTransition();
		cliqueOutPlace.addOutgoingTransition(routingTransition);
		
		for (DiscoveredActivity coExActivity : coExOutClique) {
			if (!activityTransitionsMap.containsKey(coExActivity)) {
				activityTransitionsMap.put(coExActivity, getNewLabeledTransition(coExActivity));
				unProcessedActivities.add(coExActivity);
			}
			PlaceNode coExActivityPlace = getNewPlace();
			routingTransition.addOutgoingPlace(coExActivityPlace);
			coExActivityPlace.addOutgoingTransition(activityTransitionsMap.get(coExActivity));
		}
	}
	
	public void addOptionalAndJoin(DiscoveredActivity toActivity, Set<DiscoveredActivity> coExInClique) {
		TransitionNode toActivityTransition = activityTransitionsMap.get(toActivity);
		PlaceNode cliqueInPlace = getNewPlace();
		toActivityTransition.addIncomingPlace(cliqueInPlace);
		TransitionNode coexSkipEnd = getNewSkipEnd();
		cliqueInPlace.addIncomingTransition(coexSkipEnd);
		skipEndTransitions.add(coexSkipEnd);
		TransitionNode routingTransition = getNewRoutingTransition();
		cliqueInPlace.addIncomingTransition(routingTransition);
		
		for (DiscoveredActivity coExActivity : coExInClique) {
			if (!activityTransitionsMap.containsKey(coExActivity)) {
				activityTransitionsMap.put(coExActivity, getNewLabeledTransition(coExActivity));
				unProcessedActivities.add(coExActivity);
			}
			PlaceNode coExActivityPlace = getNewPlace();
			routingTransition.addIncomingPlace(coExActivityPlace);
			coExActivityPlace.addIncomingTransition(activityTransitionsMap.get(coExActivity));
		}
	}
	
	
	public void addOptionalNextActivity(DiscoveredActivity currActivity, DiscoveredActivity nextActivity) {
		TransitionNode currActivityTransition = activityTransitionsMap.get(currActivity);
		PlaceNode nextOutPlace = getNewPlace();
		currActivityTransition.addOutgoingPlace(nextOutPlace);
		TransitionNode nextActivitySkipStart = getNewSkipStart();
		nextOutPlace.addOutgoingTransition(nextActivitySkipStart);
		
		if (!activityTransitionsMap.containsKey(nextActivity)) {
			activityTransitionsMap.put(nextActivity, getNewLabeledTransition(nextActivity));
			unProcessedActivities.add(nextActivity);
		}
		nextOutPlace.addOutgoingTransition(activityTransitionsMap.get(nextActivity));
	}
	
	
	public void addOptionalPreviousActivity(DiscoveredActivity currActivity, DiscoveredActivity previousActivity) {
		TransitionNode currActivityTransition = activityTransitionsMap.get(currActivity);
		PlaceNode previousOutPlace = getNewPlace();
		currActivityTransition.addIncomingPlace(previousOutPlace);
		TransitionNode previousActivitySkipEnd = getNewSkipEnd();
		previousOutPlace.addIncomingTransition(previousActivitySkipEnd);
		
		if (!activityTransitionsMap.containsKey(previousActivity)) {
			activityTransitionsMap.put(previousActivity, getNewLabeledTransition(previousActivity));
			unProcessedActivities.add(previousActivity);
		}
		previousOutPlace.addIncomingTransition(activityTransitionsMap.get(previousActivity));
	}
	
	
	public void addUnprocessedActivity(DiscoveredActivity respOutActivity) {
		if (!activityTransitionsMap.containsKey(respOutActivity)) {
			activityTransitionsMap.put(respOutActivity, getNewLabeledTransition(respOutActivity));
			unProcessedActivities.add(respOutActivity);
		}
	}
	
	
	
	public void connectArtificialEnd(DiscoveredActivity artificialEndActivity) {
		if (!activityTransitionsMap.containsKey(artificialEndActivity)) { //Can only be missing if model has been partially built
			activityTransitionsMap.put(artificialEndActivity, getNewLabeledTransition(artificialEndActivity));
		}
		finalPlace.addIncomingTransition(activityTransitionsMap.get(artificialEndActivity));
		
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
