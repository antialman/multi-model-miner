package task;

import java.util.List;
import java.util.Map;
import data.ActivityRelations;
import data.DiscoveredActivity;
import model.TransitionNode;

public class InitialFragments {
	
	private Map<DiscoveredActivity, ActivityRelations> activityRelationsMap;
	private List<TransitionNode> fragmentMainTransitions;
	

	public InitialFragments() {
	}
	
	public void setActivityRelationsMap(Map<DiscoveredActivity, ActivityRelations> activityRelationsMap) {
		this.activityRelationsMap = activityRelationsMap;
	}
	public Map<DiscoveredActivity, ActivityRelations> getActivityRelationsMap() {
		return activityRelationsMap;
	}
	
	public void setFragmentMainTransitions(List<TransitionNode> fragmentMainTransitions) {
		this.fragmentMainTransitions = fragmentMainTransitions;
	}
	public List<TransitionNode> getFragmentMainTransitions() {
		return fragmentMainTransitions;
	}
}
