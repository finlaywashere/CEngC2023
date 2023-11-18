package xyz.finlaym.cengc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
			new PreProcessor(new File("test_data/TA1.xml"));
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
			for(List<Node> nL : ((NodeGroup)g).getConnections().values()) {
				for(Node n : nL) {
					List<Group> g2l = groupLookup.get(n.getId());
					if(g2l == null)
						continue;
					for(Group g2 : g2l) {
						groupWays.add(new GroupGroupWay(g, g2));
					}
				}
			}
		}
		Set<Group> groups = GroupGroup.createGroup(inGL, groupWays);
		for(Group g : groups) {
			System.out.println(g);
		}
		System.out.println("I2: " + initialGroups.size() + " -> " + groups.size());
		Map<Group, Set<Group>> lookup2 = new HashMap<Group, Set<Group>>();
		
		for(Group g : groups) {
			GroupGroup gg = (GroupGroup) g;
			for(Group g2 : gg.getContained()) {
				Set<Group> l = lookup2.get(g2);
				if(l == null)
					l = new HashSet<Group>();
				l.add(g);
				lookup2.put(g2, l);
			}
		}
		Set<GroupWay> groupWays2 = new HashSet<GroupWay>();
		for(Group g : groups) {
			GroupGroup gg = (GroupGroup) g;
			for(List<Group> g2l : gg.getConnections().values()) {
				for(Group g2 : g2l) {
					for(Group g3 : lookup2.get(g2)) {
						groupWays2.add(new GroupGroupWay(g, g3));
					}
				}
			}
		}
		List<Group> lGroups = new ArrayList<Group>();
		for(Group g : groups) {
			lGroups.add(g);
		}
		List<GroupWay> lWays2 = new ArrayList<GroupWay>();
		for(GroupWay w : groupWays2) {
			lWays2.add(w);
		}
		/*Set<Group> groups2 = GroupGroup.createGroup(lGroups, lWays2);
		for(Group g : groups2) {
			System.out.println(g);
		}
		Map<Group, Set<Group>> lookup3 = new HashMap<Group, Set<Group>>();
		for(Group g : groups2) {
			GroupGroup gg = (GroupGroup) g;
			for(Group g2 : gg.getContained()) {
				Set<Group> s = lookup3.get(g2);
				if(s == null)
					s = new HashSet<Group>();
				s.add(g);
				lookup3.put(g2, s);
			}
		}*/
		
		Map<Integer, Set<Group>> l2GroupMap = new HashMap<Integer, Set<Group>>();
		Map<Integer, Set<Group>> l1GroupMap = new HashMap<Integer, Set<Group>>();
		for(Node n : nodes.values()) {
			List<Group> g1 = groupLookup.get(n.getId());
			Set<Group> s1 = new HashSet<Group>();
			s1.addAll(g1);
			l1GroupMap.put(n.getId(), s1);
			Set<Group> g2 = new HashSet<Group>();
			for(Group g : g1) {
				g2.addAll(lookup2.get(g));
			}
			l2GroupMap.put(n.getId(), g2);
		}
		
		Map<Integer, List<Node>> nWays = new HashMap<Integer, List<Node>>();
		for(int i : nodeWays.keySet()) {
			Node n1 = nodes.get(i);
			List<Node> n = new ArrayList<Node>();
			for(Way w : nodeWays.get(i)) {
				n.add(w.getNode1() == n1 ? w.getNode2() : w.getNode1());
			}
		}
		
		System.out.println("Done pre processing optimization");
		
		mapPath(nodes.get(2), nodes.get(1), l1GroupMap, l2GroupMap, groups, nWays);
	}
	public List<Node> mapPath(Node n1, Node n2,Map<Integer, Set<Group>> groupNodeMap1,Map<Integer, Set<Group>> groupNodeMap2, Set<Group> groups, Map<Integer,List<Node>> ways){
		List<Node> ret = new ArrayList<Node>();
		Set<Group> g1 = groupNodeMap2.get(n1.getId());
		Set<Group> g2 = groupNodeMap2.get(n2.getId());
		for(Group g : g1) {
			for(Group g3 : g2) {
				if(g == g3) {
					// Shared group
					GroupGroup gg = (GroupGroup) g;
					for(Group g4 : groupNodeMap1.get(n1.getId())) {
						for(Group g5 : groupNodeMap1.get(n2.getId())) {
							List<Node> pathU = gg.nodeNavInternally(g, g3, ways);
							if(pathU != null && pathU.size() > 0) {
								// Found path
								System.out.println(pathU);
							}
						}
					}
					
				}
			}
		}
		return ret;
	}
}
