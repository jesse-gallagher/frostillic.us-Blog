package com.raidomatic.xml;

import java.util.List;
import java.util.Vector;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class XMLNode {
	org.w3c.dom.Node node = null;
	XPath xPath = null;
	
	protected XMLNode() { }
	public XMLNode(org.w3c.dom.Node node) { this.node = node; }
	
	public XMLNode selectSingleNode(String xpathString) throws Exception {
		//return new XMLNode((org.w3c.dom.Node)this.getXPath().compile(xpathString).evaluate(node, XPathConstants.NODE));
		List<XMLNode> result = this.selectNodes(xpathString);
		return result.size() == 0 ? null : result.get(0);
	}
	public List<XMLNode> selectNodes(String xpathString) throws Exception {
		List<XMLNode> result = new Vector<XMLNode>();
		
		NodeList nodes = (NodeList)this.getXPath().compile(xpathString).evaluate(node, XPathConstants.NODESET);
		for(int i = 0; i < nodes.getLength(); i++) {
			result.add(new XMLNode(nodes.item(i)));
		}
		
		return result;
	}
	public String getAttribute(String attribute) throws Exception {
		if(this.node == null) { return ""; }
		NamedNodeMap attributes = this.node.getAttributes();
		org.w3c.dom.Node att = attributes.getNamedItem(attribute);
		if(att == null) { return ""; }
		return att.getTextContent();
	}
	public String getText() {
		if(node == null) {
			return "";
		}
		return node.getTextContent();
	}
	
	
	private XPath getXPath() {
		if(this.xPath == null) { xPath = XPathFactory.newInstance().newXPath(); }
		return this.xPath;
	}
}