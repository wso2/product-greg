package org.wso2.carbon.registry.metadata.test.schema;

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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.RatingBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.metadata.test.util.RegistryConstants;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.RelationAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import java.rmi.RemoteException;

import static org.testng.Assert.*;


/**
 * A test case which tests registry Schema add meta data operation
 */

public class SchemaAddMetadataTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(SchemaAddTestCase.class);
    private String schemaPath = "/_system/governance/trunk/schemas/";
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private LifeCycleAdminServiceClient lifecyclesChecklistAdminServiceclient;
    private InfoServiceAdminClient infoAdminServiceclient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private String resourceName = "company.xsd";
    private String referenceSchemaFile = "person.xsd";
    private String sessionCookie;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing SchemaAddMetadata Tests");
        log.debug("SchemaAddMetadataTestCase Initialised");
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = new LoginLogoutClient(automationContext).login();

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);

        lifecyclesChecklistAdminServiceclient =
                new LifeCycleAdminServiceClient(backendURL, sessionCookie);

        infoAdminServiceclient =
                new InfoServiceAdminClient(backendURL, sessionCookie);

        relationAdminServiceClient =
                new RelationAdminServiceClient(backendURL, sessionCookie);

    }


    @Test(groups = {"wso2.greg"}, description = "add Schema Multiple Times")
    public void addSchemaMultipleImports()
            throws Exception, RemoteException, ResourceAdminServiceExceptionException {
        String resourceUrl =
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/company.xsd";
        String resourceName = "company.xsd";
        String referenceSchemaFile = "person.xsd";
        resourceAdminServiceClient.importResource(schemaPath, resourceName,
                                                  RegistryConstants.APPLICATION_X_XSD_XML,
                "schemaFile", resourceUrl, null);
        String textContent = resourceAdminServiceClient.getTextContent(schemaPath +
                                                                       "org/charitha/1.0.0/" + resourceName);
        assertNotEquals(textContent.indexOf("xmlns:tns=\"http://charitha.org/\""), -1);
        String textContentImportedSchema = resourceAdminServiceClient.getTextContent(schemaPath +
                                                                                     "org1/charitha/1.0.0/" + referenceSchemaFile);
        assertNotEquals(textContentImportedSchema.indexOf("xmlns:tns=\"http://charitha.org1/\""), -1);
    }


    /**
     * Check associations of uploaded schema
     */
    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addSchemaMultipleImports", description = "Check associations of uploaded schema")
    public void associationTest() throws AddAssociationRegistryExceptionException, RemoteException {
        AssociationTreeBean associationTreeBean = null;

        //check association is in position
        associationTreeBean = relationAdminServiceClient.getAssociationTree(schemaPath +
                                                                            "org1/charitha/1.0.0/" + referenceSchemaFile, "association");
        assertTrue(associationTreeBean.getAssociationTree().contains(schemaPath + "org/charitha/1.0.0/" + resourceName));

    }

    /**
     * Check dependencies of uploaded schema
     */
    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addSchemaMultipleImports", description = "Check dependencies of uploaded schema")
    public void dependencyTest() throws AddAssociationRegistryExceptionException, RemoteException {
        AssociationTreeBean associationTreeBean = null;
        associationTreeBean = relationAdminServiceClient.getAssociationTree(schemaPath + "org/charitha/1.0.0/", "depends");
        assertTrue(!associationTreeBean.getAssociationTree().contains(schemaPath +
                                                                      "org1/charitha/1.0.0/" + referenceSchemaFile));


    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addSchemaMultipleImports")
    public void addCommentTest() throws RegistryException, AxisFault, RegistryExceptionException {
        infoAdminServiceclient.addComment("This is a sample comment for main " +
                                          "schema file", schemaPath + "org/charitha/1.0.0/" + resourceName, sessionCookie);
        CommentBean commentBean = infoAdminServiceclient.getComments(schemaPath + "org/charitha/1.0.0/" + resourceName, sessionCookie);
        Comment[] comment = commentBean.getComments();
        assertTrue(comment[0].getDescription().equalsIgnoreCase("This is a sample comment for main " +
                                                                "schema file"));

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addSchemaMultipleImports")
    public void tagTest() throws RegistryException, AxisFault, RegistryExceptionException {
        TagBean tagBean;
        infoAdminServiceclient.addTag("TestTag", schemaPath + "org/charitha/1.0.0/", sessionCookie);
        tagBean = infoAdminServiceclient.getTags(schemaPath + "org/charitha/1.0.0/", sessionCookie);
        Tag[] tag = tagBean.getTags();
        for (int i = 0; i <= tag.length - 1; i++) {
            assertTrue(tag[i].getTagName().equalsIgnoreCase("TestTag"));
        }

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addSchemaMultipleImports")
    public void rateTest() throws RegistryException, RegistryExceptionException {
        RatingBean ratingBean;

        infoAdminServiceclient.rateResource("3", schemaPath + "org/charitha/1.0.0/", sessionCookie);
        ratingBean = infoAdminServiceclient.getRatings(schemaPath + "org/charitha/1.0.0/", sessionCookie);
        int rateIntValue = Integer.parseInt("3");
        assertEquals(ratingBean.getUserRating(), rateIntValue);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addSchemaMultipleImports")
    public void lifeCycleTest() throws Exception {
        String[] lifeCycleItem = {"Code Completed", "WSDL, Schema Created", "QoS Created"};
        lifecyclesChecklistAdminServiceclient.addAspect(schemaPath + "org/charitha/1.0.0/", "ServiceLifeCycle");
        lifecyclesChecklistAdminServiceclient.invokeAspect(schemaPath + "org/charitha/1.0.0/", "ServiceLifeCycle", "Promote", lifeCycleItem);
        LifecycleBean lifecycleBean = lifecyclesChecklistAdminServiceclient.getLifecycleBean(schemaPath + "org/charitha/1.0.0/");
        Property[] lifecycleProperties = lifecycleBean.getLifecycleProperties();
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals("registry.lifecycle.ServiceLifeCycle.state")) {
                assertTrue("Testing".equalsIgnoreCase(property.getValues()[0]));
            }
        }


    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addSchemaMultipleImports")
    public void addCommentReferenceFileTest()
            throws RegistryException, AxisFault, RegistryExceptionException {
        //add comement
        infoAdminServiceclient.addComment("This is a sample comment for main " +
                                          "schema file", schemaPath +
                                                         "org1/charitha/1.0.0/" + referenceSchemaFile, sessionCookie);
        CommentBean commentBean = infoAdminServiceclient.getComments(schemaPath +
                                                                     "org1/charitha/1.0.0/" + referenceSchemaFile, sessionCookie);
        Comment[] comment = commentBean.getComments();
        assertTrue(comment[0].getDescription().equalsIgnoreCase("This is a sample comment for main " +
                                                                "schema file"));

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addSchemaMultipleImports")
    public void tagReferenceFileTest()
            throws RegistryException, AxisFault, RegistryExceptionException {
        TagBean tagBean;
        infoAdminServiceclient.addTag("TestTag", schemaPath +
                                                 "org1/charitha/1.0.0/", sessionCookie);
        tagBean = infoAdminServiceclient.getTags(schemaPath +
                                                 "org1/charitha/1.0.0/", sessionCookie);
        Tag[] tag = tagBean.getTags();
        for (int i = 0; i <= tag.length - 1; i++) {
            assertTrue(tag[i].getTagName().equalsIgnoreCase("TestTag"));
        }

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addSchemaMultipleImports")
    public void rateReferenceFileTest() throws RegistryException, RegistryExceptionException {
        RatingBean ratingBean;

        infoAdminServiceclient.rateResource("3", schemaPath +
                                                 "org1/charitha/1.0.0/", sessionCookie);
        ratingBean = infoAdminServiceclient.getRatings(schemaPath +
                                                       "org1/charitha/1.0.0/", sessionCookie);
        int rateIntValue = Integer.parseInt("3");
        assertEquals(ratingBean.getUserRating(), rateIntValue);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addSchemaMultipleImports")
    public void lifeCycleReferenceFileTest() throws Exception {
        String[] lifeCycleItem = {"Code Completed", "WSDL, Schema Created", "QoS Created"};
        lifecyclesChecklistAdminServiceclient.addAspect(schemaPath +
                                                        "org1/charitha/1.0.0/", "ServiceLifeCycle");
        lifecyclesChecklistAdminServiceclient.invokeAspect(schemaPath +
                                                           "org1/charitha/1.0.0/", "ServiceLifeCycle", "Promote", lifeCycleItem);
        LifecycleBean lifecycleBean = lifecyclesChecklistAdminServiceclient.getLifecycleBean(schemaPath +
                                                                                             "org1/charitha/1.0.0/");
        Property[] lifecycleProperties = lifecycleBean.getLifecycleProperties();
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals("registry.lifecycle.ServiceLifeCycle.state")) {
                assertTrue("Testing".equalsIgnoreCase(property.getValues()[0]));
            }
        }


    }


    @AfterClass(groups = "wso2.greg")
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException,
                                         CustomLifecyclesChecklistAdminServiceExceptionException,
                                         RegistryException, RegistryExceptionException {
        lifecyclesChecklistAdminServiceclient.removeAspect(schemaPath + "org/charitha/1.0.0/", "ServiceLifeCycle");
        lifecyclesChecklistAdminServiceclient.removeAspect(schemaPath + "org1/charitha/1.0.0/", "ServiceLifeCycle");
        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "org/charitha/1.0.0/" + resourceName);
        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "org1/charitha/1.0.0/" + referenceSchemaFile);
        lifecyclesChecklistAdminServiceclient = null;
        resourceAdminServiceClient = null;
        resourceAdminServiceClient = null;
        relationAdminServiceClient = null;
        infoAdminServiceclient = null;
        resourceName = null;

    }
}
