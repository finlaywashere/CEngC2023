package xyz.finlaym.cengc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
		List<Node> nodes = new ArrayList<Node>();
		List<Way> ways = new ArrayList<Way>();
		NodeList nodesList = graph.getElementsByTagName("node");
		NodeList waysList = graph.getElementsByTagName("way");
		for(int i = 0; i < nodesList.getLength(); i++) {
			org.w3c.dom.Node node = nodesList.item(i);
		}
		for(int i = 0; i < waysList.getLength(); i++) {
			org.w3c.dom.Node way = waysList.item(i);
		}
	}
}
