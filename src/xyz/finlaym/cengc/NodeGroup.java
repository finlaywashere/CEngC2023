package xyz.finlaym.cengc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NodeGroup extends Group {

	private List<Node> nodes;
	private List<Node> connections;
	public NodeGroup(float cLat, float cLon, List<Node> nodes, List<Node> connections) {
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
			List<Node> cons = new ArrayList<Node>();
			for(Way w : ways) {
				boolean matches = false;
				Node n3 = null;
				for(Node n : group) {
					if(w.getNode1() == n || w.getNode2() == n) {
						matches = true;
						n3 = n;
						for(Node n2 : group) {
							if(n2 == n)
								continue;
							if((w.getNode1() == n && w.getNode2() == n2) || (w.getNode2() == n && w.getNode1() == n2)) {
								matches = false;
								n3 = null;
								break;
							}
						}
					}
				}
				if(matches) {
					cons.add(w.getNode1() == n3 ? w.getNode2() : w.getNode1());
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
			for(Way w : ways) {
				if((w.getNode1() == n2 && w.getNode2() == node) || (w.getNode2() == n2 && w.getNode1() == node)) {
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
			for(Node n2 : connections) {
				if(n == n2)
					return true;
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
	
}
