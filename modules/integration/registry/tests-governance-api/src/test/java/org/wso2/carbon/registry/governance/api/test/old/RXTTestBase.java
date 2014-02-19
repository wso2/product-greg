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
package org.wso2.carbon.registry.governance.api.test.old;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class RXTTestBase {

    protected int userId = 1;
    protected String configPath;
    protected Registry registry;
    protected String fileName = null;
    protected String folder = null;
    protected String key = null;
    protected String path1 = null;
    protected String path2 = null;
    protected QName nameReplacement = null;
    protected Map<String, String> values = new HashMap<String, String>();
    protected Map<String, String> search = Collections.emptyMap();

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = registryProviderUtil.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        registry = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);
        configPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + folder + File.separator + fileName;

    }

    @Test(groups = {"wso2.greg"})
    public void testAddArtifact() throws Exception {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        GenericArtifactManager manager = new GenericArtifactManager(registry, key);

        GenericArtifact artifact = manager.newGovernanceArtifact(new QName("MyArtifact"));
        artifact.addAttribute("testAttribute", "somevalue");
        addMandatoryAttributes(artifact);
        manager.addGenericArtifact(artifact);

        String artifactId = artifact.getId();
        GenericArtifact newArtifact = manager.getGenericArtifact(artifactId);

        Assert.assertEquals(newArtifact.getAttribute("testAttribute"), "somevalue");

        artifact.addAttribute("testAttribute", "somevalue2");
        manager.updateGenericArtifact(artifact);

        newArtifact = manager.getGenericArtifact(artifactId);

        String[] values = newArtifact.getAttributes("testAttribute");

        Assert.assertEquals(values.length, 2);
    }

    @Test(groups = {"wso2.greg"})
    public void testArtifactContentXMLInvalid() throws RegistryException,
                                                       XMLStreamException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        GenericArtifactManager manager = new GenericArtifactManager(registry, key);
        String content = "<metadata xmlns=\"http://www.wso2.org/governance/metadata\"><overview><namespace>UserA</namespace></overview></metadata>";
        OMElement XMLContent = AXIOMUtil.stringToOM(content);
        try {
            manager.newGovernanceArtifact(XMLContent);
        } catch (GovernanceException e) {
            Assert.assertEquals(e.getMessage(), "Unable to compute QName from given XML payload, " +
                                                "please ensure that the content passed in matches the configuration.");
            return;
        }
        Assert.fail("An exception was expected to be thrown, but did not.");
    }

    @Test(groups = {"wso2.greg"})
    public void testArtifactDelete() throws Exception {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        GenericArtifactManager manager = new GenericArtifactManager(registry, key);

        GenericArtifact artifact = manager.newGovernanceArtifact(new QName("MyArtifactName"));
        artifact.addAttribute("testAttribute", "somevalue");
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

    @AfterClass
    public void cleanArtifacts() throws RegistryException {
        for (String string : new String[]{"/trunk", "/branches", "/departments", "/organizations",
                "/project-groups", "/people", "/applications", "/processes", "/projects",
                "/test_suites", "/test_cases", "/test_harnesses", "/test_methods", "/uris",
                "repository/components/org.wso2.carbon.governance/types/department.rxt",
                "repository/components/org.wso2.carbon.governance/types/organization.rxt",
                "repository/components/org.wso2.carbon.governance/types/person.rxt",
                "repository/components/org.wso2.carbon.governance/types/project-group.rxt",
                "repository/components/org.wso2.carbon.governance/types/TestCase.rxt",
                "repository/components/org.wso2.carbon.governance/types/TestHarness.rxt",
                "repository/components/org.wso2.carbon.governance/types/TestMethod.rxt",
                "repository/components/org.wso2.carbon.governance/types/TestSuite.rxt"

        }) {

            if (registry.resourceExists(string)) {
                registry.delete(string);
            }
        }
        registry = null;

    }

    private void addMandatoryAttributes(GenericArtifact artifact)
            throws GovernanceException {
        for (Map.Entry<String, String> e : values.entrySet()) {
            artifact.addAttribute(e.getKey(), e.getValue());
        }
    }

    protected void loadRXTsForAssetModelSamples(String type) {
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