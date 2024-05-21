package com.softwareag.controlplane.agent.azure.helpers;

import com.softwareag.controlplane.agent.azure.Constants;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@Component
public class PolicySAXParser extends DefaultHandler {

    boolean isInbound = false;
    boolean isBackend = false;
    boolean isOutbound =false;
    boolean isOnError = false;

    @Getter
    int policyCount = 0;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes){
        //Setting the appropriate tag to true for opening tags, such as <inbound>,<outbound> etc.
        switch (qName.toLowerCase()){
            case Constants.INBOUND_TAG :
                isInbound=true;
                break;
            case Constants.BACKEND_TAG:
                isBackend =true;
                break;
            case Constants.OUTBOUND_TAG:
                isOutbound=true;
                break;
            case Constants.ON_ERROR_TAG:
                isOnError=true;
                break;
            default:
                if (!qName.equalsIgnoreCase(Constants.BASE_TAG) && (isInbound || isBackend || isOutbound || isOnError)) {
                    policyCount++;
                }
                break;
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName){
       // Setting the appropriate tag to false for closing tags, such as </inbound>,</outbound> etc.
        switch (qName.toLowerCase()){
            case Constants.INBOUND_TAG :
                isInbound=false;
                break;
            case Constants.BACKEND_TAG:
                isBackend =false;
                break;
            case Constants.OUTBOUND_TAG:
                isOutbound=false;
                break;
            case Constants.ON_ERROR_TAG:
                isOnError=false;
                break;
            default:
                break;
        }
    }

}
