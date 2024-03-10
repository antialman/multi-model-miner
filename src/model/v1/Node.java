package model.v1;

public class Node implements Comparable<Node>{
	protected int nodeId;

	Node(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public int getNodeId() {
		return nodeId;
	}

	@Override
	public int compareTo(Node otherNode) {
		//To enable usage in TreeSet
		return this.nodeId-otherNode.nodeId;
	}
}
