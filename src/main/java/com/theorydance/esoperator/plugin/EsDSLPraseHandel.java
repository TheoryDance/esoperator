package com.theorydance.esoperator.plugin;

import java.util.HashMap;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class EsDSLPraseHandel extends DefaultHandler {
	
	private String dslId;
	private String qname;
	
    //遍历xml文件开始标签
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    //遍历xml文件结束标签
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        qname = qName;
        if(qName.equalsIgnoreCase("dsl")) {
            int num = attributes.getLength();
            Map<String,String> map = new HashMap<>();
            for (int i = 0; i < num; i++) {
            	String attrName = attributes.getQName(i);
            	String attrValue = attributes.getValue(i);
            	if(attrName.equals("id")) {
            		this.dslId = attrValue;
            	}
            	map.put(attrName, attrValue);
    		}
            EsProxy.dslMap.put(dslId, map);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if(qname.equalsIgnoreCase("dsl")) {
        	String value = new String(ch,start,length).trim();
        	Map<String,String> map = EsProxy.dslMap.get(dslId);
        	if(map == null) {
        		map = new HashMap<>();
        		EsProxy.dslMap.put(dslId, map);
        	}
        	String has = map.get("dsl");
        	map.put("dsl", has!=null? has+value: value);
        }
    }
}
