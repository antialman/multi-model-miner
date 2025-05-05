package data.v3;

import java.util.LinkedHashSet;
import java.util.Set;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;

public class ActivityRelationsContainer {
	
	private DiscoveredActivity activity;
	
	//All followers/predecessors of this activity
	private Set<DiscoveredActivity> allFollowerActivities = new LinkedHashSet<DiscoveredActivity>();
	private Set<DiscoveredActivity> allPredecessorActivities = new LinkedHashSet<DiscoveredActivity>();
	
	//Potential closest followers/predecessors of this activity based on constraints
	private Set<DiscoveredActivity> potentialNextActivities = new LinkedHashSet<DiscoveredActivity>(); //Activities that can be executed next
	private Set<DiscoveredActivity> potentialNextDecisions = new LinkedHashSet<DiscoveredActivity>(); //Activities that require an execution decision next (subset of potentialNextExecutions)
	private Set<DiscoveredActivity> potentialPrevActivities = new LinkedHashSet<DiscoveredActivity>();
	private Set<DiscoveredActivity> potentialPrevDecisions = new LinkedHashSet<DiscoveredActivity>();
	
	//Directional constraints on this activity
	private Set<DiscoveredConstraint> constraintsToFollowers = new LinkedHashSet<DiscoveredConstraint>();
	private Set<DiscoveredConstraint> constraintsFromPredecessors = new LinkedHashSet<DiscoveredConstraint>();
	
	//Directional constraints between the followers/predecessors of this activity
	private Set<DiscoveredConstraint> constraintsAmongFollowers = new LinkedHashSet<DiscoveredConstraint>();
	private Set<DiscoveredConstraint> constraintsAmongPredecessors = new LinkedHashSet<DiscoveredConstraint>();
	
	
	
	public ActivityRelationsContainer(DiscoveredActivity discoveredActivity) {
		this.activity = discoveredActivity;
	}
	
	public DiscoveredActivity getActivity() {
		return activity;
	}

	
	public void addFollowerActivity(DiscoveredActivity followerActivity) {
		allFollowerActivities.add(followerActivity);
	}
	public Set<DiscoveredActivity> getAllFollowerActivities() {
		return allFollowerActivities;
	}
	
	
	public void addPredecessorActivity(DiscoveredActivity predecessorActivity) {
		allPredecessorActivities.add(predecessorActivity);
	}
	public Set<DiscoveredActivity> getAllPredecessorActivities() {
		return allPredecessorActivities;
	}
	
	
	public void addPotentialNextActivity(DiscoveredActivity potentialNextActivity) {
		potentialNextActivities.add(potentialNextActivity);
	}
	public Set<DiscoveredActivity> getPotentialNextActivities() {
		return potentialNextActivities;
	}
	public void addPotentialNextDecision(DiscoveredActivity potentialNextActivity) {
		potentialNextDecisions.add(potentialNextActivity);
	}
	public Set<DiscoveredActivity> getPotentialNextDecisions() {
		return potentialNextDecisions;
	}
	
	public void addPotentialPrevActivity(DiscoveredActivity potentialPrevActivity) {
		potentialPrevActivities.add(potentialPrevActivity);
	}
	public Set<DiscoveredActivity> getPotentialPrevActivities() {
		return potentialPrevActivities;
	}
	public void addPotentialPrevDecision(DiscoveredActivity potentialNextActivity) {
		potentialPrevDecisions.add(potentialNextActivity);
	}
	public Set<DiscoveredActivity> getPotentialPrevDecisions() {
		return potentialPrevDecisions;
	}
	
	
	
	public void addConstraintToFollower(DiscoveredConstraint constraintToFollower) {
		constraintsToFollowers.add(constraintToFollower);
	}
	public Set<DiscoveredConstraint> getConstraintsToFollowers() {
		return constraintsToFollowers;
	}
	
	
	public void addConstraintFromPredecessor(DiscoveredConstraint constraintFromPredecessor) {
		constraintsFromPredecessors.add(constraintFromPredecessor);
	}
	public Set<DiscoveredConstraint> getConstraintsFromPredecessors() {
		return constraintsFromPredecessors;
	}
	
	
	public void addConstraintAmongFollowers(DiscoveredConstraint constraintAmongFollowers) {
		constraintsAmongFollowers.add(constraintAmongFollowers);
	}
	public Set<DiscoveredConstraint> getConstraintsAmongFollowers() {
		return constraintsAmongFollowers;
	}
	
	
	public void addConstraintAmongPredecessors(DiscoveredConstraint constraintAmongPredecessors) {
		constraintsAmongPredecessors.add(constraintAmongPredecessors);
	}
	public Set<DiscoveredConstraint> getConstraintsAmongPredecessors() {
		return constraintsAmongPredecessors;
	}
	
}
