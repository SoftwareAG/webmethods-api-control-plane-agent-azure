package com.softwareag.controlplane.agent.azure.helpers;

import lombok.Getter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PolicySAXParser extends DefaultHandler {

    boolean isInbound = false;
    boolean isBackend = false;
    boolean isOutbound =false;
    boolean isOnError = false;

    @Getter
    int policyCount = 0;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("inbound")) {
            isInbound = true;
        }
        else if(qName.equalsIgnoreCase("backend")){
            isBackend =true;
        }
        else if(qName.equalsIgnoreCase("outbound")){
            isOutbound=true;
        }
        else if(qName.equalsIgnoreCase("on-error")){
            isOnError=true;
        }
        else if (!qName.equalsIgnoreCase("base") && (isInbound || isBackend || isOutbound || isOnError)) {
            policyCount++;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("inbound")) {
            isInbound = false;
        }
        else if (qName.equalsIgnoreCase("backend")) {
            isBackend = false;
        }
        else if (qName.equalsIgnoreCase("outbound")) {
            isOutbound = false;
        }
        else if(qName.equalsIgnoreCase("on-error")){
            isOnError = false;
        }
    }

}
