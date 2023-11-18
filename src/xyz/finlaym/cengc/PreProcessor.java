package xyz.finlaym.cengc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PreProcessor {
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		try {
			new PreProcessor(new File("test_data/TA7.xml"));
		}catch(Exception e) {
			e.printStackTrace();
		}
		long elapsed = System.currentTimeMillis()-start;
		System.out.println("Time elapsed: " + elapsed + "ms");
	}
	public PreProcessor(File xml) throws Exception{
		// XML parsing was helped by https://www.baeldung.com/java-xerces-dom-parsing
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(xml);
		doc.getDocumentElement().normalize();
		Element graph = doc.getDocumentElement();
		Map<Integer,Node> nodes = new HashMap<Integer,Node>();
		List<Way> ways = new ArrayList<Way>();
		NodeList nodesList = graph.getElementsByTagName("node");
		NodeList waysList = graph.getElementsByTagName("way");
		for(int i = 0; i < nodesList.getLength(); i++) {
			org.w3c.dom.Node node = nodesList.item(i);
			if(node.getChildNodes().getLength() < 5) {
				if(node.getChildNodes().getLength() < 2)
					continue;
				Node n = new Node(Integer.valueOf(node.getAttributes().getNamedItem("id").getNodeValue()),
						Float.valueOf(node.getChildNodes().item(1).getChildNodes().item(0).getNodeValue()), 
						Float.valueOf(node.getChildNodes().item(0).getChildNodes().item(0).getNodeValue()));
				nodes.put(n.getId(), n);
			}else {
				Node n = new Node(Integer.valueOf(node.getAttributes().getNamedItem("id").getNodeValue()),
					Float.valueOf(node.getChildNodes().item(3).getChildNodes().item(0).getNodeValue()), 
					Float.valueOf(node.getChildNodes().item(1).getChildNodes().item(0).getNodeValue()));
				nodes.put(n.getId(), n);
			}
		}
		for(int i = 0; i < waysList.getLength(); i++) {
			org.w3c.dom.Node way = waysList.item(i);
			if(way.getChildNodes().getLength() < 5) {
				Way w = new Way(Integer.valueOf(way.getAttributes().getNamedItem("id").getNodeValue()),
					nodes.get(Integer.valueOf(way.getChildNodes().item(0).getChildNodes().item(0).getNodeValue())),
					nodes.get(Integer.valueOf(way.getChildNodes().item(1).getChildNodes().item(0).getNodeValue())));
				ways.add(w);
			}else {
				Way w = new Way(Integer.valueOf(way.getAttributes().getNamedItem("id").getNodeValue()),
					nodes.get(Integer.valueOf(way.getChildNodes().item(1).getChildNodes().item(0).getNodeValue())),
					nodes.get(Integer.valueOf(way.getChildNodes().item(3).getChildNodes().item(0).getNodeValue())));
				ways.add(w);
			}
		}
		Map<Integer, List<Way>> nodeWays = new HashMap<Integer, List<Way>>();
		for(Way w : ways) {
			List<Way> ways1 = nodeWays.get(w.getNode1().getId());
			if(ways1 == null)
				ways1 = new ArrayList<Way>();
			ways1.add(w);
			nodeWays.put(w.getNode1().getId(), ways1);
			List<Way> ways2 = nodeWays.get(w.getNode2().getId());
			if(ways2 == null)
				ways2 = new ArrayList<Way>();
			ways2.add(w);
			nodeWays.put(w.getNode2().getId(), ways2);
			
		}
		for(int i : nodes.keySet()) {
			Node n = nodes.get(i);
			n.setWays(nodeWays.get(n.getId()));
			nodes.put(i, n);
		}
		List<Node> nodesL = new ArrayList<Node>();
		nodesL.addAll(nodes.values());
		System.out.println("Nodes: " + nodes.size() + ", Ways: " + ways.size());
		Set<Group> initialGroups = NodeGroup.groupNodes(nodesL, ways);
		for(Group g : initialGroups) {
			System.out.println(g);
		}
		System.out.println("I1: " + nodes.size() + " -> " + initialGroups.size());
		List<Group> inGL = new ArrayList<Group>();
		inGL.addAll(initialGroups);
		List<GroupWay> ways2 = new ArrayList<GroupWay>();
		Map<Integer, List<Group>> groupLookup = new HashMap<Integer,List<Group>>();
		for(Group g : initialGroups) {
			for(Node n : ((NodeGroup)g).getNodes()) {
				List<Group> l = groupLookup.get(n.getId());
				if(l == null)
					l = new ArrayList<Group>();
				l.add(g);
				groupLookup.put(n.getId(), l);
			}
		}
		List<GroupWay> groupWays = new ArrayList<GroupWay>();
		for(Group g : initialGroups) {
			for(Node n : ((NodeGroup)g).getConnections()) {
				List<Group> g2l = groupLookup.get(n.getId());
				if(g2l == null)
					continue;
				for(Group g2 : g2l) {
					groupWays.add(new GroupGroupWay(g, g2));
				}
			}
		}
		Set<Group> groups = GroupGroup.createGroup(inGL, ways2);
		for(Group g : groups) {
			System.out.println(g);
		}
		System.out.println("I2: " + initialGroups.size() + " -> " + groups.size());
		Map<Group, List<Group>> ways3 = new HashMap<Group, List<Group>>();
		for(Group g : groups) {
			GroupGroup gg = (GroupGroup) g;
			for(Group c : gg.getConnections()) {
				
			}
		}
	}
}
