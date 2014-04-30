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

import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.RelationAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * TODO-This is an unnecessary test case. Remove!
 */

public class ResourceTestCase extends GREGIntegrationBaseTest{
    private static final String PATH = "/_system/config/test2";
    private ResourceAdminServiceClient resourceAdminClient;
    private RelationAdminServiceClient relationAdminServiceClient;

    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws LoginAuthenticationExceptionException, IOException,
            XPathExpressionException, URISyntaxException, SAXException, XMLStreamException {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        resourceAdminClient =
                new ResourceAdminServiceClient(getBackendURL(),
                                               getSessionCookie());
        relationAdminServiceClient =
                new RelationAdminServiceClient(getBackendURL(),
                                               getSessionCookie());
    }

    @Test(groups = "wso2.greg", description = "Add resource to greg")
    public void testAddResource()
            throws IOException, ResourceAdminServiceExceptionException, XPathExpressionException {

        String path =  FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addResource(PATH, "test/plain", "desc", dataHandler);
        assertTrue(resourceAdminClient.getResource(PATH)[0].getAuthorUserName().contains(automationContext.getContextTenant().getContextUser().getUserName()));
    }

    @Test(groups = "wso2.greg", description = "Add resource to greg", dependsOnMethods = "testAddResource")
    public void testAddAssociation()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException,
                   AddAssociationRegistryExceptionException {
        relationAdminServiceClient.addAssociation(PATH, "depends", "/_system", "add");
        assertTrue(relationAdminServiceClient.getDependencies(PATH).
                getAssociationBeans()[0].getAssociationType().equals("depends"));
        assertTrue(relationAdminServiceClient.getDependencies(PATH).
                getAssociationBeans()[0].getDestinationPath().equals("/_system"));
        assertTrue(relationAdminServiceClient.getDependencies(PATH).
                getAssociationBeans()[0].getSourcePath().equals(PATH));
    }

    @Test(groups = "wso2.greg", description = "Remove association", dependsOnMethods = "testAddAssociation")
    public void testRemoveAssociation()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException,
                   AddAssociationRegistryExceptionException {
        relationAdminServiceClient.addAssociation(PATH, "depends", "/_system", "remove");

        DependenciesBean bean = relationAdminServiceClient.getDependencies(PATH);
        assertNull(bean.getAssociationBeans(), "Remove association error");
    }

    @Test(alwaysRun = true, expectedExceptions = AxisFault.class,
          dependsOnMethods = "testRemoveAssociation", description = "delete resource")
    public void testCleanResources()
            throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.deleteResource(PATH);
        resourceAdminClient.getResource(PATH);
        resourceAdminClient = null;
        relationAdminServiceClient=null;
    }
}
