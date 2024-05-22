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

    private boolean isInbound = false;
    private boolean isBackend = false;
    private boolean isOutbound =false;
    private boolean isOnError = false;
    private boolean isInsidePolicyTag = false;

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
                if ((isInbound || isBackend || isOutbound || isOnError) && !qName.equalsIgnoreCase("base")) {
                    if (!isInsidePolicyTag) {
                        policyCount++;
                        isInsidePolicyTag = true;
                    }
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
                if (isInsidePolicyTag && (isInbound || isBackend || isOutbound || isOnError)) {
                    isInsidePolicyTag = false;
                }
                break;
        }
    }

}
