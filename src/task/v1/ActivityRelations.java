package task.v1;

import java.util.HashSet;
import java.util.Set;

import data.DiscoveredActivity;

public class ActivityRelations {

	private DiscoveredActivity activity;
	
	private Set<DiscoveredActivity> successionOut; //Technically not needed, added for completeness
	private Set<DiscoveredActivity> successionIn; //Technically not needed, added for completeness
	private Set<DiscoveredActivity> responseOut;
	private Set<DiscoveredActivity> responseIn;
	private Set<DiscoveredActivity> precedenceOut;
	private Set<DiscoveredActivity> precedenceIn;
	private Set<DiscoveredActivity> mutualExclusion;
	
	public ActivityRelations(DiscoveredActivity discoveredActivity) {
		this.activity = discoveredActivity;
		
		successionOut = new HashSet<DiscoveredActivity>();
		successionIn = new HashSet<DiscoveredActivity>();
		responseOut = new HashSet<DiscoveredActivity>();
		responseIn = new HashSet<DiscoveredActivity>();
		precedenceOut = new HashSet<DiscoveredActivity>();
		precedenceIn = new HashSet<DiscoveredActivity>();
		mutualExclusion = new HashSet<DiscoveredActivity>();
	}
	
	public DiscoveredActivity getActivity() {
		return activity;
	}
	
	public void addSuccessionOut(DiscoveredActivity discoveredActivity) {
		successionOut.add(discoveredActivity);
	}
	public void addSuccessionIn(DiscoveredActivity discoveredActivity) {
		successionIn.add(discoveredActivity);
	}
	public void addResponseOut(DiscoveredActivity discoveredActivity) {
		responseOut.add(discoveredActivity);
	}
	public void addResponseIn(DiscoveredActivity discoveredActivity) {
		responseIn.add(discoveredActivity);
	}
	public void addPrecedenceOut(DiscoveredActivity discoveredActivity) {
		precedenceOut.add(discoveredActivity);
	}
	public void addPrecedenceIn(DiscoveredActivity discoveredActivity) {
		precedenceIn.add(discoveredActivity);
	}
	public void addMutualExclusion(DiscoveredActivity discoveredActivity) {
		mutualExclusion.add(discoveredActivity);
	}
	
	public boolean successionOutContains(DiscoveredActivity discoveredActivity) {
		return successionOut.contains(discoveredActivity);
	}
	public boolean successionInContains(DiscoveredActivity discoveredActivity) {
		return successionIn.contains(discoveredActivity);
	}
	public boolean responseOutContains(DiscoveredActivity discoveredActivity) {
		return responseOut.contains(discoveredActivity);
	}
	public boolean responseInContains(DiscoveredActivity discoveredActivity) {
		return responseIn.contains(discoveredActivity);
	}
	public boolean precedenceOutContains(DiscoveredActivity discoveredActivity) {
		return precedenceOut.contains(discoveredActivity);
	}
	public boolean precedenceInContains(DiscoveredActivity discoveredActivity) {
		return precedenceIn.contains(discoveredActivity);
	}
	public boolean mutualExclusionContains(DiscoveredActivity discoveredActivity) {
		return mutualExclusion.contains(discoveredActivity);
	}
	
	public Set<DiscoveredActivity> getSuccessionOut() {
		return successionOut;
	}
	public Set<DiscoveredActivity> getSuccessionIn() {
		return successionIn;
	}
	public Set<DiscoveredActivity> getResponseOut() {
		return responseOut;
	}
	public Set<DiscoveredActivity> getResponseIn() {
		return responseIn;
	}
	public Set<DiscoveredActivity> getPrecedenceIn() {
		return precedenceIn;
	}
	public Set<DiscoveredActivity> getPrecedenceOut() {
		return precedenceOut;
	}
	public Set<DiscoveredActivity> getMutualExclusion() {
		return mutualExclusion;
	}
	
	
}
