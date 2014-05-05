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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.client.WSRegistrySearchClient;
import org.wso2.carbon.governance.platform.extensions.util.MediationUtils;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.governance.api.test.util.FileManagerUtil;
import org.wso2.carbon.registry.governance.api.test.util.TestRegistryServiceImpl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

public class GovernanceMediationAssestsTestCase {

    protected String synapsePath;
    protected Registry registry;
    protected String proxyKey = "proxy";
    protected String sequenceKey = "sequence";
    protected String endpointKey = "endpoint";
    private static final String RXT_MEDIA_TYPE = "application/vnd.wso2.registry-ext-type+xml";
    private static String cookie;


    protected OMElement synapseConfig;

    public Registry getRegistry() {
        return registry;
    }

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() {
        registry = TestUtils.getRegistry();
        try {
            TestUtils.cleanupResources(registry);
            installRXT("endpoint.rxt");
            installRXT("proxy.rxt");
            installRXT("sequence.rxt");

            synapsePath = FrameworkSettings.getFrameworkPath() + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                    File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                    "resources" + File.separator + "sample-synapse.xml";
            FileInputStream inputStream = new FileInputStream(new File(synapsePath));
            synapseConfig = new StAXOMBuilder(inputStream).getDocumentElement();

        } catch (RegistryException e) {
            e.printStackTrace();
            Assert.fail("Unable to run Governance API tests: " + e.getMessage());
        } catch (FileNotFoundException e) {
            Assert.fail("Unable to run Governance API tests: " + e.getMessage());
        } catch (XMLStreamException e) {
            Assert.fail("Unable to run Governance API tests: " + e.getMessage());
        } catch (IOException e) {
            Assert.fail("Unable to run Governance API tests: " + e.getMessage());
        }
    }

    private void installRXT(String key) throws RegistryException, IOException {
        Resource resource = registry.newResource();
        String rxtLocation = "/_system/governance/repository/components/org.wso2.carbon.governance/types/";

        String rxtFilePath = FrameworkSettings.getFrameworkPath() + File.separator + ".." + File.separator + ".." + File.separator + ".."
                + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                + "resources" + File.separator + "rxt" + File.separator;

        resource.setContent(FileManagerUtil.readFile(rxtFilePath + key));

        resource.setMediaType(RXT_MEDIA_TYPE);
        registry.put(rxtLocation + key, resource);
        assertTrue(registry.resourceExists(rxtLocation + key),
                key + " rxt resource doesn't exists");

    }

    @Test(groups = {"wso2.greg"})
    public void testPopulateArtifact() {
        try {
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            PaginationContext.init(0, 20, "", "", 20);

            String url = "https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT
                    + "/services/";
            WSRegistrySearchClient wsRegistrySearchClient =
                                                 new WSRegistrySearchClient();
                    

  cookie = wsRegistrySearchClient.authenticate(ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                             FrameworkSettings.CARBON_HOME +
                                     File.separator + "repository" + File.separator +
                                     "deployment" +
                                     File.separator + "client",
                             ServerConfiguration.getInstance()
                                     .getFirstProperty(
                                            "Axis2Config.clientAxis2XmlLocation")),url,FrameworkSettings.USER_NAME,FrameworkSettings.PASSWORD);
            wsRegistrySearchClient.init(cookie,url,ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                            FrameworkSettings.CARBON_HOME +
                                    File.separator + "repository" + File.separator +
                                    "deployment" +
                                    File.separator + "client",
                            ServerConfiguration.getInstance()
                                    .getFirstProperty(
                                             "Axis2Config.clientAxis2XmlLocation")));



            MediationUtils.setRegistryService(new TestRegistryServiceImpl(registry));

            GenericArtifactManager proxyManager = new GenericArtifactManager(registry, proxyKey);
            GenericArtifactManager sequenceManager = new GenericArtifactManager(registry, sequenceKey);
            GenericArtifactManager endpointManager = new GenericArtifactManager(registry, endpointKey);

            MediationUtils.populateArtifacts(MediationUtils.getProxies(synapseConfig), proxyManager);
            MediationUtils.populateArtifacts(MediationUtils.getSequences(synapseConfig), sequenceManager);
            MediationUtils.populateArtifacts(MediationUtils.getEndpoints(synapseConfig), endpointManager);

            // To wait until artifacts gets indexed
            Thread.sleep(120000);

            GenericArtifact[] proxies = proxyManager.getAllGenericArtifacts();
            GenericArtifact[] sequences = sequenceManager.getAllGenericArtifacts();
            GenericArtifact[] endpoints = endpointManager.getAllGenericArtifacts();

            Assert.assertEquals(proxies.length, 2);
            Assert.assertEquals(sequences.length, 3);
            Assert.assertEquals(endpoints.length, 1);

            String proxy = "<ns1:proxy name=\"Proxy3\" xmlns:ns1=\"http://ws.apache.org/ns/synapse\" transports=\"https http\" startOnLoad=\"false\" trace=\"true\">\n" +
                    "      <description />\n" +
                    "      <target>\n" +
                    "         <inSequence>\n" +
                    "            <log level=\"full\" />\n" +
                    "            <sequence key=\"Sequence1\" />\n" +
                    "         </inSequence>\n" +
                    "      </target>\n" +
                    "   </ns1:proxy>";

            String endpoint = "<ns1:endpoint xmlns:ns1=\"http://ws.apache.org/ns/synapse\" name=\"Endpoint2\">\n" +
                    "      <ns1:address uri=\"https://localhost:9444/carbon\" />\n" +
                    "   </ns1:endpoint>";

            String sequence = "<ns1:sequence name=\"Sequence2\" xmlns:ns1=\"http://ws.apache.org/ns/synapse\">\n" +
                    "      <log level=\"full\">\n" +
                    "         <property name=\"MESSAGE\" value=\"Executing default 'fault' sequence\" />\n" +
                    "         <property name=\"ERROR_CODE\" expression=\"get-property('ERROR_CODE')\" />\n" +
                    "         <property name=\"ERROR_MESSAGE\" expression=\"get-property('ERROR_MESSAGE')\" />\n" +
                    "      </log>\n" +
                    "      <drop />\n" +
                    "   </ns1:sequence>";

            OMElement proxyEl = AXIOMUtil.stringToOM(proxy);
            OMElement sequenceEl = AXIOMUtil.stringToOM(sequence);
            OMElement endpointEl = AXIOMUtil.stringToOM(endpoint);

            synapseConfig.addChild(proxyEl);
            synapseConfig.addChild(sequenceEl);
            synapseConfig.addChild(endpointEl);


            MediationUtils.populateArtifacts(MediationUtils.getProxies(synapseConfig), proxyManager);
            MediationUtils.populateArtifacts(MediationUtils.getSequences(synapseConfig), sequenceManager);
            MediationUtils.populateArtifacts(MediationUtils.getEndpoints(synapseConfig), endpointManager);

            // To wait until artifacts gets indexed
            Thread.sleep(120000);

            GenericArtifact[] _proxies = proxyManager.getAllGenericArtifacts();
            GenericArtifact[] _sequences = sequenceManager.getAllGenericArtifacts();
            GenericArtifact[] _endpoints = endpointManager.getAllGenericArtifacts();

            Assert.assertEquals(_proxies.length, 3);
            Assert.assertEquals(_sequences.length, 4);
            Assert.assertEquals(_endpoints.length, 2);

            OMElement newProxy = synapseConfig.getFirstChildWithName(new QName("http://ws.apache.org/ns/synapse","proxy"));
            String proxyName = newProxy.getAttributeValue(new QName("name"));
            OMAttribute attribute = newProxy.getAttribute(new QName("startOnLoad"));
            attribute.setAttributeValue("false");

            MediationUtils.populateArtifacts(MediationUtils.getProxies(synapseConfig), proxyManager);

            // To wait until artifacts gets indexed
            Thread.sleep(120000);

            GenericArtifact[] updatedProxies = proxyManager.getAllGenericArtifacts();

            boolean updateOperationWorks = false;
            for (GenericArtifact genericArtifact : updatedProxies) {
                if (genericArtifact.getAttribute("overview_name").equals(proxyName)
                        && genericArtifact.getAttribute("overview_startOnLoad").equals("false")) {
                    updateOperationWorks = true;
                    break;
                }
            }
            Assert.assertTrue(updateOperationWorks);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unable to run Governance API tests: " + e.getMessage());
        } finally {
            PaginationContext.destroy();
        }
    }
}
