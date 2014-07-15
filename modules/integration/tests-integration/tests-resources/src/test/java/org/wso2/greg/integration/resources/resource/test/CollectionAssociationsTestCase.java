/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.greg.integration.resources.resource.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.list.stub.beans.xsd.PolicyBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.SchemaBean;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ListMetaDataServiceClient;
import org.wso2.greg.integration.common.clients.RelationAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CollectionAssociationsTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminClient;
    private RelationAdminServiceClient relationServiceClient;
    private ListMetaDataServiceClient listMetaDataServiceClient;
    private Registry governance;
    private static final String ROOT = "/";
    private static final String PARENT_PATH = "/c1/";
    private static final String CHILD_NAME = "c2";
    private static final String RXT_LOCATION =
            "/_system/governance/repository/components/org.wso2.carbon.governance/types/person.rxt";
    private static final String RXT_FILE_TYPE = "application/vnd.wso2.registry-ext-type+xml";
    private static final String RXT_RESOURCE_PATH = "/_system/governance/people/testPerson";
    private static final String WSDL_PATH =
            "/_system/governance/trunk/wsdls/com/amazon/soap/1.0.0/AmazonWebServices.wsdl";
    private static final String SCHEMA_PATH ="/_system/governance/trunk/schemas/books/1.0.0/books.xsd";
    private static final String SERVICE_PATH =
            "/_system/governance/trunk/services/com/amazon/soap/1.0.0-SNAPSHOT/AmazonSearchService";
    private String pathToPolicy = "";
    private WSRegistryServiceClient registry;

    @BeforeClass (alwaysRun = true)
    public void initialize () throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();
        resourceAdminClient =
                new ResourceAdminServiceClient(getBackendURL(),sessionCookie);
        relationServiceClient =
                new RelationAdminServiceClient(getBackendURL(),sessionCookie);
        listMetaDataServiceClient =
                new ListMetaDataServiceClient(getBackendURL(),sessionCookie);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);

    }

    @Test (groups = "wso2.greg")
    public void testAddCollections ()
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException, XPathExpressionException {

        String fileType = "other";
        String description = "A test collection";
        if (registry.resourceExists(PARENT_PATH + CHILD_NAME)) {
            registry.delete(PARENT_PATH + CHILD_NAME);
        }
        resourceAdminClient.addCollection(PARENT_PATH, CHILD_NAME, fileType, description);
        // We are only creating the child here - there is no point in checking whether the parent was created by same user.
        String authorUserName = resourceAdminClient.getResource(PARENT_PATH + CHILD_NAME)[0].getAuthorUserName();
        assertEquals(authorUserName.toLowerCase(), automationContext.getContextTenant().getContextUser().getUserName(), "Child collection creation failure");
    }

    @Test (groups = "wso2.greg", dependsOnMethods = "testAddCollections")
    public void testAddDependencyFromChildCollection ()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException,
            AddAssociationRegistryExceptionException {

        String dependencyType = "depends";
        String todo = "add";
        relationServiceClient.addAssociation(PARENT_PATH, dependencyType, PARENT_PATH + CHILD_NAME, todo);
        DependenciesBean bean = relationServiceClient.getDependencies(PARENT_PATH);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()),
                "Dependency type is not correct");
        assertTrue((PARENT_PATH + CHILD_NAME).equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()),
                "Target dependency is not correct");
        assertTrue((PARENT_PATH.substring(0,
                PARENT_PATH.length() - 1)).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()),
                "Source dependency is not correct");
        //remove the dependency
        relationServiceClient.addAssociation(PARENT_PATH, dependencyType, PARENT_PATH + CHILD_NAME, "remove");

    }

    @Test (groups = "wso2.greg", dependsOnMethods = "testAddDependencyFromChildCollection")
    public void testAddDependencyFromParentCollection ()
            throws AddAssociationRegistryExceptionException, RemoteException {

        String dependencyType = "depends";
        String todo = "add";
        relationServiceClient.addAssociation(PARENT_PATH + CHILD_NAME, dependencyType, PARENT_PATH, todo);
        DependenciesBean bean = relationServiceClient.getDependencies(PARENT_PATH + CHILD_NAME);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()),
                "Dependency type is not correct");
        assertTrue((PARENT_PATH.substring(0,
                PARENT_PATH.length() - 1)).equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()),
                "Target dependency is not correct");
        assertTrue(((PARENT_PATH + CHILD_NAME)).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()),
                "Source dependency is not correct");
        relationServiceClient.addAssociation(PARENT_PATH + CHILD_NAME, dependencyType, PARENT_PATH, "remove");

    }

    @Test (groups = "wso2.greg", dependsOnMethods = "testAddDependencyFromParentCollection")
    public void testAddDependencyFromOwnCollection ()
            throws AddAssociationRegistryExceptionException, RemoteException {

        String dependencyType = "depends";
        String todo = "add";
        relationServiceClient.addAssociation(PARENT_PATH, dependencyType, PARENT_PATH, todo);
        DependenciesBean bean = relationServiceClient.getDependencies(PARENT_PATH);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()),
                "Dependency type is not correct");
        assertTrue((PARENT_PATH.substring(0,
                PARENT_PATH.length() - 1)).equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()),
                "Target dependency is not correct");
        assertTrue((((PARENT_PATH.substring(0,
                PARENT_PATH.length() - 1)))).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()),
                "Source dependency is not correct");
        relationServiceClient.addAssociation(PARENT_PATH, dependencyType, PARENT_PATH, "remove");

    }

    @Test (groups = "wso2.greg", dependsOnMethods = "testAddDependencyFromOwnCollection")
    public void testAddWsdlAsADependency ()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException,
            AddAssociationRegistryExceptionException, XPathExpressionException {

        String resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                        File.separator + "GREG" + File.separator + "wsdl" +
                        File.separator + "AmazonWebServices.wsdl";
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminClient.addResource(ROOT + "AmazonWebServices.wsdl",
                "application/wsdl+xml", "testDesc", dh);
        assertTrue(resourceAdminClient.getResource(WSDL_PATH)[0].getAuthorUserName()
                .contains(automationContext.getContextTenant().getContextUser().getUserName()), "WSDL has not been added");
        String dependencyType = "depends";
        String todo = "add";
        relationServiceClient.addAssociation(PARENT_PATH, dependencyType, WSDL_PATH, todo);
        DependenciesBean bean = relationServiceClient.getDependencies(PARENT_PATH);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()),
                "Dependency type is not correct");
        assertTrue(WSDL_PATH.equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()),
                "Target dependency is not correct");
        assertTrue((((PARENT_PATH.substring(0,
                PARENT_PATH.length() - 1)))).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()),
                "Source dependency is not correct");
        //remove dependency
        relationServiceClient.addAssociation(PARENT_PATH, dependencyType, WSDL_PATH, "remove");
    }

    @Test (groups = "wso2.greg", dependsOnMethods = "testAddWsdlAsADependency")
    public void testAddPolicyAsADependency ()
            throws MalformedURLException, ResourceAdminServiceExceptionException,
            RemoteException, AddAssociationRegistryExceptionException {

        Boolean nameExists = false;
        String path = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                + "GREG" + File.separator + "policy" + File.separator + "policy.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addPolicy("desc 1", dataHandler);
        PolicyBean policyBean = listMetaDataServiceClient.listPolicies();
        String[] names = policyBean.getName();
        for (String name : names) {
            if (name.equalsIgnoreCase("policy.xml")) {
                nameExists = true;
            }
        }
        assertTrue(nameExists, "Policy does not exist.");
        String[] policyNames = listMetaDataServiceClient.listPolicies().getPath();
        for (String policyName : policyNames) {
            if (policyName.contains("policy.xml")) {
                pathToPolicy = "/_system/governance" + policyName;
            }
        }
        String dependencyType = "depends";
        String todo = "add";
        relationServiceClient.addAssociation(PARENT_PATH, dependencyType, pathToPolicy, todo);
        DependenciesBean bean = relationServiceClient.getDependencies(PARENT_PATH);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()),
                "Dependency type is not correct");
        assertTrue(pathToPolicy.equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()),
                "Target dependency is not correct");
        assertTrue((((PARENT_PATH.substring(0,
                PARENT_PATH.length() - 1)))).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()),
                "Source dependency is not correct");
        //remove dependency
        relationServiceClient.addAssociation(PARENT_PATH, dependencyType, pathToPolicy, "remove");
    }

    @Test (groups = "wso2.greg", dependsOnMethods = "testAddPolicyAsADependency")
    public void testAddSchemaAsADependency ()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException,
            AddAssociationRegistryExceptionException {

        String path = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                + "GREG" + File.separator + "schema" + File.separator + "books.xsd";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addSchema("desc 1", dataHandler);
        SchemaBean schemaBean = listMetaDataServiceClient.listSchemas();
        String[] names2 = schemaBean.getName();
        Boolean nameExists = false;
        for (String name : names2) {
            if (name.equalsIgnoreCase("books.xsd")) {
                nameExists = true;
            }
        }
        assertTrue(nameExists, "Schema does not exist.");
        String dependencyType = "depends";
        String todo = "add";
        relationServiceClient.addAssociation(PARENT_PATH, dependencyType, SCHEMA_PATH, todo);
        DependenciesBean bean = relationServiceClient.getDependencies(PARENT_PATH);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()),
                "Dependency type is not correct");
        assertTrue(SCHEMA_PATH.equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()),
                "Target dependency is not correct");
        assertTrue((((PARENT_PATH.substring(0,
                PARENT_PATH.length() - 1)))).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()),
                "Source dependency is not correct");
        //remove dependency
        relationServiceClient.addAssociation(PARENT_PATH, dependencyType, SCHEMA_PATH, "remove");
    }

    @Test (groups = "wso2.greg", dependsOnMethods = "testAddSchemaAsADependency")
    public void testAddRxt ()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException, XPathExpressionException {

        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" +
                File.separator + "rxt" + File.separator + "person.rxt";
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminClient.addResource(RXT_LOCATION, RXT_FILE_TYPE, "TstDec", dh);
        boolean found = false;
        for (ResourceData resource : resourceAdminClient.getResource(RXT_LOCATION)) {
            if (resource.getAuthorUserName().contains(automationContext.getContextTenant().getContextUser().getUserName())) {
                found = true;
            }
        }
        assertTrue(found, "Schema not found");
    }

    @Test (groups = "wso2.greg", dependsOnMethods = "testAddRxt")
    public void testAddResourceThroughRxt () throws RegistryException {

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "person");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("testPerson"));
        artifact.setAttribute("ID", "Person_id");
        artifact.setAttribute("Name", "Person_Name");
        artifactManager.addGenericArtifact(artifact);
        assertTrue(artifact.getQName().toString().contains("testPerson"), "artifact name not found");
    }

    @Test (groups = "wso2.greg", dependsOnMethods = "testAddResourceThroughRxt")
    public void testAddDependencyFromRxtResource ()
            throws AddAssociationRegistryExceptionException, RemoteException {

        String dependencyType = "depends";
        String todo = "add";
        relationServiceClient.addAssociation(PARENT_PATH, dependencyType, RXT_RESOURCE_PATH, todo);
        DependenciesBean bean = relationServiceClient.getDependencies(PARENT_PATH);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()),
                "Dependency type is not correct");
        assertTrue(RXT_RESOURCE_PATH.equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()),
                "Target dependency is not correct");
        assertTrue((((PARENT_PATH.substring(0,
                PARENT_PATH.length() - 1)))).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()),
                "Source dependency is not correct");
        //remove dependency
        relationServiceClient.addAssociation(PARENT_PATH, dependencyType, RXT_RESOURCE_PATH, "remove");

    }

    @AfterClass
    public void cleanUp () throws ResourceAdminServiceExceptionException, RemoteException {

        resourceAdminClient.deleteResource(PARENT_PATH.substring(0, PARENT_PATH.length() - 1));
        resourceAdminClient.deleteResource(RXT_LOCATION);
        resourceAdminClient.deleteResource(WSDL_PATH);
        resourceAdminClient.deleteResource(pathToPolicy);
        resourceAdminClient.deleteResource(RXT_RESOURCE_PATH);
        resourceAdminClient.deleteResource(SERVICE_PATH);
        resourceAdminClient.deleteResource(SCHEMA_PATH);
        resourceAdminClient = null;
        relationServiceClient = null;
        listMetaDataServiceClient = null;
        governance = null;
    }
}
