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
package org.wso2.carbon.registry.smoke.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceStub;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.info.stub.InfoAdminServiceStub;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.GetAssociationTreeRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class GovernanceRegistrySmokeTestCase {

    private String loggedInSessionCookie = "";
    private String frameworkPath = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        loggedInSessionCookie = util.login();
        frameworkPath = FrameworkSettings.getFrameworkPath();
    }

    @Test(groups = {"wso2.greg"})
    public void runBasicSmokeTests() {
        try {
            invokeResourceAndCollectionScenarios("/TestAutomation/SmokeTestCollection",
                    "/TestAutomation/SmokeTestResource.txt");

        } catch (Exception e) {
            Assert.fail("Exception thrown while running smoke tests: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"})
    public void runBasicSmokeTests2() {
        try {
            invokeResourceAndCollectionScenarios("/TestAutomation/Test/SmokeTestCollection",
                    "/TestAutomation/Test/SmokeTestResource.txt");

        } catch (Exception e) {
            Assert.fail("Exception thrown while running smoke tests: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"})
    public void runBasicSmokeTests3() {
        try {
            invokeResourceAndCollectionScenarios(
                    "/_system/governance/TestAutomation/Test/SmokeTestCollection",
                    "/_system/governance/TestAutomation/Test/SmokeTestResource.txt");

        } catch (Exception e) {
            Assert.fail("Exception thrown while running smoke tests: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"})
    public void runBasicSmokeTestsOnRoot() {
        try {
            invokeResourceAndCollectionScenariosForLinks(
                    "/SmokeTestCollectionActual", "/SmokeTestResourceActual.txt",
                    "/SmokeTestCollectionLink", "/SmokeTestResourceLink.txt");

        } catch (Exception e) {
            Assert.fail("Exception thrown while running smoke tests: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"})
    public void runSymbolicLinkTests1() {
        try {
            invokeResourceAndCollectionScenariosForLinks(
                    "/SmokeTestCollectionActual1", "/SmokeTestResourceActual1.txt",
                    "/SmokeTestCollectionLink1", "/SmokeTestResourceLink1.txt");

        } catch (Exception e) {
            Assert.fail("Exception thrown while running smoke tests: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"})
    public void runSymbolicLinkTests2() {
        try {
            invokeResourceAndCollectionScenariosForLinks(
                    "/TestAutomation/SmokeTestCollectionActual2",
                    "/TestAutomation/SmokeTestResourceActual2.txt",
                    "/SmokeTestCollectionLink2", "/SmokeTestResourceLink2.txt");

        } catch (Exception e) {
            Assert.fail("Exception thrown while running smoke tests: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"})
    public void runSymbolicLinkTests3() {
        try {
            invokeResourceAndCollectionScenariosForLinks(
                    "/TestAutomation/SmokeTestCollectionActual3",
                    "/TestAutomation/SmokeTestResourceActual3.txt",
                    "/TestAutomation/SmokeTestCollectionLink3",
                    "/TestAutomation/SmokeTestResourceLink3.txt");

        } catch (Exception e) {
            Assert.fail("Exception thrown while running smoke tests: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"})
    public void runSymbolicLinkTests4() {
        try {
            invokeResourceAndCollectionScenariosForLinks(
                    "/SmokeTestCollectionActual4", "/SmokeTestResourceActual4.txt",
                    "/TestAutomation/SmokeTestCollectionLink4",
                    "/TestAutomation/SmokeTestResourceLink4.txt");

        } catch (Exception e) {
            Assert.fail("Exception thrown while running smoke tests: " + e.getMessage());
        }
    }

    private void invokeResourceAndCollectionScenariosForLinks(String smokeTestCollection,
                                                              String smokeTestResource,
                                                              String smokeTestCollectionLink,
                                                              String smokeTestResourceLink)
            throws RemoteException, ResourceAdminServiceExceptionException, MalformedURLException,
            AddAssociationRegistryExceptionException, GetAssociationTreeRegistryExceptionException,
            RegistryExceptionException, CustomLifecyclesChecklistAdminServiceExceptionException,
            PropertiesAdminServiceRegistryExceptionException {
        smokeTestCollection(smokeTestCollection);
        smokeTestResource(smokeTestResource);
        smokeTestSymbolicLink(smokeTestCollectionLink, smokeTestCollection);
        smokeTestSymbolicLink(smokeTestResourceLink, smokeTestResource);
        smokeTestCommunityFeaturesWithComparison(smokeTestCollection, smokeTestResource,
                smokeTestCollectionLink, smokeTestResourceLink);
        smokeTestPropertiesWithComparison(smokeTestCollection, smokeTestResource,
                smokeTestCollectionLink, smokeTestResourceLink);
    }

    private void invokeResourceAndCollectionScenarios(String smokeTestCollection,
                                                      String smokeTestResource)
            throws RemoteException, ResourceAdminServiceExceptionException, MalformedURLException,
            AddAssociationRegistryExceptionException, GetAssociationTreeRegistryExceptionException,
            RegistryExceptionException, CustomLifecyclesChecklistAdminServiceExceptionException {
        smokeTestCollection(smokeTestCollection);
        smokeTestResource(smokeTestResource);
        smokeTestRelationships(smokeTestCollection, smokeTestResource);
        smokeTestCommunityFeatures(smokeTestCollection, smokeTestResource);
        smokeTestLifecycles(smokeTestCollection, smokeTestResource);
        //smokeTestSubscriptions(smokeTestCollection, smokeTestResource);
    }

    private void smokeTestLifecycles(String smokeTestCollection, String smokeTestResource)
            throws RemoteException, CustomLifecyclesChecklistAdminServiceExceptionException {
        CustomLifecyclesChecklistAdminServiceStub customLifecyclesChecklistAdminServiceStub =
                TestUtils.getCustomLifecyclesChecklistAdminServiceStub(loggedInSessionCookie);

        String[] lifeCycleItem = {"Code Completed", "WSDL, Schema Created", "QoS Created"};

        customLifecyclesChecklistAdminServiceStub
                .addAspect(smokeTestCollection, "ServiceLifeCycle");
        customLifecyclesChecklistAdminServiceStub
                .invokeAspect(smokeTestCollection, "ServiceLifeCycle", "Promote",
                        lifeCycleItem);
        LifecycleBean lifecycleBean = customLifecyclesChecklistAdminServiceStub
                .getLifecycleBean(smokeTestCollection);
        Property[] lifecycleProperties = lifecycleBean.getLifecycleProperties();
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals("registry.lifecycle.ServiceLifeCycle.state")) {
                Assert.assertEquals(property.getValues()[0], "Testing",
                        "Life-cycle not promoted");
            }
        }
        customLifecyclesChecklistAdminServiceStub
                .invokeAspect(smokeTestCollection, "ServiceLifeCycle", "Demote",
                        lifeCycleItem);
        lifecycleBean = customLifecyclesChecklistAdminServiceStub
                .getLifecycleBean(smokeTestCollection);
        lifecycleProperties = lifecycleBean.getLifecycleProperties();
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals("registry.lifecycle.ServiceLifeCycle.state")) {
                Assert.assertEquals(property.getValues()[0], "Development",
                        "Life-cycle not demoted");
            }
        }

        customLifecyclesChecklistAdminServiceStub
                .addAspect(smokeTestResource, "ServiceLifeCycle");
        customLifecyclesChecklistAdminServiceStub
                .invokeAspect(smokeTestResource, "ServiceLifeCycle", "Promote",
                        lifeCycleItem);
        lifecycleBean = customLifecyclesChecklistAdminServiceStub
                .getLifecycleBean(smokeTestResource);
        lifecycleProperties = lifecycleBean.getLifecycleProperties();
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals("registry.lifecycle.ServiceLifeCycle.state")) {
                Assert.assertEquals(property.getValues()[0], "Testing",
                        "Life-cycle not promoted");
            }
        }
        customLifecyclesChecklistAdminServiceStub
                .invokeAspect(smokeTestResource, "ServiceLifeCycle", "Demote",
                        lifeCycleItem);
        lifecycleBean = customLifecyclesChecklistAdminServiceStub
                .getLifecycleBean(smokeTestResource);
        lifecycleProperties = lifecycleBean.getLifecycleProperties();
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals("registry.lifecycle.ServiceLifeCycle.state")) {
                Assert.assertEquals(property.getValues()[0], "Development",
                        "Life-cycle not demoted");
            }
        }
    }

    private void smokeTestCommunityFeatures(String smokeTestCollection, String smokeTestResource)
            throws RemoteException, RegistryExceptionException {
        smokeTestCommunityFeaturesWithComparison(smokeTestCollection, smokeTestResource,
                smokeTestCollection, smokeTestResource);
    }

    private void smokeTestPropertiesWithComparison(String smokeTestCollection,
                                                   String smokeTestResource,
                                                   String smokeTestCollectionVerify,
                                                   String smokeTestResourceVerify)
            throws RemoteException, RegistryExceptionException,
            PropertiesAdminServiceRegistryExceptionException {
        PropertiesAdminServiceStub propertiesAdminServiceStub =
                TestUtils.getPropertiesAdminServiceStub(loggedInSessionCookie);

        propertiesAdminServiceStub.setProperty(smokeTestCollectionVerify, "foo", "bar");
        boolean found = false;
        for (org.wso2.carbon.registry.properties.stub.utils.xsd.Property property :
                propertiesAdminServiceStub.getProperties(smokeTestCollection, "no").getProperties()) {
            if (property.getKey().equals("foo")) {
                found = true;
                Assert.assertEquals(property.getValue(), "bar");
                break;
            }
        }
        Assert.assertTrue(found, "The property was not found");

        propertiesAdminServiceStub.setProperty(smokeTestResourceVerify, "foo", "bar");
        found = false;
        for (org.wso2.carbon.registry.properties.stub.utils.xsd.Property property :
                propertiesAdminServiceStub.getProperties(smokeTestResource, "no").getProperties()) {
            if (property.getKey().equals("foo")) {
                found = true;
                Assert.assertEquals(property.getValue(), "bar");
                break;
            }
        }
        Assert.assertTrue(found, "The property was not found");
    }

    private void smokeTestCommunityFeaturesWithComparison(String smokeTestCollection,
                                                          String smokeTestResource,
                                                          String smokeTestCollectionVerify,
                                                          String smokeTestResourceVerify)
            throws RemoteException, RegistryExceptionException {
        InfoAdminServiceStub infoAdminServiceStub =
                TestUtils.getInfoAdminServiceStub(loggedInSessionCookie);
        infoAdminServiceStub.addComment("this is sample comment", smokeTestCollection,
                loggedInSessionCookie);
        infoAdminServiceStub.addComment("this is sample comment2", smokeTestCollection,
                loggedInSessionCookie);
        Comment[] comment = infoAdminServiceStub.getComments(smokeTestCollectionVerify,
                loggedInSessionCookie).getComments();
        Assert.assertTrue(
                comment[0].getDescription().equalsIgnoreCase("this is sample comment"),
                "added comment was not found");
        Assert.assertTrue(
                comment[1].getDescription().equalsIgnoreCase("this is sample comment2"),
                "added comment was not found");
        infoAdminServiceStub.removeComment(comment[0].getCommentPath(), loggedInSessionCookie);
        comment = infoAdminServiceStub.getComments(smokeTestCollectionVerify,
                loggedInSessionCookie).getComments();
        Assert.assertTrue(
                comment[0].getDescription().equalsIgnoreCase("this is sample comment2"),
                "comment was not removed");

        infoAdminServiceStub.addComment("this is sample comment", smokeTestResource,
                loggedInSessionCookie);
        infoAdminServiceStub.addComment("this is sample comment2", smokeTestResource,
                loggedInSessionCookie);
        comment = infoAdminServiceStub.getComments(smokeTestResourceVerify,
                loggedInSessionCookie).getComments();
        Assert.assertTrue(
                comment[0].getDescription().equalsIgnoreCase("this is sample comment"),
                "added comment was not found");
        Assert.assertTrue(
                comment[1].getDescription().equalsIgnoreCase("this is sample comment2"),
                "added comment was not found");
        infoAdminServiceStub.removeComment(comment[0].getCommentPath(), loggedInSessionCookie);
        comment = infoAdminServiceStub.getComments(smokeTestResourceVerify,
                loggedInSessionCookie).getComments();
        Assert.assertTrue(
                comment[0].getDescription().equalsIgnoreCase("this is sample comment2"),
                "comment was not removed");

        infoAdminServiceStub.addTag("foo", smokeTestCollection, loggedInSessionCookie);
        Assert.assertEquals(infoAdminServiceStub.getTags(smokeTestCollectionVerify,
                loggedInSessionCookie).getTags().length, 1, "the tag was not added");
        infoAdminServiceStub.removeTag("foo", smokeTestCollection, loggedInSessionCookie);
        Assert.assertNull(infoAdminServiceStub.getTags(smokeTestCollectionVerify,
                loggedInSessionCookie).getTags(), "the tag was not removed");

        infoAdminServiceStub.addTag("foo", smokeTestResource, loggedInSessionCookie);
        Assert.assertEquals(infoAdminServiceStub.getTags(smokeTestResourceVerify,
                loggedInSessionCookie).getTags().length, 1, "the tag was not added");
        infoAdminServiceStub.removeTag("foo", smokeTestResource, loggedInSessionCookie);
        Assert.assertNull(infoAdminServiceStub.getTags(smokeTestResourceVerify,
                loggedInSessionCookie).getTags(), "the tag was not removed");

        infoAdminServiceStub.rateResource("5", smokeTestCollection, loggedInSessionCookie);
        Assert.assertEquals(
                infoAdminServiceStub.getRatings(smokeTestCollectionVerify, loggedInSessionCookie)
                        .getAverageRating(), 5.0f, "the ratings do not tally");
        Assert.assertEquals(
                infoAdminServiceStub.getRatings(smokeTestCollectionVerify, loggedInSessionCookie)
                        .getUserRating(), 5, "the ratings do not tally");
        infoAdminServiceStub.rateResource("0", smokeTestCollection, loggedInSessionCookie);
        Assert.assertEquals(
                infoAdminServiceStub.getRatings(smokeTestCollectionVerify, loggedInSessionCookie)
                        .getAverageRating(), 0.0f, "the ratings do not tally");
        Assert.assertEquals(
                infoAdminServiceStub.getRatings(smokeTestCollectionVerify, loggedInSessionCookie)
                        .getUserRating(), 0, "the ratings do not tally");

        infoAdminServiceStub.rateResource("5", smokeTestResource, loggedInSessionCookie);
        Assert.assertEquals(
                infoAdminServiceStub.getRatings(smokeTestResourceVerify, loggedInSessionCookie)
                        .getAverageRating(), 5.0f, "the ratings do not tally");
        Assert.assertEquals(
                infoAdminServiceStub.getRatings(smokeTestResourceVerify, loggedInSessionCookie)
                        .getUserRating(), 5, "the ratings do not tally");
        infoAdminServiceStub.rateResource("0", smokeTestResource, loggedInSessionCookie);
        Assert.assertEquals(
                infoAdminServiceStub.getRatings(smokeTestResourceVerify, loggedInSessionCookie)
                        .getAverageRating(), 0.0f, "the ratings do not tally");
        Assert.assertEquals(
                infoAdminServiceStub.getRatings(smokeTestResourceVerify, loggedInSessionCookie)
                        .getUserRating(), 0, "the ratings do not tally");
    }

    private void smokeTestSubscriptions(String smokeTestCollection, String smokeTestResource)
            throws RemoteException, RegistryExceptionException {
        InfoAdminServiceStub infoAdminServiceStub =
                TestUtils.getInfoAdminServiceStub(loggedInSessionCookie);
        Assert.assertEquals(infoAdminServiceStub.subscribe(smokeTestCollection,
                "https://localhost:9443/services/Hello", "CollectionUpdated",
                loggedInSessionCookie).getSubscriptionInstances().length, 1,
                "subscription failed");
        SubscriptionInstance[] instances =
                infoAdminServiceStub.subscribe(smokeTestCollection,
                        "digest://h/mailto:dev@wso2.org", "CollectionUpdated",
                        loggedInSessionCookie).getSubscriptionInstances();
        Assert.assertEquals(instances.length, 1, "subscription failed");
        Assert.assertEquals(infoAdminServiceStub.subscribeREST(smokeTestCollection,
                "https://localhost:9443/services/RESTHello", "CollectionUpdated",
                loggedInSessionCookie).getSubscriptionInstances().length, 1,
                "subscription failed");
        Assert.assertEquals(instances[0].getDigestType(), "h",
                "invalid digest type");
        Assert.assertEquals(instances[0].getAddress(),
                "digest://h/mailto:dev@wso2.org", "invalid address");
        Assert.assertEquals(instances[0].getNotificationMethod(),
                "email", "invalid notification method");
        Assert.assertTrue(infoAdminServiceStub.unsubscribe(smokeTestCollection,
                infoAdminServiceStub.getSubscriptions(smokeTestCollection,
                        loggedInSessionCookie).getSubscriptionInstances()[0].getId(),
                loggedInSessionCookie), "failed to unsubscribe");
        Assert.assertEquals(infoAdminServiceStub.getSubscriptions(smokeTestCollection,
                loggedInSessionCookie).getSubscriptionInstances().length, 2,
                "failed to unsubscribe");
        Assert.assertTrue(infoAdminServiceStub.unsubscribe(smokeTestCollection,
                infoAdminServiceStub.getSubscriptions(smokeTestCollection,
                        loggedInSessionCookie).getSubscriptionInstances()[0].getId(),
                loggedInSessionCookie), "failed to unsubscribe");
        Assert.assertEquals(infoAdminServiceStub.getSubscriptions(smokeTestCollection,
                loggedInSessionCookie).getSubscriptionInstances().length, 1,
                "failed to unsubscribe");

        Assert.assertEquals(infoAdminServiceStub.subscribe(smokeTestResource,
                "https://localhost:9443/services/Hello", "ResourceUpdated",
                loggedInSessionCookie).getSubscriptionInstances().length, 1,
                "subscription failed");
        instances = infoAdminServiceStub.subscribe(smokeTestResource,
                "digest://h/mailto:dev@wso2.org", "ResourceUpdated",
                loggedInSessionCookie).getSubscriptionInstances();
        Assert.assertEquals(instances.length, 1, "subscription failed");
        Assert.assertEquals(infoAdminServiceStub.subscribeREST(smokeTestResource,
                "https://localhost:9443/services/RESTHello", "ResourceUpdated",
                loggedInSessionCookie).getSubscriptionInstances().length, 1,
                "subscription failed");
        Assert.assertEquals(instances[0].getDigestType(), "h",
                "invalid digest type");
        Assert.assertEquals(instances[0].getAddress(),
                "digest://h/mailto:dev@wso2.org", "invalid address");
        Assert.assertEquals(instances[0].getNotificationMethod(),
                "email", "invalid notification method");
        Assert.assertTrue(infoAdminServiceStub.unsubscribe(smokeTestResource,
                infoAdminServiceStub.getSubscriptions(smokeTestResource,
                        loggedInSessionCookie).getSubscriptionInstances()[0].getId(),
                loggedInSessionCookie), "failed to unsubscribe");
        Assert.assertEquals(infoAdminServiceStub.getSubscriptions(smokeTestResource,
                loggedInSessionCookie).getSubscriptionInstances().length, 2,
                "failed to unsubscribe");
        Assert.assertTrue(infoAdminServiceStub.unsubscribe(smokeTestResource,
                infoAdminServiceStub.getSubscriptions(smokeTestResource,
                        loggedInSessionCookie).getSubscriptionInstances()[0].getId(),
                loggedInSessionCookie), "failed to unsubscribe");
        Assert.assertEquals(infoAdminServiceStub.getSubscriptions(smokeTestResource,
                loggedInSessionCookie).getSubscriptionInstances().length, 1,
                "failed to unsubscribe");
    }

    private void smokeTestRelationships(String smokeTestCollection, String smokeTestResource)
            throws RemoteException, AddAssociationRegistryExceptionException,
            GetAssociationTreeRegistryExceptionException {
        RelationAdminServiceStub relationAdminServiceStub =
                TestUtils.getRelationAdminServiceStub(loggedInSessionCookie);
        relationAdminServiceStub.addAssociation(smokeTestCollection,
                "uses", smokeTestResource, "add");
        Assert.assertTrue(relationAdminServiceStub.getAssociationTree(
                smokeTestCollection, "uses").getAssociationTree().contains(
                smokeTestResource), "The association was not added");

        relationAdminServiceStub =
                TestUtils.getRelationAdminServiceStub(loggedInSessionCookie);
        relationAdminServiceStub.addAssociation(smokeTestResource,
                "usedBy", smokeTestCollection, "add");
        Assert.assertTrue(relationAdminServiceStub.getAssociationTree(
                smokeTestResource, "usedBy").getAssociationTree().contains(
                smokeTestCollection), "The association was not added");

        relationAdminServiceStub =
                TestUtils.getRelationAdminServiceStub(loggedInSessionCookie);
        relationAdminServiceStub.addAssociation(smokeTestCollection,
                "depends", smokeTestResource, "add");
        Assert.assertTrue(relationAdminServiceStub.getAssociationTree(
                smokeTestCollection, "depends").getAssociationTree().contains(
                smokeTestResource), "The association was not added");

        relationAdminServiceStub =
                TestUtils.getRelationAdminServiceStub(loggedInSessionCookie);
        relationAdminServiceStub.addAssociation(smokeTestResource,
                "depends", smokeTestCollection, "add");
        Assert.assertTrue(relationAdminServiceStub.getAssociationTree(
                smokeTestResource, "depends").getAssociationTree().contains(
                smokeTestCollection), "The association was not added");

        relationAdminServiceStub.addAssociation(smokeTestCollection,
                "uses", smokeTestResource, "delete");
        Assert.assertFalse(relationAdminServiceStub.getAssociationTree(
                smokeTestCollection, "uses").getAssociationTree().contains(
                smokeTestResource), "The association was not added");

        relationAdminServiceStub =
                TestUtils.getRelationAdminServiceStub(loggedInSessionCookie);
        relationAdminServiceStub.addAssociation(smokeTestResource,
                "usedBy", smokeTestCollection, "delete");
        Assert.assertFalse(relationAdminServiceStub.getAssociationTree(
                smokeTestResource, "usedBy").getAssociationTree().contains(
                smokeTestCollection), "The association was not added");

        relationAdminServiceStub =
                TestUtils.getRelationAdminServiceStub(loggedInSessionCookie);
        relationAdminServiceStub.addAssociation(smokeTestCollection,
                "depends", smokeTestResource, "delete");
        Assert.assertFalse(relationAdminServiceStub.getAssociationTree(
                smokeTestCollection, "depends").getAssociationTree().contains(
                smokeTestResource), "The association was not added");

        relationAdminServiceStub =
                TestUtils.getRelationAdminServiceStub(loggedInSessionCookie);
        relationAdminServiceStub.addAssociation(smokeTestResource,
                "depends", smokeTestCollection, "delete");
        Assert.assertFalse(relationAdminServiceStub.getAssociationTree(
                smokeTestResource, "depends").getAssociationTree().contains(
                smokeTestCollection), "The association was not added");
    }

    private void smokeTestCollection(String smokeTestCollection)
            throws RemoteException, ResourceAdminServiceExceptionException {
        ResourceAdminServiceStub resourceAdminServiceStub = TestUtils
                .getResourceAdminServiceStub(loggedInSessionCookie);
        resourceAdminServiceStub.addCollection(RegistryUtils.getParentPath(smokeTestCollection),
                RegistryUtils.getResourceName(smokeTestCollection), "", "A smoke test collection.");
        CollectionContentBean collectionContentBean =
                resourceAdminServiceStub.getCollectionContent(RegistryUtils.getParentPath(
                        smokeTestCollection));
        if (collectionContentBean.getChildCount() > 0) {
            String[] childPath = collectionContentBean.getChildPaths();
            boolean found = false;
            for (int i = 0; i <= childPath.length - 1; i++) {
                if (childPath[i].equalsIgnoreCase(smokeTestCollection)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found, "The newly added collection was not found");
        } else {
            Assert.fail("No children found under root");
        }
    }

    private void smokeTestSymbolicLink(String smokeTestLink, String path)
            throws RemoteException, ResourceAdminServiceExceptionException {
        ResourceAdminServiceStub resourceAdminServiceStub = TestUtils
                .getResourceAdminServiceStub(loggedInSessionCookie);
        resourceAdminServiceStub.addSymbolicLink(RegistryUtils.getParentPath(smokeTestLink),
                RegistryUtils.getResourceName(smokeTestLink), path);
        CollectionContentBean collectionContentBean =
                resourceAdminServiceStub.getCollectionContent(RegistryUtils.getParentPath(
                        smokeTestLink));
        if (collectionContentBean.getChildCount() > 0) {
            String[] childPath = collectionContentBean.getChildPaths();
            boolean found = false;
            for (int i = 0; i <= childPath.length - 1; i++) {
                if (childPath[i].equalsIgnoreCase(smokeTestLink)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found, "The newly added link was not found");
        } else {
            Assert.fail("No children found under root");
        }
    }

    private void smokeTestResource(String smokeTestResource)
            throws RemoteException, ResourceAdminServiceExceptionException, MalformedURLException {
        ResourceAdminServiceStub resourceAdminServiceStub = TestUtils
                .getResourceAdminServiceStub(loggedInSessionCookie);
        String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                "resources" + File.separator + "sampleText.txt";
        resourceAdminServiceStub.addResource(smokeTestResource, "text/plain",
                "A smoke test",
                new DataHandler(new URL("file:///" + resource)), null, null);
        CollectionContentBean collectionContentBean =
                resourceAdminServiceStub.getCollectionContent(
                        RegistryUtils.getParentPath(smokeTestResource));
        if (collectionContentBean.getChildCount() > 0) {
            String[] childPath = collectionContentBean.getChildPaths();
            boolean found = false;
            for (int i = 0; i <= childPath.length - 1; i++) {
                if (childPath[i].equalsIgnoreCase(smokeTestResource)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found, "The newly added resource was not found");
        } else {
            Assert.fail("No children found under root");
        }
    }

}
