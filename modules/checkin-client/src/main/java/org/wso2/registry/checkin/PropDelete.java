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
import java.util.Set;

public class PropDelete {
    private ClientOptions clientOptions;
    private Log log = LogFactory.getLog(PropDelete.class);

    public PropDelete(ClientOptions clientOptions) {
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
                return;
            }

            Set<String> deletedPropertyMap = clientOptions.getDeletedProperties();
            AXIOMXPath expression;
            Iterator<String> keys = deletedPropertyMap.iterator();
            while (keys.hasNext()){
                String key = keys.next();
                expression = new AXIOMXPath("property[@key='" + key + "']");
                List<OMElement> elements = expression.selectNodes(propertiesElement);
                if(!elements.isEmpty()){
                    elements.get(0).detach();
                    log.info("D Property " + key);
                }
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
