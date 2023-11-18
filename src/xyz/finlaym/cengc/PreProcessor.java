package xyz.finlaym.cengc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PreProcessor {
	public static void main(String[] args) {
		try {
			new PreProcessor(new File("test_data/TA1.xml"));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public PreProcessor(File xml) throws Exception{
		// XML parsing was helped by https://www.baeldung.com/java-xerces-dom-parsing
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(xml);
		doc.getDocumentElement().normalize();
		Element graph = doc.getDocumentElement();
		List<Node> nodes = new ArrayList<Node>();
		List<Way> ways = new ArrayList<Way>();
	}
}
