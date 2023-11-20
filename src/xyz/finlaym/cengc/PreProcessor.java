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
		Map<Integer, Set<Group>> l1GroupMap = new HashMap<Integer, Set<Group>>();
		for(Node n : nodes.values()) {
			List<Group> g1 = groupLookup.get(n.getId());
			Set<Group> s1 = new HashSet<Group>();
			s1.addAll(g1);
			l1GroupMap.put(n.getId(), s1);
		}
		IterationResult i2 = doIteration(2, initialGroups.size(), groupWays, inGL, nodes, l1GroupMap);
		IterationResult last = i2;
		List<IterationResult> results = new ArrayList<IterationResult>();
		results.add(i2);
		int i = 3;
		while(last.getGroups().size() > 1) {
			IterationResult result = doIteration(i, last.getGroups().size(), last.getWays(), last.getGroups(), nodes, last.getGroupMap());
			results.add(result);
			last = result;
			i++;
		}
		System.out.println("Converged on iteration " + i);
		
		
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
		
		
		
		/*Map<Integer, List<Node>> nWays = new HashMap<Integer, List<Node>>();
		for(int i : nodeWays.keySet()) {
			Node n1 = nodes.get(i);
			List<Node> n = new ArrayList<Node>();
			for(Way w : nodeWays.get(i)) {
				n.add(w.getNode1() == n1 ? w.getNode2() : w.getNode1());
			}
		}*/
		
		System.out.println("Done pre processing optimization");
		
		System.out.println("Final: " + mapPath(nodes.get(4), nodes.get(1), l1GroupMap, results));
	}
	public IterationResult doIteration(int id, int lastSize, List<GroupWay> groupWays, List<Group> inGL, Map<Integer, Node> nodes, Map<Integer, Set<Group>> lastLookup) {
		Set<Group> groups = GroupGroup.createGroup(inGL, groupWays);
		System.out.println("I" + id + ": " + lastSize + " -> " + groups.size());
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
			for(Set<Group> g2l : gg.getConnections().values()) {
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
		Map<Integer, Set<Group>> groupMap = new HashMap<Integer, Set<Group>>();
		for(int i : nodes.keySet()) {
			for(Group g : lastLookup.get(i)) {
				Set<Group> cur = groupMap.get(i);
				if(cur == null)
					cur = new HashSet<Group>();
				Set<Group> g1 = lookup2.get(g);
				cur.addAll(g1);
				groupMap.put(i, cur);
			}
		}
		
		return new IterationResult(lGroups, lWays2, lookup2, groupMap);
	}
	public List<Node> mapPath(Node n1, Node n2,Map<Integer, Set<Group>> groupNodeMap1,List<IterationResult> results){
		List<Node> ret = new ArrayList<Node>();
		if(n1 == n2) {
			ret.add(n1);
			return ret;
		}
		for(Group g : groupNodeMap1.get(n1.getId())) {
			for(Group g1 : groupNodeMap1.get(n2.getId())) {
				if(g == g1) {
					ret.add(n1);
					ret.add(n2);
					return ret;
				}
			}
		}
		List<Group> tmp = new ArrayList<Group>();
		for(int i = 0; i < results.size(); i++) {
			IterationResult result = results.get(i);
			Set<Group> g1 = result.getGroupMap().get(n1.getId());
			Set<Group> g2 = result.getGroupMap().get(n2.getId());
			boolean found = false;
			for(Group g3 : g1) {
				if(found)
					break;
				for(Group g4 : g2) {
					if(g3 == g4) {
						// Found match
						System.out.println("Found hit at level " + (i+2));
						tmp.clear();
						tmp.addAll(((GroupGroup)g3).getContained());
						for(int i1 = i-1; i1 > 1; i1--) {
							IterationResult curR = results.get(i1-1);
							Group src = curR.getGroupMap().get(n1.getId()).iterator().next();
							Group dst = curR.getGroupMap().get(n2.getId()).iterator().next();
							
							if(src instanceof GroupGroup) {
								GroupGroup srcGG = (GroupGroup) src;
								GroupGroup dstGG = (GroupGroup) dst;
								GroupGroup last = srcGG;
								List<Group> tmpNew = new ArrayList<Group>();
								tmpNew.add(last);
								for(int i2 = 0; i2 < tmp.size(); i2++) {
									GroupGroup tmpG = (GroupGroup) tmp.get(i2);
									boolean found1 = false;
									for(Group key : last.getConnections().keySet()) {
										if(found1)
											break;
										for(Group value : last.getConnections().get(key)) {
											if(tmpG.getContained().contains(value)) {
												tmpNew.add(value);
												found1 = true;
												break;
											}
										}
									}
									Set<Group> lastUS = results.get(i1).getLookup().get(last);
									for(Group last2 : lastUS) {
										GroupGroup lastU = (GroupGroup) last2;
										for(Group group : lastU.getContained()) {
											if(found1)
												break;
											GroupGroup groupGroup = (GroupGroup) group;
											for(Group key : groupGroup.getConnections().keySet()) {
												if(found1)
													break;
												for(Group value : groupGroup.getConnections().get(key)) {
													if(tmpG.getContained().contains(value)) {
														tmpNew.add(group);
														tmpNew.add(value);
														found1 = true;
														break;
													}
												}
											}
										}
										for(Group key : tmpG.getConnections().keySet()) {
											if(found1)
												break;
											for(Group value : tmpG.getConnections().get(key)) {
												if(last.getContained().contains(value)) {
													tmpNew.add(value);
													found1 = true;
													break;
												}
											}
										}
										for(Group group : lastU.getContained()) {
											if(found1)
												break;
											for(Group key : tmpG.getConnections().keySet()) {
												if(found1)
													break;
												for(Group value : tmpG.getConnections().get(key)) {
													if(tmpG.getContained().contains(group)) {
														tmpNew.add(group);
														tmpNew.add(value);
														found1 = true;
														break;
													}
												}
											}
										}
									}
									last = (GroupGroup) tmpNew.get(tmpNew.size()-1);
								}
								GroupGroup tmpG = dstGG;
								boolean found1 = false;
								for(Group key : last.getConnections().keySet()) {
									if(found1)
										break;
									for(Group value : last.getConnections().get(key)) {
										if(value == last) {
											tmpNew.add(key);
											found1 = true;
											break;
										}
									}
								}
								for(Group cont : tmpG.getContained()) {
									if(found1)
										break;
									GroupGroup ggCont = (GroupGroup) cont;
									for(Group key : ggCont.getConnections().keySet()) {
										if(found1)
											break;
										for(Group value : ggCont.getConnections().get(key)) {
											if(value == last) {
												tmpNew.add(key);
												found1 = true;
												break;
											}
										}
									}
								}
								System.out.println("Old Group Size:" +tmp.size());
								System.out.println(tmp);
								tmp = tmpNew;
								System.out.println("New Group Size:" +tmp.size());
								System.out.println(tmp);
							}else {
								System.out.println("Node Size:" +tmp.size());
							}
						}
						found = true;
						break;
					}
				}
			}
			if(found)
				break;
		}
		
		return ret;
	}
	public List<Group> findGroupChain(Group start, Group end, List<Group> groups){
		List<Group> ret = new ArrayList<Group>();
		if(start == end) {
			ret.add(start);
			return ret;
		}
		if(start instanceof GroupGroup) {
			GroupGroup g1 = (GroupGroup) start;
			for(Set<Group> gs : g1.getConnections().values()) {
				for(Group g : gs) {
					if(g == end) {
						ret.add(start);
						ret.add(end);
					}
				}
			}
			GroupGroup g2 = (GroupGroup) end;
			for(Set<Group> gs : g2.getConnections().values()) {
				for(Group g : gs) {
					if(g == start) {
						ret.add(end);
						ret.add(start);
					}
				}
			}
		}
		
		return ret;
			
	}
}
