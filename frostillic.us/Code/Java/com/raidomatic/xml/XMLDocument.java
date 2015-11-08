package com.raidomatic.xml;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XMLDocument extends XMLNode {
	public XMLDocument() { }
	public XMLDocument(org.w3c.dom.Node node) { super(node); }
	
	public void loadURL(String urlString) throws Exception {
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
		this.node = builder.parse((InputStream)conn.getContent());
	}
	
	public void loadInputStream(InputStream is) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
		this.node = builder.parse(is);
	}
}
