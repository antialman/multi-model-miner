package data.v3;

import java.util.LinkedHashSet;
import java.util.Set;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;

public class ActivityRelationsContainer {
	
	private DiscoveredActivity activity;
	
	//All followers/preceders of this activity
	private Set<DiscoveredActivity> allFollowerActivities = new LinkedHashSet<DiscoveredActivity>();
	private Set<DiscoveredActivity> allPrecederActivities = new LinkedHashSet<DiscoveredActivity>();
	
	//Potential closest followers/preceders of this activity based on constraints
	private Set<DiscoveredActivity> potentialNextActivities = new LinkedHashSet<DiscoveredActivity>(); //Activities that can be executed next
	private Set<DiscoveredActivity> potentialNextDecisions = new LinkedHashSet<DiscoveredActivity>(); //Activities that require an execution decision next (subset of potentialNextExecutions)
	private Set<DiscoveredActivity> potentialPrevActivities = new LinkedHashSet<DiscoveredActivity>();
	private Set<DiscoveredActivity> potentialPrevDecisions = new LinkedHashSet<DiscoveredActivity>();
	
	//Directional constraints on this activity
	private Set<DiscoveredConstraint> constraintsToFollowers = new LinkedHashSet<DiscoveredConstraint>();
	private Set<DiscoveredConstraint> constraintsFromPreceders = new LinkedHashSet<DiscoveredConstraint>();
	
	//Directional constraints between the followers/preceders of this activity
	private Set<DiscoveredConstraint> constraintsAmongFollowers = new LinkedHashSet<DiscoveredConstraint>();
	private Set<DiscoveredConstraint> constraintsAmongPreceders = new LinkedHashSet<DiscoveredConstraint>();
	
	
	
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
	
	
	public void addPrecederActivity(DiscoveredActivity precederActivity) {
		allPrecederActivities.add(precederActivity);
	}
	public Set<DiscoveredActivity> getAllPrecederActivities() {
		return allPrecederActivities;
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
	
	
	public void addConstraintFromPreceder(DiscoveredConstraint constraintFromPreceder) {
		constraintsFromPreceders.add(constraintFromPreceder);
	}
	public Set<DiscoveredConstraint> getConstraintsFromPreceders() {
		return constraintsFromPreceders;
	}
	
	
	public void addConstraintAmongFollowers(DiscoveredConstraint constraintAmongFollowers) {
		constraintsAmongFollowers.add(constraintAmongFollowers);
	}
	public Set<DiscoveredConstraint> getConstraintsAmongFollowers() {
		return constraintsAmongFollowers;
	}
	
	
	public void addConstraintAmongPreceders(DiscoveredConstraint constraintAmongPreceders) {
		constraintsAmongPreceders.add(constraintAmongPreceders);
	}
	public Set<DiscoveredConstraint> getConstraintsAmongPreceders() {
		return constraintsAmongPreceders;
	}
	
}
