package xyz.finlaym.cengc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		System.out.println("Time elapsed: "+ (elapsed/1000));
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
			Node n = new Node(Integer.valueOf(node.getAttributes().getNamedItem("id").getNodeValue()),
					Float.valueOf(node.getChildNodes().item(1).getNodeValue()), 
					Float.valueOf(node.getChildNodes().item(0).getNodeValue()));
			nodes.put(n.getId(), n);
		}
		for(int i = 0; i < waysList.getLength(); i++) {
			org.w3c.dom.Node way = waysList.item(i);
			//Way w = new Way(, node1, node2)
		}
	}
}
