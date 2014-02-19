package org.wso2.registry.checkin;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.synchronization.SynchronizationConstants;
import org.wso2.carbon.registry.synchronization.SynchronizationException;
import org.wso2.carbon.registry.synchronization.Utils;
import org.wso2.carbon.registry.synchronization.message.MessageCode;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;

public class Add {
    private ClientOptions clientOptions;
    private String registryUrl = null;
    private Log log = LogFactory.getLog(Add.class);

    public Add(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    public void execute() throws SynchronizationException {
        String path = clientOptions.getWorkingLocation() + File.separator + clientOptions.getTargetResource();

        String metaFile = path.substring(0, path.lastIndexOf(File.separator)) + File.separator + SynchronizationConstants.META_DIRECTORY +
                File.separator + SynchronizationConstants.META_FILE_PREFIX +
                SynchronizationConstants.META_FILE_EXTENSION;
        File file = new File(metaFile);
        String pathAttribute;
        try {
            OMElement resourceElement = new StAXOMBuilder(new FileInputStream(file)).getDocumentElement();
            pathAttribute = resourceElement.getAttribute(new QName("path")).getAttributeValue();
            OMAttribute registryUrlAttr;
            if((registryUrlAttr = resourceElement.getAttribute(new QName("registryUrl")) ) != null){
                registryUrl = registryUrlAttr.getAttributeValue();
            }
        } catch (FileNotFoundException e) {
            throw new SynchronizationException(MessageCode.CURRENT_COLLECTION_NOT_UNDER_REGISTRY_CONTROL);
        } catch (Exception e) {
            throw new SynchronizationException(MessageCode.RESOURCE_METADATA_CORRUPTED);
        }
        addResourceMetadataRecursively(path, pathAttribute, true);
    }

    private void addResourceMetadataRecursively(String path, String parentRegistryPath, boolean root) throws SynchronizationException {
        File file = new File(path);

        if (!file.exists()) {
            throw new SynchronizationException(MessageCode.FILE_DOES_NOT_EXIST);
        }

        String registryPath = parentRegistryPath + File.separator + file.getName();
        System.out.println("A " + path);
        String metaFilePath;
        if(file.isDirectory()){
            metaFilePath = path + File.separator + SynchronizationConstants.META_DIRECTORY +
                    File.separator +
                    SynchronizationConstants.META_FILE_PREFIX +
                    SynchronizationConstants.META_FILE_EXTENSION;
            addMetadata(metaFilePath, file.getName(), true, registryPath, root);
            for(String fileName : file.list(new FilenameFilter() {
                public boolean accept(File file, String s) {
                    if(SynchronizationConstants.META_DIRECTORY.equals(s)){
                        return false;
                    }
                    return true;
                }
            })){
                addResourceMetadataRecursively(path + File.separator + fileName, registryPath, false);
            }
        } else {
            String parentDirName = file.getParent();
            metaFilePath =
                    parentDirName + File.separator + SynchronizationConstants.META_DIRECTORY +
                            File.separator + SynchronizationConstants.META_FILE_PREFIX +
                            Utils.encodeResourceName(file.getName()) +
                            SynchronizationConstants.META_FILE_EXTENSION;
            addMetadata(metaFilePath, file.getName(), false, registryPath, root);
        }
    }

    private void addMetadata(String metaFilePath, String fileName, boolean isCollection, String registryPath, boolean root)
            throws SynchronizationException {
        File file = new File(metaFilePath);
        if(root && file.exists()){
            throw new SynchronizationException(MessageCode.RESOURCE_ALREADY_UNDER_REGISTRY_CONTROL);
        }
        try {
            File metaDir = new File(file.getParent());
            metaDir.mkdirs();
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            XMLStreamWriter xmlWriter = xof.createXMLStreamWriter(writer);
            xmlWriter.writeStartElement("resource");
            xmlWriter.writeAttribute("name", fileName);
            xmlWriter.writeAttribute("isCollection", String.valueOf(isCollection));
            xmlWriter.writeAttribute("path", registryPath);
            if(registryUrl != null){
                xmlWriter.writeAttribute("registryUrl", registryUrl);
            }
            xmlWriter.writeAttribute("status", "added");

            if(root && clientOptions.getMediatype() != null){
                OMFactory factory = OMAbstractFactory.getOMFactory();
                OMElement mediaTypeElement = factory.createOMElement(new QName("mediaType"));
                mediaTypeElement.setText(clientOptions.getMediatype());
                mediaTypeElement.serialize(xmlWriter);
            }
            xmlWriter.writeEndElement();
            xmlWriter.flush();
        } catch (Exception e) {
            throw new SynchronizationException(MessageCode.ERROR_IN_ADDING_METADATA);
        }
    }
}
