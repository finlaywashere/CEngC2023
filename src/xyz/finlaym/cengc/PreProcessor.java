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
		} catch (Exception e) {
			e.printStackTrace();
		}
		long elapsed = System.currentTimeMillis() - start;
		System.out.println("Time elapsed: " + elapsed + "ms");
	}

	public PreProcessor(File xml) throws Exception {
		// XML parsing was helped by https://www.baeldung.com/java-xerces-dom-parsing
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(xml);
		doc.getDocumentElement().normalize();
		Element graph = doc.getDocumentElement();
		Map<Integer, Node> nodes = new HashMap<Integer, Node>();
		List<Way> ways = new ArrayList<Way>();
		NodeList nodesList = graph.getElementsByTagName("node");
		NodeList waysList = graph.getElementsByTagName("way");
		for (int i = 0; i < nodesList.getLength(); i++) {
			org.w3c.dom.Node node = nodesList.item(i);
			if (node.getChildNodes().getLength() < 5) {
				if (node.getChildNodes().getLength() < 2)
					continue;
				Node n = new Node(Integer.valueOf(node.getAttributes().getNamedItem("id").getNodeValue()),
						Float.valueOf(node.getChildNodes().item(1).getChildNodes().item(0).getNodeValue()),
						Float.valueOf(node.getChildNodes().item(0).getChildNodes().item(0).getNodeValue()));
				nodes.put(n.getId(), n);
			} else {
				Node n = new Node(Integer.valueOf(node.getAttributes().getNamedItem("id").getNodeValue()),
						Float.valueOf(node.getChildNodes().item(3).getChildNodes().item(0).getNodeValue()),
						Float.valueOf(node.getChildNodes().item(1).getChildNodes().item(0).getNodeValue()));
				nodes.put(n.getId(), n);
			}
		}
		for (int i = 0; i < waysList.getLength(); i++) {
			org.w3c.dom.Node way = waysList.item(i);
			if (way.getChildNodes().getLength() < 5) {
				Way w = new Way(Integer.valueOf(way.getAttributes().getNamedItem("id").getNodeValue()),
						nodes.get(Integer.valueOf(way.getChildNodes().item(0).getChildNodes().item(0).getNodeValue())),
						nodes.get(Integer.valueOf(way.getChildNodes().item(1).getChildNodes().item(0).getNodeValue())));
				ways.add(w);
			} else {
				Way w = new Way(Integer.valueOf(way.getAttributes().getNamedItem("id").getNodeValue()),
						nodes.get(Integer.valueOf(way.getChildNodes().item(1).getChildNodes().item(0).getNodeValue())),
						nodes.get(Integer.valueOf(way.getChildNodes().item(3).getChildNodes().item(0).getNodeValue())));
				ways.add(w);
			}
		}
		Map<Integer, List<Way>> nodeWays = new HashMap<Integer, List<Way>>();
		for (Way w : ways) {
			List<Way> ways1 = nodeWays.get(w.getNode1().getId());
			if (ways1 == null)
				ways1 = new ArrayList<Way>();
			ways1.add(w);
			nodeWays.put(w.getNode1().getId(), ways1);
			List<Way> ways2 = nodeWays.get(w.getNode2().getId());
			if (ways2 == null)
				ways2 = new ArrayList<Way>();
			ways2.add(w);
			nodeWays.put(w.getNode2().getId(), ways2);

		}
		for (int i : nodes.keySet()) {
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
		Map<Integer, List<Group>> groupLookup = new HashMap<Integer, List<Group>>();
		for (Group g : initialGroups) {
			for (Node n : ((NodeGroup) g).getNodes()) {
				List<Group> l = groupLookup.get(n.getId());
				if (l == null)
					l = new ArrayList<Group>();
				l.add(g);
				groupLookup.put(n.getId(), l);
			}
		}
		List<GroupWay> groupWays = new ArrayList<GroupWay>();
		for (Group g : initialGroups) {
			for (List<Node> nL : ((NodeGroup) g).getConnections().values()) {
				for (Node n : nL) {
					List<Group> g2l = groupLookup.get(n.getId());
					if (g2l == null)
						continue;
					for (Group g2 : g2l) {
						groupWays.add(new GroupGroupWay(g, g2));
					}
				}
			}
		}
		Map<Integer, Set<Group>> l1GroupMap = new HashMap<Integer, Set<Group>>();
		for (Node n : nodes.values()) {
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
		while (last.getGroups().size() > 1) {
			IterationResult result = doIteration(i, last.getGroups().size(), last.getWays(), last.getGroups(), nodes,
					last.getGroupMap());
			results.add(result);
			last = result;
			i++;
		}
		System.out.println("Converged on iteration " + i);

		/*
		 * Set<Group> groups2 = GroupGroup.createGroup(lGroups, lWays2); for(Group g :
		 * groups2) { System.out.println(g); } Map<Group, Set<Group>> lookup3 = new
		 * HashMap<Group, Set<Group>>(); for(Group g : groups2) { GroupGroup gg =
		 * (GroupGroup) g; for(Group g2 : gg.getContained()) { Set<Group> s =
		 * lookup3.get(g2); if(s == null) s = new HashSet<Group>(); s.add(g);
		 * lookup3.put(g2, s); } }
		 */

		/*
		 * Map<Integer, List<Node>> nWays = new HashMap<Integer, List<Node>>(); for(int
		 * i : nodeWays.keySet()) { Node n1 = nodes.get(i); List<Node> n = new
		 * ArrayList<Node>(); for(Way w : nodeWays.get(i)) { n.add(w.getNode1() == n1 ?
		 * w.getNode2() : w.getNode1()); } }
		 */

		System.out.println("Done pre processing optimization");

		System.out.println("Final: " + mapPath(nodes.get(8), nodes.get(1), l1GroupMap, results));
	}

	public IterationResult doIteration(int id, int lastSize, List<GroupWay> groupWays, List<Group> inGL,
			Map<Integer, Node> nodes, Map<Integer, Set<Group>> lastLookup) {
		Set<Group> groups = GroupGroup.createGroup(inGL, groupWays);
		System.out.println("I" + id + ": " + lastSize + " -> " + groups.size());
		Map<Group, Set<Group>> lookup2 = new HashMap<Group, Set<Group>>();

		for (Group g : groups) {
			GroupGroup gg = (GroupGroup) g;
			for (Group g2 : gg.getContained()) {
				Set<Group> l = lookup2.get(g2);
				if (l == null)
					l = new HashSet<Group>();
				l.add(g);
				lookup2.put(g2, l);
			}
		}
		Set<GroupWay> groupWays2 = new HashSet<GroupWay>();
		for (Group g : groups) {
			GroupGroup gg = (GroupGroup) g;
			for (Set<Group> g2l : gg.getConnections().values()) {
				for (Group g2 : g2l) {
					for (Group g3 : lookup2.get(g2)) {
						groupWays2.add(new GroupGroupWay(g, g3));
					}
				}
			}
		}
		List<Group> lGroups = new ArrayList<Group>();
		for (Group g : groups) {
			lGroups.add(g);
		}
		List<GroupWay> lWays2 = new ArrayList<GroupWay>();
		for (GroupWay w : groupWays2) {
			lWays2.add(w);
		}
		Map<Integer, Set<Group>> groupMap = new HashMap<Integer, Set<Group>>();
		for (int i : nodes.keySet()) {
			for (Group g : lastLookup.get(i)) {
				Set<Group> cur = groupMap.get(i);
				if (cur == null)
					cur = new HashSet<Group>();
				Set<Group> g1 = lookup2.get(g);
				cur.addAll(g1);
				groupMap.put(i, cur);
			}
		}

		return new IterationResult(lGroups, lWays2, lookup2, groupMap);
	}

	public List<Node> mapPath(Node n1, Node n2, Map<Integer, Set<Group>> groupNodeMap1, List<IterationResult> results) {
		List<Node> ret = new ArrayList<Node>();
		if (n1 == n2) {
			ret.add(n1);
			return ret;
		}
		for (Group g : groupNodeMap1.get(n1.getId())) {
			for (Group g1 : groupNodeMap1.get(n2.getId())) {
				if (g == g1) {
					ret.add(n1);
					ret.add(n2);
					return ret;
				}
			}
		}
		List<Group> tmp = new ArrayList<Group>();
		for (int i = 0; i < results.size(); i++) {
			IterationResult result = results.get(i);
			Set<Group> g1 = result.getGroupMap().get(n1.getId());
			Set<Group> g2 = result.getGroupMap().get(n2.getId());
			boolean found = false;
			for (Group g3 : g1) {
				if (found)
					break;
				for (Group g4 : g2) {
					if (g3 == g4) {
						// Found match
						System.out.println("Found hit at level " + (i + 2));
						tmp.clear();
						tmp.addAll(((GroupGroup) g3).getContained());
						for (int i1 = i - 1; i1 > 0; i1--) {
							IterationResult curR = results.get(i1 - 1);
							Group src = curR.getGroupMap().get(n1.getId()).iterator().next();
							Group dst = curR.getGroupMap().get(n2.getId()).iterator().next();

							if (src instanceof GroupGroup) {
								GroupGroup srcGG = (GroupGroup) src;
								GroupGroup dstGG = (GroupGroup) dst;
								Group last = srcGG;
								List<Group> tmpNew = new ArrayList<Group>();
								tmpNew.add(last);
								for (int i2 = 0; i2 < tmp.size() + 1; i2++) {
									if (tmpNew.contains(dstGG))
										break;
									Group c;
									if (i2 < tmp.size())
										c = (GroupGroup) tmp.get(i2);
									else
										c = dstGG;

									if (c instanceof NodeGroup) {
										tmpNew.add(c);
										last = c;

									} else if (c instanceof GroupGroup) {
										GroupGroup curr = (GroupGroup) c;

										boolean found1 = false;
										if (curr.getContained().contains(last)) {
											found1 = true;
										}
										if (!found1) {
											for (Group key : curr.getConnections().keySet()) {
												if (tmpNew.contains(key))
													continue;
												Set<Group> value = curr.getConnections().get(key);
												if (value.contains(last)) {
													// last group connects to this group
													tmpNew.add(key);
													found1 = true;
													break;
												}
											}
										}
										if (!found1 && last instanceof GroupGroup) {
											GroupGroup ggLast = (GroupGroup) last;
											for (Group key : ggLast.getConnections().keySet()) {
												Set<Group> value = ggLast.getConnections().get(key);
												for (Group contained : curr.getContained()) {
													if (value.contains(contained)) {
														tmpNew.add(key);
														found1 = true;
														break;
													}
												}
											}
										}
										if (!found1)
											System.out.println("Failed to find!");
										if (i2 != tmp.size() && tmpNew.get(tmpNew.size() - 1) instanceof GroupGroup)
											last = (GroupGroup) tmpNew.get(tmpNew.size() - 1);
									}
								}
								System.out.println("Old Group Size:" + tmp.size());
								System.out.println(tmp);
								tmp = tmpNew;
								System.out.println("New Group Size:" + tmp.size());
								System.out.println(tmp);
							}
						}
					}
				}
			}
			if (found)
				break;
		}
		List<Group> newTmp = new ArrayList<Group>();
		NodeGroup src = (NodeGroup) groupNodeMap1.get(n1.getId()).iterator().next();
		NodeGroup dst = (NodeGroup) groupNodeMap1.get(n2.getId()).iterator().next();
		newTmp.add(src);
		NodeGroup last2 = src;
		for (int i = 0; i < tmp.size(); i++) {
			Group g = tmp.get(i);
			if (g instanceof NodeGroup) {
				last2 = (NodeGroup) g;
				if (!newTmp.contains(g))
					newTmp.add(g);
			} else {
				GroupGroup gg = (GroupGroup) g;
				boolean found1 = false;
				if (gg.getContained().contains(last2)) {
					found1 = true;
				}
				if (!found1) {
					for (Group key : gg.getConnections().keySet()) {
						if (newTmp.contains(key))
							continue;
						NodeGroup nkey = (NodeGroup) key;
						Set<Group> value = gg.getConnections().get(key);
						if (value.contains(last2) && !newTmp.contains(key)) {
							// last group connects to this group
							if (i != tmp.size() - 1) {
								GroupGroup n = (GroupGroup) tmp.get(i + 1);
								for (Group k : n.getConnections().keySet()) {
									Set<Group> v = n.getConnections().get(k);
									if (v.contains(nkey)) {
										newTmp.add(key);
										found1 = true;
										break;
									}

								}
							} else {
								if (key instanceof GroupGroup) {
									GroupGroup kgg = (GroupGroup) key;
									for (Group k : (kgg.getConnections().keySet())) {
										Set<Group> v = kgg.getConnections().get(k);
										if (v.contains(dst)) {
											newTmp.add(key);
											found1 = true;
											break;
										}
									}
								} else if (key instanceof NodeGroup) {
									NodeGroup kgg = (NodeGroup) key;
									for (Node k : (kgg.getConnections().keySet())) {
										List<Node> v = kgg.getConnections().get(k);
										for (Node n3 : dst.getNodes()) {
											if (v.contains(n3)) {
												newTmp.add(key);
												found1 = true;
												break;
											}
										}
									}
								}
							}
						}
					}
				}
				if (!found1)
					System.out.println("Not found!");
			}
		}
		if (!newTmp.contains(dst))
			newTmp.add(dst);

		System.out.println("Old Group Size:" + tmp.size());
		System.out.println(tmp);
		tmp = newTmp;
		System.out.println("New Group Size:" + tmp.size());
		System.out.println(tmp);

		List<Node> ng = new ArrayList<Node>();
		ng.add(n1);
		for (int i = 1; i < tmp.size() - 1; i++) {
			Node last = ng.get(ng.size() - 1);
			NodeGroup g = (NodeGroup) tmp.get(i);
			NodeGroup next = null;
			if (i + 1 < tmp.size()) {
				next = (NodeGroup) tmp.get(i + 1);
			}
			boolean found = false;
			for (Node key : g.getConnections().keySet()) {
				List<Node> value = g.getConnections().get(key);
				if (value.contains(last)) {
					ng.add(key);
					found = true;
					if (next != null) {
						boolean found1 = false;
						for (Node c : next.getNodes()) {
							if (value.contains(c)) {
								found1 = true;
								break;
							}
						}
						if (!found1) {
							for (Node k : g.getConnections().keySet()) {
								value = g.getConnections().get(k);
								for (Node n : next.getNodes()) {
									if (value.contains(n)) {
										found1 = true;
										ng.add(k);
										break;
									}
								}
							}
						}
						if (!found1)
							System.out.println("Not found!");
					} else {
						if (!value.contains(n2)) {
							for (Node k : g.getConnections().keySet()) {
								value = g.getConnections().get(k);
								if (value.contains(n2)) {
									ng.add(k);
									break;
								}
							}
						}
					}
					break;
				}
			}
			if (!found) {
				System.out.println("Not found!");
			}

		}
		ng.add(n2);
		System.out.println(ng);
		return ret;
	}

	public List<Group> findGroupChain(Group start, Group end, List<Group> groups) {
		List<Group> ret = new ArrayList<Group>();
		if (start == end) {
			ret.add(start);
			return ret;
		}
		if (start instanceof GroupGroup) {
			GroupGroup g1 = (GroupGroup) start;
			for (Set<Group> gs : g1.getConnections().values()) {
				for (Group g : gs) {
					if (g == end) {
						ret.add(start);
						ret.add(end);
					}
				}
			}
			GroupGroup g2 = (GroupGroup) end;
			for (Set<Group> gs : g2.getConnections().values()) {
				for (Group g : gs) {
					if (g == start) {
						ret.add(end);
						ret.add(start);
					}
				}
			}
		}

		return ret;

	}
}
