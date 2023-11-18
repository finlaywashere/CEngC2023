package xyz.finlaym.cengc;

public class Way {
	private int id;
	private Node node1;
	private Node node2;
	public Way(int id, Node node1, Node node2) {
		this.id = id;
		this.node1 = node1;
		this.node2 = node2;
	}
	public int getId() {
		return id;
	}
	public Node getNode1() {
		return node1;
	}
	public Node getNode2() {
		return node2;
	}
	@Override
	public String toString() {
		return node1.toString() + " -> " + node2.toString();
	}
}
