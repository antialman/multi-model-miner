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
	
	
	public void addXorSplit(DiscoveredActivity fromActivity, Set<DiscoveredActivity> notcoClique, boolean required) {
		TransitionNode fromActivityTransition = activityTransitionsMap.get(fromActivity);
		PlaceNode cliqueOutPlace = getNewPlace();
		fromActivityTransition.addOutgoingPlace(cliqueOutPlace);
		for (DiscoveredActivity notcoActivity : notcoClique) {
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
	
	
	public void addOptionalAndSplit(DiscoveredActivity fromActivity, Set<DiscoveredActivity> coExClique) {
		TransitionNode fromActivityTransition = activityTransitionsMap.get(fromActivity);
		PlaceNode cliqueOutPlace = getNewPlace();
		fromActivityTransition.addOutgoingPlace(cliqueOutPlace);
		TransitionNode coexSkipStart = getNewSkipStart();
		cliqueOutPlace.addOutgoingTransition(coexSkipStart);
		skipStartTransitions.add(coexSkipStart);
		TransitionNode routingTransition = getNewRoutingTransition();
		cliqueOutPlace.addOutgoingTransition(routingTransition);
		
		for (DiscoveredActivity coExActivity : coExClique) {
			if (!activityTransitionsMap.containsKey(coExActivity)) {
				activityTransitionsMap.put(coExActivity, getNewLabeledTransition(coExActivity));
				unProcessedActivities.add(coExActivity);
			}
			PlaceNode coExActivityPlace = getNewPlace();
			routingTransition.addOutgoingPlace(coExActivityPlace);
			coExActivityPlace.addOutgoingTransition(activityTransitionsMap.get(coExActivity));
		}
	}
	
	
	public void addOptionalFollower(DiscoveredActivity fromActivity, DiscoveredActivity toActivity) {
		TransitionNode fromActivityTransition = activityTransitionsMap.get(fromActivity);
		PlaceNode followerOutPlace = getNewPlace();
		fromActivityTransition.addOutgoingPlace(followerOutPlace);
		TransitionNode followerSkipStart = getNewSkipStart();
		followerOutPlace.addOutgoingTransition(followerSkipStart);
		
		if (!activityTransitionsMap.containsKey(toActivity)) {
			activityTransitionsMap.put(toActivity, getNewLabeledTransition(toActivity));
			unProcessedActivities.add(toActivity);
		}
		followerOutPlace.addOutgoingTransition(activityTransitionsMap.get(toActivity));
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
