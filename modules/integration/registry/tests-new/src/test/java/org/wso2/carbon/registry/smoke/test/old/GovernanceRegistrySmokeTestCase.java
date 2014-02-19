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

package org.wso2.carbon.registry.smoke.test.old;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.GetAssociationTreeRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class GovernanceRegistrySmokeTestCase {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private ManageEnvironment environment;
    private RelationAdminServiceClient relationAdminServiceClient;

    @BeforeClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void init() throws Exception {
        int userId = 1;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);

        environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(1);

        resourceAdminServiceClient = new ResourceAdminServiceClient(environment
                .getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());

        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        infoServiceAdminClient = new InfoServiceAdminClient(environment
                .getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        propertiesAdminServiceClient = new PropertiesAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        relationAdminServiceClient = new RelationAdminServiceClient(environment
                .getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());

    }

    @Test(groups = {"wso2.greg"}, description = "basic Smoke tests")
    public void runBasicSmokeTests() throws RemoteException,
            MalformedURLException, RegistryException,
            ResourceAdminServiceExceptionException,
            AddAssociationRegistryExceptionException,
            GetAssociationTreeRegistryExceptionException,
            RegistryExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException {

        invokeResourceAndCollectionScenarios(
                "/TestAutomation/SmokeTestCollection",
                "/TestAutomation/SmokeTestResource.txt");

    }

    @Test(groups = {"wso2.greg"}, description = "second basic Smoke tests")
    public void runBasicSmokeTests2() throws RemoteException,
            MalformedURLException, RegistryException,
            ResourceAdminServiceExceptionException,
            AddAssociationRegistryExceptionException,
            GetAssociationTreeRegistryExceptionException,
            RegistryExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException {

        invokeResourceAndCollectionScenarios(
                "/TestAutomation/Test/SmokeTestCollection",
                "/TestAutomation/Test/SmokeTestResource.txt");

    }

    @Test(groups = {"wso2.greg"}, description = "third basic Smoke tests")
    public void runBasicSmokeTests3() throws RemoteException,
            MalformedURLException, RegistryException,
            ResourceAdminServiceExceptionException,
            AddAssociationRegistryExceptionException,
            GetAssociationTreeRegistryExceptionException,
            RegistryExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException {

        invokeResourceAndCollectionScenarios(
                "/_system/governance/TestAutomation/Test/SmokeTestCollection",
                "/_system/governance/TestAutomation/Test/SmokeTestResource.txt");

    }

    @Test(groups = {"wso2.greg"}, description = "basic Smoke tests on root")
    public void runBasicSmokeTestsOnRoot() throws RemoteException,
            MalformedURLException, RegistryException,
            ResourceAdminServiceExceptionException,
            AddAssociationRegistryExceptionException,
            GetAssociationTreeRegistryExceptionException,
            RegistryExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            PropertiesAdminServiceRegistryExceptionException {

        invokeResourceAndCollectionScenariosForLinks(
                "/SmokeTestCollectionActual", "/SmokeTestResourceActual.txt",
                "/SmokeTestCollectionLink", "/SmokeTestResourceLink.txt");

    }

    @Test(groups = {"wso2.greg"}, description = " link basic Smoke test1")
    public void runSymbolicLinkTests1() throws RemoteException,
            MalformedURLException, RegistryException,
            ResourceAdminServiceExceptionException,
            AddAssociationRegistryExceptionException,
            GetAssociationTreeRegistryExceptionException,
            RegistryExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            PropertiesAdminServiceRegistryExceptionException {

        invokeResourceAndCollectionScenariosForLinks(
                "/SmokeTestCollectionActual1", "/SmokeTestResourceActual1.txt",
                "/SmokeTestCollectionLink1", "/SmokeTestResourceLink1.txt");

    }

    @Test(groups = {"wso2.greg"}, description = " link basic Smoke test2")
    public void runSymbolicLinkTests2() throws RemoteException,
            MalformedURLException, RegistryException,
            ResourceAdminServiceExceptionException,
            AddAssociationRegistryExceptionException,
            GetAssociationTreeRegistryExceptionException,
            RegistryExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            PropertiesAdminServiceRegistryExceptionException {

        invokeResourceAndCollectionScenariosForLinks(
                "/TestAutomation/SmokeTestCollectionActual2",
                "/TestAutomation/SmokeTestResourceActual2.txt",
                "/SmokeTestCollectionLink2", "/SmokeTestResourceLink2.txt");

    }

    @Test(groups = {"wso2.greg"}, description = " link basic Smoke test3")
    public void runSymbolicLinkTests3() throws RemoteException,
            MalformedURLException, RegistryException,
            ResourceAdminServiceExceptionException,
            AddAssociationRegistryExceptionException,
            GetAssociationTreeRegistryExceptionException,
            RegistryExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            PropertiesAdminServiceRegistryExceptionException {

        invokeResourceAndCollectionScenariosForLinks(
                "/TestAutomation/SmokeTestCollectionActual3",
                "/TestAutomation/SmokeTestResourceActual3.txt",
                "/TestAutomation/SmokeTestCollectionLink3",
                "/TestAutomation/SmokeTestResourceLink3.txt");

    }

    @Test(groups = {"wso2.greg"}, description = " link basic Smoke test4")
    public void runSymbolicLinkTests4() throws RemoteException,
            MalformedURLException, RegistryException,
            ResourceAdminServiceExceptionException,
            AddAssociationRegistryExceptionException,
            GetAssociationTreeRegistryExceptionException,
            RegistryExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            PropertiesAdminServiceRegistryExceptionException {

        invokeResourceAndCollectionScenariosForLinks(
                "/SmokeTestCollectionActual4", "/SmokeTestResourceActual4.txt",
                "/TestAutomation/SmokeTestCollectionLink4",
                "/TestAutomation/SmokeTestResourceLink4.txt");

    }

    private void invokeResourceAndCollectionScenariosForLinks(
            String smokeTestCollection, String smokeTestResource,
            String smokeTestCollectionLink, String smokeTestResourceLink)
            throws RemoteException, ResourceAdminServiceExceptionException,
            MalformedURLException, RegistryExceptionException,
            PropertiesAdminServiceRegistryExceptionException, RegistryException {

        smokeTestCollection(smokeTestCollection);
        smokeTestResource(smokeTestResource);
        smokeTestSymbolicLink(smokeTestCollectionLink, smokeTestCollection);
        smokeTestSymbolicLink(smokeTestResourceLink, smokeTestResource);
        smokeTestCommunityFeaturesWithComparison(smokeTestCollection,
                smokeTestResource, smokeTestCollectionLink,
                smokeTestResourceLink);
        smokeTestPropertiesWithComparison(smokeTestCollection,
                smokeTestResource, smokeTestCollectionLink,
                smokeTestResourceLink);
    }

    private void invokeResourceAndCollectionScenarios(
            String smokeTestCollection, String smokeTestResource)
            throws RemoteException, ResourceAdminServiceExceptionException,
            MalformedURLException, AddAssociationRegistryExceptionException,
            GetAssociationTreeRegistryExceptionException,
            RegistryExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            RegistryException {
        smokeTestCollection(smokeTestCollection);
        smokeTestResource(smokeTestResource);
        smokeTestRelationships(smokeTestCollection, smokeTestResource);
        smokeTestCommunityFeatures(smokeTestCollection, smokeTestResource);
        smokeTestLifecycles(smokeTestCollection, smokeTestResource);
        /* commented out in the original one, it's working */
        // smokeTestSubscriptions(smokeTestCollection, smokeTestResource);

    }

    private void smokeTestLifecycles(String smokeTestCollection,
                                     String smokeTestResource) throws RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException {

        String[] lifeCycleItem = {"Code Completed", "WSDL, Schema Created",
                "QoS Created"};

        lifeCycleAdminServiceClient.addAspect(smokeTestCollection,
                "ServiceLifeCycle");
        lifeCycleAdminServiceClient.invokeAspect(smokeTestCollection,
                "ServiceLifeCycle", "Promote", lifeCycleItem);
        LifecycleBean lifecycleBean = lifeCycleAdminServiceClient
                .getLifecycleBean(smokeTestCollection);
        Property[] lifecycleProperties = lifecycleBean.getLifecycleProperties();
        boolean isPromoted = false;
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals(
                    "registry.lifecycle.ServiceLifeCycle.state")) {
                if (property.getValues()[0].contains("Testing")) {
                    isPromoted = true;
                }
            }
        }
        assertTrue(isPromoted, "Life-cycle not promoted");

        lifeCycleAdminServiceClient.invokeAspect(smokeTestCollection,
                "ServiceLifeCycle", "Demote", lifeCycleItem);
        lifecycleBean = lifeCycleAdminServiceClient
                .getLifecycleBean(smokeTestCollection);
        lifecycleProperties = lifecycleBean.getLifecycleProperties();
        boolean isDemoted = false;
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals(
                    "registry.lifecycle.ServiceLifeCycle.state")) {
                if (property.getValues()[0].contentEquals("Development")) {
                    isDemoted = true;
                }
            }
        }
        assertTrue(isDemoted, "Life-cycle not demoted");

        lifeCycleAdminServiceClient.addAspect(smokeTestResource,
                "ServiceLifeCycle");
        lifeCycleAdminServiceClient.invokeAspect(smokeTestResource,
                "ServiceLifeCycle", "Promote", lifeCycleItem);
        lifecycleBean = lifeCycleAdminServiceClient
                .getLifecycleBean(smokeTestResource);
        lifecycleProperties = lifecycleBean.getLifecycleProperties();

        boolean isPromotedRes = false;
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals(
                    "registry.lifecycle.ServiceLifeCycle.state")) {
                if (property.getValues()[0].contentEquals("Testing")) {
                    isPromotedRes = true;
                }
            }
        }

        assertTrue(isPromotedRes, "Life-cycle not promoted");

        lifeCycleAdminServiceClient.invokeAspect(smokeTestResource,
                "ServiceLifeCycle", "Demote", lifeCycleItem);
        lifecycleBean = lifeCycleAdminServiceClient
                .getLifecycleBean(smokeTestResource);
        lifecycleProperties = lifecycleBean.getLifecycleProperties();
        boolean isDemotedRes = false;
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals(
                    "registry.lifecycle.ServiceLifeCycle.state")) {
                if (property.getValues()[0].contentEquals("Development")) {
                    isDemotedRes = true;
                }
            }
        }
        assertTrue(isDemotedRes, "Life-cycle not Demoted");
    }

    private void smokeTestCommunityFeatures(String smokeTestCollection,
                                            String smokeTestResource) throws RemoteException,
            RegistryExceptionException, RegistryException {
        smokeTestCommunityFeaturesWithComparison(smokeTestCollection,
                smokeTestResource, smokeTestCollection, smokeTestResource);
    }

    private void smokeTestPropertiesWithComparison(String smokeTestCollection,
                                                   String smokeTestResource, String smokeTestCollectionVerify,
                                                   String smokeTestResourceVerify) throws RemoteException,
            RegistryExceptionException,
            PropertiesAdminServiceRegistryExceptionException {

        propertiesAdminServiceClient.setProperty(smokeTestCollectionVerify,
                                                 "foo", "bar");
        boolean found = false;
        for (org.wso2.carbon.registry.properties.stub.utils.xsd.Property property : propertiesAdminServiceClient
                .getProperty(smokeTestCollection, "no").getProperties()) {
            if (property.getKey().equals("foo")) {
                if (property.getValue().contentEquals("bar")) {
                    found = true;
                    break;
                }

            }
        }
        assertTrue(found, "The property was not found");

        propertiesAdminServiceClient.setProperty(smokeTestResourceVerify,
                "foo", "bar");
        found = false;
        for (org.wso2.carbon.registry.properties.stub.utils.xsd.Property property : propertiesAdminServiceClient
                .getProperty(smokeTestResource, "no").getProperties()) {
            if (property.getKey().equals("foo")) {
                if (property.getValue().contentEquals("bar")) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found, "The property was not found");
    }

    private void smokeTestCommunityFeaturesWithComparison(
            String smokeTestCollection, String smokeTestResource,
            String smokeTestCollectionVerify, String smokeTestResourceVerify)
            throws RemoteException, RegistryExceptionException,
            RegistryException {

        infoServiceAdminClient.addComment("this is sample comment1",
                smokeTestCollection, environment.getGreg().getSessionCookie());
        infoServiceAdminClient.addComment("this is sample comment2",
                smokeTestCollection, environment.getGreg().getSessionCookie());
        Comment[] comment = infoServiceAdminClient.getComments(
                smokeTestCollectionVerify,
                environment.getGreg().getSessionCookie()).getComments();
        assertTrue(
                comment[0].getDescription().equalsIgnoreCase(
                        "this is sample comment1"),
                "added comment1 was not found");
        assertTrue(
                comment[1].getDescription().equalsIgnoreCase(
                        "this is sample comment2"),
                "added comment2 was not found");
        infoServiceAdminClient.removeComment(comment[0].getCommentPath(),
                environment.getGreg().getSessionCookie());
        comment = infoServiceAdminClient.getComments(smokeTestCollectionVerify,
                environment.getGreg().getSessionCookie()).getComments();
        assertTrue(
                comment[0].getDescription().equalsIgnoreCase(
                        "this is sample comment2"), "comment was not removed");

        infoServiceAdminClient.addComment("this is sample comment",
                smokeTestResource, environment.getGreg().getSessionCookie());
        infoServiceAdminClient.addComment("this is sample comment2",
                smokeTestResource, environment.getGreg().getSessionCookie());
        comment = infoServiceAdminClient.getComments(smokeTestResourceVerify,
                environment.getGreg().getSessionCookie()).getComments();
        assertTrue(
                comment[0].getDescription().equalsIgnoreCase(
                        "this is sample comment"),
                "added comment1 was not found");
        assertTrue(
                comment[1].getDescription().equalsIgnoreCase(
                        "this is sample comment2"),
                "added comment2 was not found");
        infoServiceAdminClient.removeComment(comment[0].getCommentPath(),
                environment.getGreg().getSessionCookie());
        comment = infoServiceAdminClient.getComments(smokeTestResourceVerify,
                environment.getGreg().getSessionCookie()).getComments();
        assertTrue(
                comment[0].getDescription().equalsIgnoreCase(
                        "this is sample comment2"), "comment was not removed");

        infoServiceAdminClient.addTag("foo", smokeTestCollection, environment
                .getGreg().getSessionCookie());
        assertEquals(
                infoServiceAdminClient.getTags(smokeTestCollectionVerify,
                        environment.getGreg().getSessionCookie()).getTags().length,
                1, "the tag was not added");
        infoServiceAdminClient.removeTag("foo", smokeTestCollection,
                environment.getGreg().getSessionCookie());
        assertNull(
                infoServiceAdminClient.getTags(smokeTestCollectionVerify,
                        environment.getGreg().getSessionCookie()).getTags(),
                "the tag was not removed");

        infoServiceAdminClient.addTag("foo", smokeTestResource, environment
                .getGreg().getSessionCookie());
        assertEquals(
                infoServiceAdminClient.getTags(smokeTestResourceVerify,
                        environment.getGreg().getSessionCookie()).getTags().length,
                1, "the tag was not added");
        infoServiceAdminClient.removeTag("foo", smokeTestResource, environment
                .getGreg().getSessionCookie());
        assertNull(
                infoServiceAdminClient.getTags(smokeTestResourceVerify,
                        environment.getGreg().getSessionCookie()).getTags(),
                "the tag was not removed");

        infoServiceAdminClient.rateResource("5", smokeTestCollection,
                environment.getGreg().getSessionCookie());
        assertEquals(
                infoServiceAdminClient.getRatings(smokeTestCollectionVerify,
                        environment.getGreg().getSessionCookie())
                        .getAverageRating(), 5.0f, "the ratings do not tally");
        assertEquals(
                infoServiceAdminClient.getRatings(smokeTestCollectionVerify,
                        environment.getGreg().getSessionCookie())
                        .getUserRating(), 5, "the ratings do not tally");
        infoServiceAdminClient.rateResource("0", smokeTestCollection,
                environment.getGreg().getSessionCookie());
        assertEquals(
                infoServiceAdminClient.getRatings(smokeTestCollectionVerify,
                        environment.getGreg().getSessionCookie())
                        .getAverageRating(), 0.0f, "the ratings do not tally");
        assertEquals(
                infoServiceAdminClient.getRatings(smokeTestCollectionVerify,
                        environment.getGreg().getSessionCookie())
                        .getUserRating(), 0, "the ratings do not tally");

        infoServiceAdminClient.rateResource("5", smokeTestResource, environment
                .getGreg().getSessionCookie());
        assertEquals(
                infoServiceAdminClient.getRatings(smokeTestResourceVerify,
                        environment.getGreg().getSessionCookie())
                        .getAverageRating(), 5.0f, "the ratings do not tally");
        assertEquals(
                infoServiceAdminClient.getRatings(smokeTestResourceVerify,
                        environment.getGreg().getSessionCookie())
                        .getUserRating(), 5, "the ratings do not tally");
        infoServiceAdminClient.rateResource("0", smokeTestResource, environment
                .getGreg().getSessionCookie());
        assertEquals(
                infoServiceAdminClient.getRatings(smokeTestResourceVerify,
                        environment.getGreg().getSessionCookie())
                        .getAverageRating(), 0.0f, "the ratings do not tally");
        assertEquals(
                infoServiceAdminClient.getRatings(smokeTestResourceVerify,
                        environment.getGreg().getSessionCookie())
                        .getUserRating(), 0, "the ratings do not tally");
    }

    private void smokeTestSubscriptions(String smokeTestCollection,
                                        String smokeTestResource) throws RemoteException,
            RegistryExceptionException, RegistryException {

        assertEquals(
                infoServiceAdminClient.subscribe(smokeTestCollection,
                        "https://localhost:9443/services/Hello",
                        "CollectionUpdated",
                        environment.getGreg().getSessionCookie())
                        .getSubscriptionInstances().length, 1,
                "subscription failed");
        SubscriptionInstance[] instances = infoServiceAdminClient.subscribe(
                smokeTestCollection, "digest://h/mailto:dev@wso2.org",
                "CollectionUpdated", environment.getGreg().getSessionCookie())
                .getSubscriptionInstances();
        assertEquals(instances.length, 1, "subscription failed");
        assertEquals(
                infoServiceAdminClient.subscribe(smokeTestCollection,
                        "https://localhost:9443/services/RESTHello",
                        "CollectionUpdated",
                        environment.getGreg().getSessionCookie())
                        .getSubscriptionInstances().length, 1,
                "subscription failed");
        assertEquals(instances[0].getDigestType(), "h", "invalid digest type");
        assertEquals(instances[0].getAddress(),
                "digest://h/mailto:dev@wso2.org", "invalid address");
        assertEquals(instances[0].getNotificationMethod(), "email",
                "invalid notification method");

        infoServiceAdminClient.unsubscribe(
                smokeTestCollection,
                infoServiceAdminClient.getSubscriptions(smokeTestCollection,
                        environment.getGreg().getSessionCookie())
                        .getSubscriptionInstances()[0].getId(), environment
                .getGreg().getSessionCookie());

        assertEquals(
                infoServiceAdminClient.getSubscriptions(smokeTestCollection,
                        environment.getGreg().getSessionCookie())
                        .getSubscriptionInstances().length, 2,
                "failed to unsubscribe");
        infoServiceAdminClient.unsubscribe(
                smokeTestCollection,
                infoServiceAdminClient.getSubscriptions(smokeTestCollection,
                        environment.getGreg().getSessionCookie())
                        .getSubscriptionInstances()[0].getId(), environment
                .getGreg().getSessionCookie());

        assertEquals(
                infoServiceAdminClient.getSubscriptions(smokeTestCollection,
                        environment.getGreg().getSessionCookie())
                        .getSubscriptionInstances().length, 1,
                "failed to unsubscribe");

        assertEquals(
                infoServiceAdminClient.subscribe(smokeTestResource,
                        "https://localhost:9443/services/Hello",
                        "ResourceUpdated",
                        environment.getGreg().getSessionCookie())
                        .getSubscriptionInstances().length, 1,
                "subscription failed");
        instances = infoServiceAdminClient.subscribe(smokeTestResource,
                "digest://h/mailto:dev@wso2.org", "ResourceUpdated",
                environment.getGreg().getSessionCookie())
                .getSubscriptionInstances();
        assertEquals(instances.length, 1, "subscription failed");
        assertEquals(
                infoServiceAdminClient.subscribe(smokeTestResource,
                        "https://localhost:9443/services/RESTHello",
                        "ResourceUpdated",
                        environment.getGreg().getSessionCookie())
                        .getSubscriptionInstances().length, 1,
                "subscription failed");
        assertEquals(instances[0].getDigestType(), "h", "invalid digest type");
        assertEquals(instances[0].getAddress(),
                "digest://h/mailto:dev@wso2.org", "invalid address");
        assertEquals(instances[0].getNotificationMethod(), "email",
                "invalid notification method");
        infoServiceAdminClient.unsubscribe(
                smokeTestResource,
                infoServiceAdminClient.getSubscriptions(smokeTestResource,
                        environment.getGreg().getSessionCookie())
                        .getSubscriptionInstances()[0].getId(), environment
                .getGreg().getSessionCookie());

        assertEquals(
                infoServiceAdminClient.getSubscriptions(smokeTestResource,
                        environment.getGreg().getSessionCookie())
                        .getSubscriptionInstances().length, 2,
                "failed to unsubscribe");
        infoServiceAdminClient.unsubscribe(
                smokeTestResource,
                infoServiceAdminClient.getSubscriptions(smokeTestResource,
                        environment.getGreg().getSessionCookie())
                        .getSubscriptionInstances()[0].getId(), environment
                .getGreg().getSessionCookie());
        assertEquals(
                infoServiceAdminClient.getSubscriptions(smokeTestResource,
                        environment.getGreg().getSessionCookie())
                        .getSubscriptionInstances().length, 1,
                "failed to unsubscribe");
    }

    private void smokeTestRelationships(String smokeTestCollection,
                                        String smokeTestResource) throws RemoteException,
            AddAssociationRegistryExceptionException,
            GetAssociationTreeRegistryExceptionException {

        relationAdminServiceClient.addAssociation(smokeTestCollection, "uses",
                                                  smokeTestResource, "add");
        assertTrue(
                relationAdminServiceClient
                        .getAssociationTree(smokeTestCollection, "uses")
                        .getAssociationTree().contains(smokeTestResource),
                "The association was not added");

        relationAdminServiceClient.addAssociation(smokeTestResource, "usedBy",
                                                  smokeTestCollection, "add");
        assertTrue(
                relationAdminServiceClient
                        .getAssociationTree(smokeTestResource, "usedBy")
                        .getAssociationTree().contains(smokeTestCollection),
                "The association was not added");

        relationAdminServiceClient.addAssociation(smokeTestCollection,
                                                  "depends", smokeTestResource, "add");
        assertTrue(
                relationAdminServiceClient
                        .getAssociationTree(smokeTestCollection, "depends")
                        .getAssociationTree().contains(smokeTestResource),
                "The association was not added");

        relationAdminServiceClient.addAssociation(smokeTestResource, "depends",
                                                  smokeTestCollection, "add");
        assertTrue(
                relationAdminServiceClient
                        .getAssociationTree(smokeTestResource, "depends")
                        .getAssociationTree().contains(smokeTestCollection),
                "The association was not added");

        relationAdminServiceClient.addAssociation(smokeTestCollection, "uses",
                                                  smokeTestResource, "delete");
        assertFalse(
                relationAdminServiceClient
                        .getAssociationTree(smokeTestCollection, "uses")
                        .getAssociationTree().contains(smokeTestResource),
                "The association was not added");

        relationAdminServiceClient.addAssociation(smokeTestResource, "usedBy",
                                                  smokeTestCollection, "delete");
        assertFalse(
                relationAdminServiceClient
                        .getAssociationTree(smokeTestResource, "usedBy")
                        .getAssociationTree().contains(smokeTestCollection),
                "The association was not added");

        relationAdminServiceClient.addAssociation(smokeTestCollection,
                "depends", smokeTestResource, "delete");
        assertFalse(
                relationAdminServiceClient
                        .getAssociationTree(smokeTestCollection, "depends")
                        .getAssociationTree().contains(smokeTestResource),
                "The association was not added");

        relationAdminServiceClient.addAssociation(smokeTestResource, "depends",
                                                  smokeTestCollection, "delete");
        assertFalse(
                relationAdminServiceClient
                        .getAssociationTree(smokeTestResource, "depends")
                        .getAssociationTree().contains(smokeTestCollection),
                "The association was not added");
    }

    private void smokeTestCollection(String smokeTestCollection)
            throws RemoteException, ResourceAdminServiceExceptionException {

        resourceAdminServiceClient.addCollection(
                RegistryUtils.getParentPath(smokeTestCollection),
                RegistryUtils.getResourceName(smokeTestCollection), "",
                "A smoke test collection.");
        CollectionContentBean collectionContentBean = resourceAdminServiceClient
                .getCollectionContent(RegistryUtils
                        .getParentPath(smokeTestCollection));
        boolean found = false;

        if (collectionContentBean.getChildCount() > 0) {
            String[] childPath = collectionContentBean.getChildPaths();

            for (int i = 0; i <= childPath.length - 1; i++) {
                if (childPath[i].equalsIgnoreCase(smokeTestCollection)) {
                    found = true;
                    break;
                }
            }

        }
        assertTrue(found, "The newly added collection was not found");
    }

    private void smokeTestSymbolicLink(String smokeTestLink, String path)
            throws RemoteException, ResourceAdminServiceExceptionException {

        resourceAdminServiceClient.addSymbolicLink(
                RegistryUtils.getParentPath(smokeTestLink),
                RegistryUtils.getResourceName(smokeTestLink), path);
        CollectionContentBean collectionContentBean = resourceAdminServiceClient
                .getCollectionContent(RegistryUtils
                                              .getParentPath(smokeTestLink));
        boolean found = false;
        if (collectionContentBean.getChildCount() > 0) {
            String[] childPath = collectionContentBean.getChildPaths();

            for (int i = 0; i <= childPath.length - 1; i++) {
                if (childPath[i].equalsIgnoreCase(smokeTestLink)) {
                    found = true;
                    break;
                }
            }

        }
        assertTrue(found, "The newly added link was not found");
    }

    private void smokeTestResource(String smokeTestResource)
            throws RemoteException, ResourceAdminServiceExceptionException,
            MalformedURLException {

        String sampleTextPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                + "artifacts" + File.separator + "GREG" + File.separator + "txt" + File.separator
                + "sampleText.txt";
        resourceAdminServiceClient.addResource(smokeTestResource, "text/plain",
                "A smoke test", new DataHandler(new URL("file:///"
                + sampleTextPath)));
        CollectionContentBean collectionContentBean = resourceAdminServiceClient
                .getCollectionContent(RegistryUtils
                        .getParentPath(smokeTestResource));
        boolean found = false;
        if (collectionContentBean.getChildCount() > 0) {
            String[] childPath = collectionContentBean.getChildPaths();

            for (int i = 0; i <= childPath.length - 1; i++) {
                if (childPath[i].equalsIgnoreCase(smokeTestResource)) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found, "The newly added resource was not found");

    }

    @AfterClass(groups = "org.greg", alwaysRun = true, description = "when actual resources are deleted the respective "
            + "links too are getting deleted")
    public void tearDown() throws RemoteException,
            ResourceAdminServiceExceptionException {

        resourceAdminServiceClient.deleteResource("/TestAutomation");
        resourceAdminServiceClient
                .deleteResource("/_system/governance/TestAutomation");

        resourceAdminServiceClient.deleteResource("/SmokeTestCollectionActual");
        resourceAdminServiceClient
                .deleteResource("/SmokeTestResourceActual.txt");
        resourceAdminServiceClient
                .deleteResource("/SmokeTestCollectionActual1");
        resourceAdminServiceClient
                .deleteResource("/SmokeTestResourceActual1.txt");

        resourceAdminServiceClient
                .deleteResource("/SmokeTestCollectionActual4");

        resourceAdminServiceClient
                .deleteResource("/SmokeTestResourceActual4.txt");

        resourceAdminServiceClient = null;
        infoServiceAdminClient = null;
        propertiesAdminServiceClient = null;
        relationAdminServiceClient = null;
        lifeCycleAdminServiceClient = null;
        environment = null;


    }
}
