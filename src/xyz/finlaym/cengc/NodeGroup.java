package xyz.finlaym.cengc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NodeGroup extends Group {

	private List<Node> nodes;
	private Map<Node,List<Node>> connections;
	public NodeGroup(float cLat, float cLon, List<Node> nodes, Map<Node,List<Node>> connections) {
		super(cLat, cLon);
		this.nodes = nodes;
		this.connections = connections;
	}

	@Override
	public Set<Group> group(List<Group> existingGroups, List<GroupWay> ways) {
		return null;
	}
	public static Set<Group> groupNodes(List<Node> nodes, List<Way> ways){
		Set<Group> groups = new HashSet<Group>();
		while(nodes.size() > 0) {
			System.out.println(nodes.size());
			Node seed = nodes.get(0);
			nodes.remove(0);
			List<Node> group = new ArrayList<Node>();
			group.add(seed);
			for(int i = 0; i < Group.MAX_GROUP_SIZE; i++) {
				if(group.size() >= Group.MAX_GROUP_SIZE)
					break;
				Node nearest = findNearest(nodes, group.get(group.size()-1), ways);
				if(nearest == null) {
					break;
				}
				group.add(nearest);
				nodes.remove(nearest);
			}
			float avgLon = 0, avgLat = 0;
			for(Node n : group) {
				avgLon += n.getLon();
				avgLat += n.getLat();
			}
			avgLon /= group.size();
			avgLat /= group.size();
			Map<Node,List<Node>> cons = new HashMap<Node,List<Node>>();
			for(Node n : group) {
				for(Way w : n.getWays()) {
					List<Node> nL = cons.get(n);
					if(nL == null)
						nL = new ArrayList<Node>();
					nL.add(w.getNode1() == n ? w.getNode2() : w.getNode1());
					cons.put(n, nL);
				}
			}
			NodeGroup g = new NodeGroup(avgLat, avgLon, group, cons);
			groups.add(g);
		}
		return groups;
	}
	public static Node findNearest(List<Node> nodes, Node node, List<Way> ways) {
		float dist = Float.MAX_VALUE;
		Node n = null;
		for(Node n2 : nodes) {
			boolean connected = false;
			for(Way w : node.getWays()) {
				if(w.getNode1() == n2 || w.getNode2() == n2) {
					connected = true;
					break;
				}
			}
			if(!connected)
				continue;
			float dist2 = (float) Math.sqrt(Math.pow(n2.getLat()-node.getLat(), 2) + Math.pow(n2.getLon()-node.getLon(), 2));
			if(dist2 < dist) {
				dist = dist2;
				n = n2;
			}
		}
		return n;
	}

	@Override
	public boolean overlaps(Group group) {
		if(!(group instanceof NodeGroup))
			return false;
		NodeGroup g = (NodeGroup) group;
		for(Node n : g.nodes) {
			for(List<Node> n2l : connections.values()) {
				for(Node n2 : n2l) {
					if(n == n2)
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public String hash() {
		String hash = "";
		for(Node n : nodes) {
			hash += "-"+n.getId();
		}
		return hash;
	}

	@Override
	public float distance(Group group) {
		return (float) Math.sqrt(Math.pow(this.cLat-group.cLat, 2) + Math.pow(this.cLon-group.cLon, 2));
	}
	@Override
	public String toString() {
		String group = "";
		for(Node n : nodes) {
			group += "-" + n.toString();
		}
		return group;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public Map<Node,List<Node>> getConnections() {
		return connections;
	}
	public List<Node> navigateInternally(Node n1, Node n2, Map<Integer, List<Node>> ways){
		List<Node> path = new ArrayList<Node>();
		if(this.connections.containsKey(n1)) {
			List<Node> connections = this.connections.get(n1);
			if(connections.contains(n2)) {
				path.add(n1);
				path.add(n2);
				return path;
			}
			for(Node n : connections) {
				List<Node> connections2 = this.connections.get(n);
				if(connections2 == null)
					continue;
				if(connections2.contains(n2)) {
					path.add(n1);
					path.add(n);
					path.add(n2);
					return path;
				}
			}
			for(Node n : connections) {
				List<Node> connections2 = this.connections.get(n);
				if(connections2 == null)
					continue;
				for(Node n3 : connections2) {
					List<Node> connections3 = this.connections.get(n3);
					if(connections3 == null)
						continue;
					if(connections3.contains(n2)) {
						path.add(n1);
						path.add(n);
						path.add(n3);
						path.add(n2);
						return path;
					}
				}
			}
		}else if(this.connections.containsKey(n2)) {
			List<Node> connections = this.connections.get(n2);
			if(connections.contains(n1)) {
				path.add(n2);
				path.add(n1);
				return path;
			}
			for(Node n : connections) {
				List<Node> connections2 = this.connections.get(n);
				if(connections2.contains(n1)) {
					path.add(n2);
					path.add(n);
					path.add(n1);
					return path;
				}
			}
			for(Node n : connections) {
				List<Node> connections2 = this.connections.get(n);
				for(Node n3 : connections2) {
					List<Node> connections3 = this.connections.get(n3);
					if(connections3.contains(n1)) {
						path.add(n2);
						path.add(n);
						path.add(n3);
						path.add(n1);
						return path;
					}
				}
			}
		}
		return null;
	}
	
}
