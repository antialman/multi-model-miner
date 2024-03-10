package task.v1;

import java.util.Set;
import java.util.TreeSet;

import model.v1.TransitionNode;

public class InitialFragmentsResult {

	private Set<TransitionNode> fragmentMainTransitions = new TreeSet<TransitionNode>();
	private int nodeCount;
	
	public InitialFragmentsResult() {
	}
	
	public Set<TransitionNode> getFragmentMainTransitions() {
		return fragmentMainTransitions;
	}
	public void addFragmentMainTransition(TransitionNode fragmentMainTransition) {
		this.fragmentMainTransitions.add(fragmentMainTransition);
	}
	
	public int getNodeCount() {
		return nodeCount;
	}
	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}
	
}
