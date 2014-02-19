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
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.Iterator;
import java.util.List;

/**
    Process BPMN file OMElement and save
 */
public class BPMN2Processor {
    private static final Log log = LogFactory.getLog(BPMN2Processor.class);
    private OMElement processesInfoElement;
    private UserRegistry governanceSystemRegistry;
    private Registry registry;

    public BPMN2Processor(Registry registry, OMElement processesInfoElement) throws RegistryException {
        this.processesInfoElement = processesInfoElement;
        this.registry = registry;
        this.governanceSystemRegistry =
                       RegistryCoreServiceComponent.getRegistryService().getGovernanceSystemRegistry();
    }

    public void putProcessesToRegistry() throws RegistryException {
        try{
            Iterator processes = processesInfoElement.getChildElements();
            while (processes.hasNext()) {
                saveProcessToRegistry((OMElement) processes.next());
            }
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
            if("id".equals(localName)) {
                artifactBean.setArtifactId(attribute.getAttributeValue());
            } else if("name".equals(localName)) {
                processName = attribute.getAttributeValue();
                artifactBean.setAttribute("details_name", processName);
            } else if("isexecutable".equals(localName)) {
                artifactBean.setAttribute("details_executability", attribute.getAttributeValue());
            }
        }

        GenericArtifact  processArtifact = artifactBean.getArtifact();
        genericArtifactManager.addGenericArtifact(processArtifact);
        processPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        GovernanceUtils.getArtifactPath(governanceSystemRegistry, processArtifact.getId());

        Iterator processElements = processInfoElement.getChildElements();
        while (processElements.hasNext()) {
            OMElement processElement = (OMElement)processElements.next();
            String taskPath = null;
            if("usertask".equals(processElement.getLocalName().toLowerCase())){
                taskPath = saveUserTaskToRegistry(processElement, processName);
            } else if("servicetask".equals(processElement.getLocalName().toLowerCase())){
                taskPath = saveServiceTaskToRegistry(processElement, processName);
            }
            if(taskPath != null){
                registry.addAssociation(processPath, taskPath, ProcessUtil.OWNS);
                registry.addAssociation(taskPath, processPath, ProcessUtil.OWNED_BY);
            }
        }
    }

    private String saveUserTaskToRegistry(OMElement taskElement, String process)
            throws JaxenException, RegistryException {
        AXIOMXPath expression;

        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(governanceSystemRegistry, "userTasks");
        ArtifactBean artifactBean = new ArtifactBean(genericArtifactManager);

        expression = new AXIOMXPath("@*");
        List attributes= expression.selectNodes(taskElement);
        for (int i = 0; i < attributes.size(); i++) {
            OMAttribute attribute = (OMAttribute) attributes.get(i);
            String localName = attribute.getLocalName().toLowerCase();
            if("id".equals(localName)) {
                artifactBean.setArtifactId(attribute.getAttributeValue());
            } else if("name".equals(localName)) {
                artifactBean.setAttribute("details_name", attribute.getAttributeValue());
            }
        }

        artifactBean.setAttribute("details_process", process);
        GenericArtifact  userTaskArtifact = artifactBean.getArtifact();
        genericArtifactManager.addGenericArtifact(userTaskArtifact);
        return  RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                GovernanceUtils.getArtifactPath(governanceSystemRegistry, userTaskArtifact.getId());
    }

    private String saveServiceTaskToRegistry(OMElement taskElement , String process)
            throws JaxenException, RegistryException {
        AXIOMXPath expression;

        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(governanceSystemRegistry, "serviceTasks");
        ArtifactBean artifactBean = new ArtifactBean(genericArtifactManager);

        expression = new AXIOMXPath("@*");
        List attributes= expression.selectNodes(taskElement);
        for (int i = 0; i < attributes.size(); i++) {
            OMAttribute attribute = (OMAttribute) attributes.get(i);
            String localName = attribute.getLocalName().toLowerCase();
            if("id".equals(localName)) {
                artifactBean.setArtifactId(attribute.getAttributeValue());
            } else if("name".equals(localName)) {
                artifactBean.setAttribute("details_name", attribute.getAttributeValue());
            }
        }

        artifactBean.setAttribute("details_process", process);
        GenericArtifact  serviceTaskArtifact = artifactBean.getArtifact();
        genericArtifactManager.addGenericArtifact(serviceTaskArtifact);
        return  RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                GovernanceUtils.getArtifactPath(governanceSystemRegistry, serviceTaskArtifact.getId());
    }
}
