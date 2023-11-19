package xyz.finlaym.cengc;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class IterationResult {
	private List<Group> groups;
	private List<GroupWay> ways;
	private Map<Group, Set<Group>> lookup;
	private Map<Integer, Set<Group>> groupMap;
	
	public IterationResult(List<Group> groups, List<GroupWay> ways, Map<Group, Set<Group>> lookup, Map<Integer, Set<Group>> groupMap) {
		this.groups = groups;
		this.ways = ways;
		this.lookup = lookup;
		this.groupMap = groupMap;
	}
	public List<Group> getGroups() {
		return groups;
	}
	public List<GroupWay> getWays() {
		return ways;
	}
	public Map<Group, Set<Group>> getLookup() {
		return lookup;
	}
	public Map<Integer, Set<Group>> getGroupMap() {
		return groupMap;
	}
}
