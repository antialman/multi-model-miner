package data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ActivityRelationsContainer {

	private DiscoveredActivity activity;

	//Succession, Precedence and Response constraints after pruning (needed for PN patterns)
	private Map<DiscoveredActivity, DiscoveredConstraint> succPrunedInMap = new HashMap<DiscoveredActivity, DiscoveredConstraint>();
	private Map<DiscoveredActivity, DiscoveredConstraint> succPrunedOutMap = new HashMap<DiscoveredActivity, DiscoveredConstraint>();
	private Map<DiscoveredActivity, DiscoveredConstraint> precPrunedInMap = new HashMap<DiscoveredActivity, DiscoveredConstraint>();
	private Map<DiscoveredActivity, DiscoveredConstraint> precPrunedOutMap = new HashMap<DiscoveredActivity, DiscoveredConstraint>();
	private Map<DiscoveredActivity, DiscoveredConstraint> respPrunedInMap = new HashMap<DiscoveredActivity, DiscoveredConstraint>();
	private Map<DiscoveredActivity, DiscoveredConstraint> respPrunedOutMap = new HashMap<DiscoveredActivity, DiscoveredConstraint>();

	//Co-Existence constraints before pruning (needed for PN patterns and determining activity order)
	private Map<DiscoveredActivity, DiscoveredConstraint> coexAllMap = new HashMap<DiscoveredActivity, DiscoveredConstraint>();
	private Map<DiscoveredActivity, DiscoveredConstraint> notCoexAllMap = new HashMap<DiscoveredActivity, DiscoveredConstraint>();
	
	//Not Succession constraints (needed for determining activity order)
	private Map<DiscoveredActivity, DiscoveredConstraint> notSuccAllInMap = new HashMap<DiscoveredActivity, DiscoveredConstraint>();
	private Map<DiscoveredActivity, DiscoveredConstraint> notSuccAllOutMap = new HashMap<DiscoveredActivity, DiscoveredConstraint>();
	
	//Pruned incoming and outgoing activities for easier lookup (populated by other add methods)
	private Set<DiscoveredActivity> prunedInActivities = new HashSet<DiscoveredActivity>();
	private Set<DiscoveredActivity> prunedOutActivities = new HashSet<DiscoveredActivity>();
	

	public ActivityRelationsContainer(DiscoveredActivity discoveredActivity) {
		this.activity = discoveredActivity;
	}
	
	public DiscoveredActivity getActivity() {
		return activity;
	}
	
	//Adding constraint relations to the container
	public void addSuccPrunedIn(DiscoveredActivity discoveredActivity, DiscoveredConstraint discoveredConstraint) {
		succPrunedInMap.put(discoveredActivity, discoveredConstraint);
		prunedInActivities.add(discoveredActivity);
	}
	public void addSuccPrunedOut(DiscoveredActivity discoveredActivity, DiscoveredConstraint discoveredConstraint) {
		succPrunedOutMap.put(discoveredActivity, discoveredConstraint);
		prunedOutActivities.add(discoveredActivity);
	}
	public void addPrecPrunedIn(DiscoveredActivity discoveredActivity, DiscoveredConstraint discoveredConstraint) {
		precPrunedInMap.put(discoveredActivity, discoveredConstraint);
		prunedInActivities.add(discoveredActivity);
	}
	public void addPrecPrunedOut(DiscoveredActivity discoveredActivity, DiscoveredConstraint discoveredConstraint) {
		precPrunedOutMap.put(discoveredActivity, discoveredConstraint);
		prunedOutActivities.add(discoveredActivity);
	}
	public void addRespPrunedIn(DiscoveredActivity discoveredActivity, DiscoveredConstraint discoveredConstraint) {
		respPrunedInMap.put(discoveredActivity, discoveredConstraint);
		prunedInActivities.add(discoveredActivity);
	}
	public void addRespPrunedOut(DiscoveredActivity discoveredActivity, DiscoveredConstraint discoveredConstraint) {
		respPrunedOutMap.put(discoveredActivity, discoveredConstraint);
		prunedOutActivities.add(discoveredActivity);
	}
	public void addCoex(DiscoveredActivity discoveredActivity, DiscoveredConstraint discoveredConstraint) {
		coexAllMap.put(discoveredActivity, discoveredConstraint);
	}
	public void addNotCoex(DiscoveredActivity discoveredActivity, DiscoveredConstraint discoveredConstraint) {
		notCoexAllMap.put(discoveredActivity, discoveredConstraint);
	}
	public void addNotSuccAllIn(DiscoveredActivity discoveredActivity, DiscoveredConstraint discoveredConstraint) {
		notSuccAllInMap.put(discoveredActivity, discoveredConstraint);
	}
	public void addNotSuccAllOut(DiscoveredActivity discoveredActivity, DiscoveredConstraint discoveredConstraint) {
		notSuccAllOutMap.put(discoveredActivity, discoveredConstraint);
	}
	
	
	//Getting activities that have a specific relation to this.activity
	public Set<DiscoveredActivity> getSuccPrunedInActivities() {
		return succPrunedInMap.keySet();
	}
	public Set<DiscoveredActivity> getSuccPrunedOutActivities() {
		return succPrunedOutMap.keySet();
	}
	public Set<DiscoveredActivity> getPrecPrunedInActivities() {
		return precPrunedInMap.keySet();
	}
	public Set<DiscoveredActivity> getPrecPrunedOutActivities() {
		return precPrunedOutMap.keySet();
	}
	public Set<DiscoveredActivity> getRespPrunedInActivities() {
		return respPrunedInMap.keySet();
	}
	public Set<DiscoveredActivity> getRespPrunedOutActivities() {
		return respPrunedOutMap.keySet();
	}
	public Set<DiscoveredActivity> getCoexActivities() {
		return coexAllMap.keySet();
	}
	public Set<DiscoveredActivity> getNotCoexActivities() {
		return notCoexAllMap.keySet();
	}
	public Set<DiscoveredActivity> getNotSuccAllInActivities() {
		return notSuccAllInMap.keySet();
	}
	public Set<DiscoveredActivity> getNotSuccAllOutActivities() {
		return notSuccAllOutMap.keySet();
	}
	
	
	//Getting the constraint object representing the specific relation between this.activity and discoveredActivity
	public DiscoveredConstraint getSuccPrunedInConstraint(DiscoveredActivity discoveredActivity) {
		return succPrunedInMap.get(discoveredActivity);
	}
	public DiscoveredConstraint getSuccPrunedOutConstraint(DiscoveredActivity discoveredActivity) {
		return succPrunedOutMap.get(discoveredActivity);
	}
	public DiscoveredConstraint getPrecPrunedInConstraint(DiscoveredActivity discoveredActivity) {
		return precPrunedInMap.get(discoveredActivity);
	}
	public DiscoveredConstraint getPrecPrunedOutConstraint(DiscoveredActivity discoveredActivity) {
		return precPrunedOutMap.get(discoveredActivity);
	}
	public DiscoveredConstraint getRespPrunedInConstraint(DiscoveredActivity discoveredActivity) {
		return respPrunedInMap.get(discoveredActivity);
	}
	public DiscoveredConstraint getRespPrunedOutConstraint(DiscoveredActivity discoveredActivity) {
		return respPrunedOutMap.get(discoveredActivity);
	}
	
	public DiscoveredConstraint getCoexConstraint(DiscoveredActivity discoveredActivity) {
		return coexAllMap.get(discoveredActivity);
	}
	public DiscoveredConstraint getNotCoexConstraint(DiscoveredActivity discoveredActivity) {
		return notCoexAllMap.get(discoveredActivity);
	}
	
	
	//Getting pruned incoming and outgoing activities
	public Set<DiscoveredActivity> getPrunedInActivities() {
		return prunedInActivities;
	}
	public Set<DiscoveredActivity> getOutPrunedActivities() {
		return prunedOutActivities;
	}
	
	@Override
	public String toString() {
		return "Relations of " + activity.getActivityName() + " - prunedInActivities: " + prunedInActivities .size() + ", prunedOutActivities: " + prunedOutActivities .size() + 
				", succPrunedInMap: " + succPrunedInMap.size() + ", succPrunedOutMap: " + succPrunedOutMap.size() + 
				", precPrunedInMap: " + precPrunedInMap.size() + ", precPrunedOutMap: " + precPrunedOutMap.size() +
				", respPrunedInMap: " + respPrunedInMap.size() + ", respPrunedOutMap: " + respPrunedOutMap.size() + 
				", coexAllMap: " + coexAllMap.size() + ", notCoexAllMap: " + notCoexAllMap.size() + 
				", notSuccAllInMap: " + notSuccAllInMap.size() + ", notSuccAllOutMap: " + notSuccAllOutMap.size();
	}
	
}
