package org.wso2.registry.checkin;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.synchronization.SynchronizationConstants;
import org.wso2.carbon.registry.synchronization.SynchronizationException;
import org.wso2.carbon.registry.synchronization.message.MessageCode;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;

public class Delete {
    private ClientOptions clientOptions;
    private Log log = LogFactory.getLog(Delete.class);

    public Delete(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    public void execute() throws SynchronizationException {
        String path = clientOptions.getWorkingLocation() + File.separator + clientOptions.getTargetResource();
        setDeleteRecursively(path);
    }

    private void setDeleteRecursively(String path) throws SynchronizationException{
        File file = new File(path);
        setDelete(ClientUtils.getMetaFilePath(path));
        System.out.println("D " + path);
        if(file.isDirectory()){
            for(String fileName : file.list(new FilenameFilter() {
                public boolean accept(File file, String s) {
                    if(SynchronizationConstants.META_DIRECTORY.equals(s)){
                        return false;
                    }
                    return true;
                }
            })){
                setDeleteRecursively(path + File.separator + fileName);
            }
        }
    }

    private void setDelete(String metaFilePath) throws SynchronizationException {
        File metaFile = new File(metaFilePath);
        XMLStreamWriter xmlWriter = null;
        try {
            OMElement resourceElement = new StAXOMBuilder(new FileInputStream(metaFile)).getDocumentElement();
            resourceElement.addAttribute("status", "deleted", null);
            FileWriter writer = new FileWriter(metaFile);
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            xmlWriter = xof.createXMLStreamWriter(writer);
            resourceElement.serialize(xmlWriter);
            xmlWriter.flush();

        } catch (FileNotFoundException e) {
            throw new SynchronizationException(MessageCode.RESOURCE_NOT_UNDER_REGISTRY_CONTROL);
        } catch (Exception e) {
            throw new SynchronizationException(MessageCode.RESOURCE_METADATA_CORRUPTED);
        } finally {
            try {
                if (xmlWriter != null) {
                    xmlWriter.close();
                }
            } catch (XMLStreamException e) {
                log.warn("XML writer not closed correctly" + e.getMessage());
            }
        }
    }

}
