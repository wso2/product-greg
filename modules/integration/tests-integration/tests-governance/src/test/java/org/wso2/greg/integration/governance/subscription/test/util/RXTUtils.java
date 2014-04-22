/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.greg.integration.governance.subscription.test.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.testng.Assert;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;

public class RXTUtils {

    public void loadRXTsForAssetModelSamples(String type, Registry registry, String configPath, String fileName) {

        try {
            String path = CarbonUtils.getCarbonHome() + File.separator + "samples" +
                    File.separator + "asset-models" + File.separator + type + File.separator +
                    "registry-extensions";
            File parentFile = new File(path);
            File[] children = parentFile.listFiles((FileFilter) new SuffixFileFilter("rxt"));
            for (File file : children) {
                Resource resource = registry.newResource();
                resource.setMediaType(
                        GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
                String resourcePath = "repository/components/org.wso2.carbon.governance/types/" + file.getName();
                byte[] fileContents;
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(file);
                    fileContents = new byte[(int) file.length()];
                    fileInputStream.read(fileContents);
                } finally {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                }
                resource.setContent(fileContents);
                registry.put(resourcePath, resource);
                OMElement element = AXIOMUtil.stringToOM(
                        new String(((byte[]) registry.get(resourcePath).getContent()),"UTF-8"));

                String shortName = element.getAttributeValue(new QName("shortName"));
                file = new File(configPath.replace(fileName,
                        file.getName().replace("rxt", "metadata.xml")));
                if (file.exists()) {
                    fileInputStream = null;
                    try {
                        fileInputStream = new FileInputStream(file);
                        fileContents = new byte[(int) file.length()];
                        fileInputStream.read(fileContents);
                    } finally {
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                    }

                    OMElement contentElement = GovernanceUtils.buildOMElement(fileContents);

                    GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
                    GenericArtifactManager manager =
                            new GenericArtifactManager(registry, shortName);
                    GenericArtifact artifact = manager.newGovernanceArtifact(contentElement);
                    manager.addGenericArtifact(artifact);
                }
            }
        } catch (RegistryException e) {
            Assert.fail("Unable to populate RXT configuration", e);
        } catch (XMLStreamException e) {
            Assert.fail("Unable to parse RXT configuration");
        } catch (IOException e) {
            Assert.fail("Unable to read asset payload");
        }
    }
}
