/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.governance.api.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RXTTestBase {

    protected String configPath;
    protected Registry registry;
    protected String fileName = null;
    protected String key = null;
    protected String path1 = null;
    protected String path2 = null;
    protected QName nameReplacement = null;
    protected Map<String, String> values = new HashMap<String, String>();
    protected Map<String, String> search = Collections.emptyMap();

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() {
        registry = TestUtils.getRegistry();
        try {
            TestUtils.cleanupResources(registry);
            configPath = FrameworkSettings.getFrameworkPath() + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                    File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                    "resources" + File.separator + fileName;
        } catch (RegistryException e) {
            e.printStackTrace();
            Assert.fail("Unable to run Governance API tests: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"})
    public void testAddArtifact() throws Exception {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)registry);
        GenericArtifactManager manager = new GenericArtifactManager(registry, key);

        GenericArtifact artifact = manager.newGovernanceArtifact(new QName("MyArtifact"));
        artifact.addAttribute("testTable_testAttribute", "somevalue");
        addMandatoryAttributes(artifact);
        manager.addGenericArtifact(artifact);

        String artifactId = artifact.getId();
        GenericArtifact newArtifact = manager.getGenericArtifact(artifactId);

        Assert.assertEquals(newArtifact.getAttribute("testTable_testAttribute"), "somevalue");

        artifact.addAttribute("testTable_testAttribute", "somevalue2");
        manager.updateGenericArtifact(artifact);

        newArtifact = manager.getGenericArtifact(artifactId);

        String[] values = newArtifact.getAttributes("testTable_testAttribute");

        Assert.assertEquals(values.length, 2);
    }

    @Test(groups = {"wso2.greg"})
    public void testArtifactDelete() throws Exception {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)registry);
        GenericArtifactManager manager = new GenericArtifactManager(registry, key);

        GenericArtifact artifact = manager.newGovernanceArtifact(new QName("MyArtifactName"));
        artifact.addAttribute("testTable_testAttribute", "somevalue");
        addMandatoryAttributes(artifact);
        manager.addGenericArtifact(artifact);

        GenericArtifact newArtifact = manager.getGenericArtifact(artifact.getId());
        Assert.assertNotNull(newArtifact);

        manager.removeGenericArtifact(newArtifact.getId());
        newArtifact = manager.getGenericArtifact(newArtifact.getId());
        Assert.assertNull(newArtifact);


        artifact = manager.newGovernanceArtifact(new QName("MyArtifactName"));
        addMandatoryAttributes(artifact);
        manager.addGenericArtifact(artifact);

        newArtifact = manager.getGenericArtifact(artifact.getId());
        Assert.assertNotNull(newArtifact);

        registry.delete(newArtifact.getPath());
        newArtifact = manager.getGenericArtifact(artifact.getId());
        Assert.assertNull(newArtifact);
    }

    private void addMandatoryAttributes(GenericArtifact artifact)
            throws GovernanceException{
        for (Map.Entry<String, String> e : values.entrySet()) {
            artifact.addAttribute(e.getKey(), e.getValue());
        }
    }

    protected void loadRXTsForAssetModelSamples(String type) {
        try {
            String path = FrameworkSettings.getFrameworkPath() + File.separator + ".." +
                    File.separator + ".." + File.separator + ".." + File.separator + ".." +
                    File.separator + ".." + File.separator + ".." + File.separator + "samples" +
                    File.separator + "asset-models" + File.separator + type + File.separator +
                    "registry-extensions";
            File parentFile = new File(path);
            File[] children = parentFile.listFiles((FileFilter) new SuffixFileFilter("rxt"));
            for (File file : children) {
                Resource resource = registry.newResource();
                resource.setMediaType(
                        GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
                String resourcePath = GovernanceConstants.RXT_CONFIGS_PATH +"/" + file.getName();
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
                        new String((byte[]) registry.get(resourcePath).getContent()));
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

                    GovernanceUtils.loadGovernanceArtifacts((UserRegistry)registry);
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
