package xyz.finlaym.cengc;

import java.util.List;
import java.util.Set;

public abstract class Group {
	public static final int MAX_GROUP_SIZE = 50;
	
	protected float cLon;
	protected float cLat;
	
	public Group(float cLon, float cLat) {
		this.cLon = cLon;
		this.cLat = cLat;
	}
	public float getcLon() {
		return cLon;
	}
	public float getcLat() {
		return cLat;
	}
	public abstract Set<Group> group(List<Group> existingGroups, List<GroupWay> ways);
	public abstract boolean overlaps(Group group);
	public abstract String hash();
	public abstract float distance(Group group);
}
