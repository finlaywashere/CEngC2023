package xyz.finlaym.cengc;

public abstract class GroupWay {
	private Group group1;
	private Group group2;
	public GroupWay(Group group1, Group group2) {
		super();
		this.group1 = group1;
		this.group2 = group2;
	}
	public Group getGroup1() {
		return group1;
	}
	public Group getGroup2() {
		return group2;
	}
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GroupWay))
			return false;
		GroupWay g = (GroupWay) obj;
		return group1.equals(g.group1) && group2.equals(g.group2);
	}
}
