package xyz.finlaym.cengc;

import java.util.List;

public class NodeGroup {
	private List<Node> nodes;
	private List<Node> sharingNodes;
	public NodeGroup(List<Node> nodes, List<Node> sharingNodes) {
		this.nodes = nodes;
		this.sharingNodes = sharingNodes;
	}
	public List<Node> getNodes() {
		return nodes;
	}
	public List<Node> getSharingNodes() {
		return sharingNodes;
	}
}
