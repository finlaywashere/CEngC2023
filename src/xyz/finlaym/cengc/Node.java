package xyz.finlaym.cengc;

public class Node {
	private int id;
	private float lon;
	private float lat;
	public Node(int id, float lon, float lat) {
		this.id = id;
		this.lon = lon;
		this.lat = lat;
	}
	public int getId() {
		return id;
	}
	public float getLon() {
		return lon;
	}
	public float getLat() {
		return lat;
	}
}
