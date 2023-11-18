package xyz.finlaym.cengc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupGroup extends Group {
	private List<Group> contained;
	private List<Group> connections;
	public GroupGroup(float cLon, float cLat, List<Group> contained, List<Group> connections) {
		super(cLon, cLat);
		this.contained = contained;
		this.connections = connections;
	}

	public Set<Group> group(List<Group> existingGroups, List<GroupWay> ways) {
		Set<Group> result = new HashSet<Group>();
		while(existingGroups.size() > 0) {
			Group seed = existingGroups.get(0);
			existingGroups.remove(0);
			List<Group> groups = new ArrayList<Group>();
			groups.add(seed);
			for(int i = 0; i < Group.MAX_GROUP_SIZE; i++) {
				if(groups.size() >= Group.MAX_GROUP_SIZE)
					break;
				Group next = findNearest(existingGroups, seed);
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
			List<Group> cons = new ArrayList<Group>();
			for(GroupWay way : ways) {
				for(Group g : groups) {
					if(way.getGroup1() == g) {
						cons.add(way.getGroup2());
					}else if (way.getGroup2() == g) {
						cons.add(way.getGroup1());
					}
				}
			}
			GroupGroup gg = new GroupGroup(avgLon, avgLat, groups, cons);
			result.add(gg);
		}
		return result;
	}
	private Group findNearest(List<Group> groups, Group group) {
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

	@Override
	public boolean overlaps(Group group) {
		for(Group c : connections) {
			if(c == group) {
				return true;
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
}
