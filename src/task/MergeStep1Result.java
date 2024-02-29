package task;

import java.util.Set;
import java.util.TreeSet;

import model.TransitionNode;

public class MergeStep1Result {

	private Set<TransitionNode> step1MainTransitions = new TreeSet<TransitionNode>();
	private int nodeCount;
	
	public MergeStep1Result() {
	}
	
	public Set<TransitionNode> getStep1MainTransitions() {
		return step1MainTransitions;
	}
	public void addStep1MainTransition(TransitionNode fragmentMainTransition) {
		this.step1MainTransitions.add(fragmentMainTransition);
	}
	
	public int getNodeCount() {
		return nodeCount;
	}
	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}
	
}
