package xyz.finlaym.cengc;

import java.util.List;

public class Node {
	private int id;
	private float lon;
	private float lat;
	private List<Way> ways;
	
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
	public List<Way> getWays() {
		return ways;
	}
	public void setWays(List<Way> ways) {
		this.ways = ways;
	}
	@Override
	public String toString() {
		return "ID: " + id + ", lat: " + lat + ", lon: " + lon;
	}
}
