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
	private Set<DiscoveredActivity> potentialClosestFollowers = new LinkedHashSet<DiscoveredActivity>();
	private Set<DiscoveredActivity> potentialClosestPreceders = new LinkedHashSet<DiscoveredActivity>();
	
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
	
	
	public void addPotentialClosestFollower(DiscoveredActivity potentialClosestFollower) {
		potentialClosestFollowers.add(potentialClosestFollower);
	}
	public Set<DiscoveredActivity> getPotentialClosestFollowers() {
		return potentialClosestFollowers;
	}
	
	
	public void addPotentialClosestPreceder(DiscoveredActivity potentialClosestPreceder) {
		potentialClosestPreceders.add(potentialClosestPreceder);
	}
	public Set<DiscoveredActivity> getPotentialClosestPreceders() {
		return potentialClosestPreceders;
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
