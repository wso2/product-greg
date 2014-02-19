package org.wso2.registry.checkin;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.synchronization.SynchronizationException;
import org.wso2.carbon.registry.synchronization.message.MessageCode;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PropSet {
    private ClientOptions clientOptions;
    private Log log = LogFactory.getLog(PropSet.class);

    public PropSet(ClientOptions clientOptions){
        this.clientOptions = clientOptions;
    }

    public void execute() throws SynchronizationException {
        String path = clientOptions.getWorkingLocation() + File.separator + clientOptions.getTargetResource();
        File metaFile = new File(ClientUtils.getMetaFilePath(path));

        try {
            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMElement resourceElement = new StAXOMBuilder(new FileInputStream(metaFile)).getDocumentElement();
            if(resourceElement.getAttribute(new QName("status")) == null){
                resourceElement.addAttribute("status", "updated", null);
            }
            Iterator i = resourceElement.getChildrenWithName(new QName("properties"));
            OMElement propertiesElement;
            if(i.hasNext()){
                propertiesElement = (OMElement) i.next();
            } else {
                propertiesElement  = factory.createOMElement(new QName("properties"));
                resourceElement.addChild(propertiesElement);
            }

            Map<String, String> propertyMap = clientOptions.getProperties();
            AXIOMXPath expression;
            for(String key : propertyMap.keySet()){
                expression = new AXIOMXPath("property[@key='" + key + "']");
                List<OMElement> elements = expression.selectNodes(propertiesElement);
                String value = propertyMap.get(key);
                if(!elements.isEmpty()){
                    elements.get(0).setText(value);
                } else {
                    OMElement propertyElement = factory.createOMElement(new QName("property"));
                    propertyElement.addAttribute("key", key, null);
                    propertyElement.setText(value);
                    propertiesElement.addChild(propertyElement);
                }
                log.info("A Property " + key + "Value" + value);
            }

            FileWriter writer = new FileWriter(metaFile);
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            XMLStreamWriter xmlWriter = xof.createXMLStreamWriter(writer);
            resourceElement.serialize(xmlWriter);
            xmlWriter.flush();

        } catch (FileNotFoundException e) {
            throw new SynchronizationException(MessageCode.RESOURCE_NOT_UNDER_REGISTRY_CONTROL);
        } catch (Exception e) {
            throw new SynchronizationException(MessageCode.RESOURCE_METADATA_CORRUPTED);
        }
    }
}
