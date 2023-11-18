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
}
