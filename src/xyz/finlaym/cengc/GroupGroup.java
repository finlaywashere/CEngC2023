package xyz.finlaym.cengc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupGroup extends Group {
	private List<Group> contained;
	private Map<Group,List<Group>> connections;
	
	public GroupGroup(float cLon, float cLat, List<Group> contained, Map<Group,List<Group>> connections) {
		super(cLon, cLat);
		this.contained = contained;
		this.connections = connections;
	}

	public static Set<Group> createGroup(List<Group> existingGroups, List<GroupWay> ways) {
		Set<Group> result = new HashSet<Group>();
		Map<Group, List<Group>> wayMap = new HashMap<Group, List<Group>>();
		for(GroupWay w : ways) {
			List<Group> ways1 = wayMap.get(w.getGroup1());
			if(ways1 == null)
				ways1 = new ArrayList<Group>();
			ways1.add(w.getGroup2());
			wayMap.put(w.getGroup1(), ways1);
			List<Group> ways2 = wayMap.get(w.getGroup2());
			if(ways2 == null)
				ways2 = new ArrayList<Group>();
			ways2.add(w.getGroup1());
			wayMap.put(w.getGroup2(), ways2);
		}
		while(existingGroups.size() > 0) {
			System.out.println(existingGroups.size());
			Group seed = existingGroups.get(0);
			existingGroups.remove(0);
			List<Group> groups = new ArrayList<Group>();
			groups.add(seed);
			for(int i = 0; i < Group.MAX_GROUP_SIZE; i++) {
				if(groups.size() >= Group.MAX_GROUP_SIZE)
					break;
				Group next = findNearest(existingGroups, seed);
				if(next == null)
					break;
				existingGroups.remove(next);
				groups.add(next);
			}
			float avgLat = 0, avgLon = 0;
			for(Group g : groups) {
				avgLat += g.cLat;
				avgLon += g.cLon;
			}
			avgLat /= groups.size();
			avgLon /= groups.size();
			Map<Group,List<Group>> cons = new HashMap<Group, List<Group>>();
			for(Group g : groups) {
				for(Group way : wayMap.get(g)) {
					List<Group> gL = cons.get(g);
					if(gL == null)
						gL = new ArrayList<Group>();
					gL.add(way);
					cons.put(g, gL);
				}
			}
			GroupGroup gg = new GroupGroup(avgLon, avgLat, groups, cons);
			result.add(gg);
		}
		return result;
	}
	private static Group findNearest(List<Group> groups, Group group) {
		float dist = Float.MAX_VALUE;
		Group near = null;
		for(Group g : groups) {
			if(g == group)
				continue;
			float d = g.distance(group);
			if(d < dist && g.overlaps(group)) {
				near = g;
				dist = d;
			}
		}
		return near;
	}
	public List<Node> nodeNavInternally(Group g1, Group g2, Map<Integer, List<Node>> ways){
		List<Group> groups = navigateInternally(g1, g2);
		List<Node> path = new ArrayList<Node>();
		Node last = null;
		NodeGroup lastG = null;
		for(int i = 0; i < groups.size()-1; i++) {
			Group g3 = groups.get(i);
			Group g4 = groups.get(i+1);
			if(g3 instanceof NodeGroup) {
				NodeGroup n1 = (NodeGroup) g3;
				NodeGroup n2 = (NodeGroup) g4;
				boolean found = false;
				for(Node start : n1.getConnections().keySet()) {
					if(found)
						break;
					List<Node> connections = n1.getConnections().get(start);
					for(Node end : n2.getNodes()) {
						if(connections.contains(end)) {
							if(last != null) {
								path.addAll(lastG.navigateInternally(last, start, ways));
							}
							path.add(start);
							path.add(end);
							last = end;
							lastG = n2;
							found = true;
							break;
						}
					}
				}
			} else {
				/*GroupGroup n1 = (GroupGroup) g3;
				GroupGroup n2 = (GroupGroup) g4;
				boolean found = false;
				for(Group start : n1.getConnections().keySet()) {
					for(Group g5 : n1.getConnections().get(start)) {
						for(Group end : n2.getContained()) {
							if(g5 == end) {
								path.add();
							}
						}
					}
				}*/
				
			}
		}
		return path;
	}
	public List<Group> navigateInternally(Group g1, Group g2){
		List<Group> path = new ArrayList<Group>();
		if(connections.containsKey(g1)) {
			if(connections.get(g1).contains(g2)) {
				path.add(g1);
				path.add(g2);
				return path;
			}
		} else {
			if(connections.get(g2).contains(g1)) {
				path.add(g2);
				path.add(g1);
				return path;
			}
		}
		for(Group g : connections.keySet()) {
			List<Group> g3l = connections.get(g);
			for(Group g3 : g3l) {
				if(connections.get(g3).contains(g2)) {
					path.add(g1);
					path.add(g3);
					path.add(g2);
					return path;
				}
				if(connections.get(g3).contains(g1)) {
					path.add(g2);
					path.add(g3);
					path.add(g1);
					return path;
				}
			}
		}
		for(Group g : connections.keySet()) {
			List<Group> g3l = connections.get(g);
			for(Group g3 : g3l) {
				for(Group g4 : connections.get(g3)) {
					if(connections.get(g4).contains(g2)) {
						path.add(g1);
						path.add(g3);
						path.add(g4);
						path.add(g2);
						return path;
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean overlaps(Group group) {
		for(List<Group> l : connections.values()) {
			for(Group c : l) {
				if(!(group instanceof GroupGroup))
					continue;
				GroupGroup gg = (GroupGroup) group;
				for(Group g : gg.contained) {
					if(c == g) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String hash() {
		String hash = "";
		for(Group g : this.contained) {
			hash += "-"+g.hash();
		}
		return hash;
	}

	@Override
	public float distance(Group group) {
		return (float) Math.sqrt(Math.pow(this.cLat-group.cLat,2) + Math.pow(this.cLon-group.cLon,2));
	}

	@Override
	public Set<Group> group(List<Group> existingGroups, List<GroupWay> ways) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String toString() {
		String hash = "";
		for(Group g : this.contained) {
			hash += "-"+g;
		}
		return hash;
	}

	public List<Group> getContained() {
		return contained;
	}

	public Map<Group,List<Group>> getConnections() {
		return connections;
	}
}
