package data;

import java.util.HashSet;
import java.util.Set;

public class ActivityRelations {

	private DiscoveredActivity activity;
	
	private Set<DiscoveredActivity> responseOut;
	private Set<DiscoveredActivity> responseIn;
	private Set<DiscoveredActivity> precedenceOut;
	private Set<DiscoveredActivity> precedenceIn;
	private Set<DiscoveredActivity> mutualExclusion;
	
	public ActivityRelations(DiscoveredActivity discoveredActivity) {
		this.activity = discoveredActivity;
		
		responseOut = new HashSet<DiscoveredActivity>();
		responseIn = new HashSet<DiscoveredActivity>();
		precedenceOut = new HashSet<DiscoveredActivity>();
		precedenceIn = new HashSet<DiscoveredActivity>();
		mutualExclusion = new HashSet<DiscoveredActivity>();
	}
	
	public DiscoveredActivity getActivity() {
		return activity;
	}

	public void addResponseOut(DiscoveredActivity activity) {
		responseOut.add(activity);
	}
	public void addResponseIn(DiscoveredActivity activity) {
		responseIn.add(activity);
	}
	public void addPrecedenceOut(DiscoveredActivity activity) {
		precedenceOut.add(activity);
	}
	public void addPrecedenceIn(DiscoveredActivity activity) {
		precedenceIn.add(activity);
	}
	public void addMutualExclusion(DiscoveredActivity discoveredActivity) {
		mutualExclusion.add(activity);
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
	
	
}
