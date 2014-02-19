/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.samples.handler.process.utils;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class BPELProcessor {

    private static final Log log = LogFactory.getLog(BPELProcessor.class);
    private OMElement processInfoElement;
    private Map<String, String> processPrefixes = new HashMap<String, String>();
    private Registry registry;
    private UserRegistry governanceSystemRegistry;
    public BPELProcessor(Registry registry, OMElement processInfoElement) throws RegistryException {
        this.processInfoElement = processInfoElement;
        this.registry = registry;
        this.governanceSystemRegistry =
                       RegistryCoreServiceComponent.getRegistryService().getGovernanceSystemRegistry();
        Iterator i =processInfoElement.getAllDeclaredNamespaces();
        while (i.hasNext()){
            OMNamespace namespace = (OMNamespace)i.next();
            if(!namespace.getPrefix().equals("")){
                processPrefixes.put(namespace.getPrefix() , namespace.getNamespaceURI());
            }
        }
    }

    public void putProcessesToRegistry() throws RegistryException {
        try{
            saveProcessToRegistry(processInfoElement);
        } catch (JaxenException e) {
            String msg = "Some not valid elements in the source file";
            log.error(msg);
            throw new RegistryException(msg, e);
        }
    }

    private void saveProcessToRegistry(OMElement processInfoElement)
            throws RegistryException, JaxenException {
        String processName = null;
        String processPath;
        AXIOMXPath expression;

        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(governanceSystemRegistry, "processes");
        ArtifactBean artifactBean = new ArtifactBean(genericArtifactManager);

        expression = new AXIOMXPath("@*");
        List attributes = expression.selectNodes(processInfoElement);

        for (int i = 0; i < attributes.size(); i++) {
            OMAttribute attribute = (OMAttribute) attributes.get(i);
            String localName = attribute.getLocalName().toLowerCase();
            if("name".equals(localName)) {
                processName = attribute.getAttributeValue();
                artifactBean.setArtifactId(processName);
                artifactBean.setAttribute("details_name", processName);
            }
        }

        artifactBean.setAttribute("details_executability", "true");
        GenericArtifact  processArtifact = artifactBean.getArtifact();
        genericArtifactManager.addGenericArtifact(processArtifact);
        processPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        GovernanceUtils.getArtifactPath(governanceSystemRegistry, processArtifact.getId());

        SimpleNamespaceContext simpleNamespaceContext = new SimpleNamespaceContext();
        simpleNamespaceContext.addNamespace("pre",processInfoElement.getNamespace().getNamespaceURI());

        expression = new AXIOMXPath("*/pre:invoke");
        expression.setNamespaceContext(simpleNamespaceContext);
        List userTasks = expression.selectNodes(processInfoElement);
        for (int s = 0; s < userTasks.size(); s++) {
            OMElement task = (OMElement) userTasks.get(s);
            String taskPath = saveServiceTaskToRegistry(task, processName);
            registry.addAssociation(processPath, taskPath, ProcessUtil.OWNS);
            registry.addAssociation(taskPath, processPath, ProcessUtil.OWNED_BY);
        }
    }

    private String saveServiceTaskToRegistry(OMElement taskElement, String process)
            throws JaxenException, RegistryException {
        String taskPath = null;
        String partnerLink = null;
        AXIOMXPath expression;

        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(governanceSystemRegistry, "serviceTasks");
        ArtifactBean artifactBean = new ArtifactBean(genericArtifactManager);

        expression = new AXIOMXPath("@*");
        List attributes= expression.selectNodes(taskElement);
        for (int i = 0; i < attributes.size(); i++) {
            OMAttribute attribute = (OMAttribute) attributes.get(i);
            String localName = attribute.getLocalName().toLowerCase();
            if("name".equals(localName)) {
                artifactBean.setArtifactId(attribute.getAttributeValue());
                artifactBean.setAttribute("details_name", attribute.getAttributeValue());
            } else if("partnerlink".equals(localName)) {
                partnerLink = attribute.getAttributeValue();
            }
        }

        artifactBean.setAttribute("details_process", process);
        GenericArtifact  serviceTaskArtifact = artifactBean.getArtifact();
        genericArtifactManager.addGenericArtifact(serviceTaskArtifact);
        taskPath =  RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        GovernanceUtils.getArtifactPath(governanceSystemRegistry, serviceTaskArtifact.getId());

        return taskPath;
    }
}
