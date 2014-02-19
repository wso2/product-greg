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

package org.wso2.carbon.registry.samples.handler.process;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.samples.handler.process.utils.BPELProcessor;
import org.wso2.carbon.registry.samples.handler.process.utils.BPMN2Processor;
import org.wso2.carbon.registry.samples.handler.process.utils.ProcessUtil;
import org.wso2.carbon.registry.samples.handler.process.utils.XPDLProcessor;


import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

public class BusinessProcessMediaTypeHandler extends Handler {

    private static final Log log = LogFactory.getLog(BusinessProcessMediaTypeHandler.class);
    private Registry registry;

    public void put(RequestContext requestContext) throws RegistryException{
        if (!ProcessUtil.isUpdateLockAvailable()) {
            return;
        }
        ProcessUtil.acquireUpdateLock();

        try {
            registry = requestContext.getRegistry();
            Resource resource = requestContext.getResource();
            String originalProcessPath = requestContext.getResourcePath().getPath();
            registry.put(originalProcessPath, resource);

            OMElement processInfoElement;
            Object resourceContent = resource.getContent();

            String processContent;
            if (resourceContent instanceof String) {
                processContent = (String) resourceContent;
            } else {
                processContent = new String((byte[]) resourceContent);
            }

            try {
                XMLStreamReader reader = XMLInputFactory.newInstance().
                        createXMLStreamReader(new StringReader(processContent));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                processInfoElement = builder.getDocumentElement();
            } catch (Exception e) {
                String msg = "Error in parsing the Process content of the Process. " +
                        "The requested path to store the Process: " + originalProcessPath + ".";
                log.error(msg);
                throw new RegistryException(msg, e);
            }

            if(resource.getId().toLowerCase().matches(".*bpmn$")){
                BPMN2Processor processor = new BPMN2Processor(registry, processInfoElement);
                processor.putProcessesToRegistry();
            } else if(resource.getId().toLowerCase().matches(".*bpel$")){
                BPELProcessor processor = new BPELProcessor(registry, processInfoElement);
                processor.putProcessesToRegistry();
            } else if(resource.getId().toLowerCase().matches(".*xpdl$")){
                XPDLProcessor processor = new XPDLProcessor(registry, processInfoElement);
                processor.putProcessesToRegistry();
            } else {
                String msg = "Registry currently does not support the process type of " + resource.getId() + ".";
                log.error(msg);
                throw new RegistryException(msg);
            }
            requestContext.setProcessingComplete(true);
        } finally {
            ProcessUtil.releaseUpdateLock();
        }
    }

    public void makeDir(File file) throws IOException {
        if (file != null && !file.exists() && !file.mkdir()) {
            log.warn("Failed to create directory at path: " + file.getAbsolutePath());
        }
    }

    public void makeDirs(File file) throws IOException {
        if (file != null && !file.exists() && !file.mkdirs()) {
            log.warn("Failed to create directories at path: " + file.getAbsolutePath());
        }
    }

    public void delete(File file) throws IOException {
        if (file != null && file.exists() && !file.delete()) {
            log.warn("Failed to delete file/directory at path: " + file.getAbsolutePath());
        }
    }
}
