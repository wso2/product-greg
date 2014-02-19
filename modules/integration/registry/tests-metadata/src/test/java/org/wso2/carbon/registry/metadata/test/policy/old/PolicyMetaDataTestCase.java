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
package org.wso2.carbon.registry.metadata.test.policy.old;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
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
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.RatingBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

/**
 * A test case which tests registry policy meta data operation
 */

public class PolicyMetaDataTestCase {
    private static final Log log = LogFactory.getLog(PolicyMetaDataTestCase.class);

    private String policyPath = "/_system/governance/trunk/policies/";
    private String resourceName = "policy.xml";
    private ManageEnvironment environment;
    private UserInfo userInfo;


    private ResourceAdminServiceClient resourceAdminServiceClient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;


    @BeforeClass(groups = {"wso2.greg."})
    public void init() throws Exception {
        log.info("Initializing Tests for Community Features in Registry Policy");
        log.debug("Community Features in Registry Policy Tests Initialised");
        log.debug("Running SuccessCase");
        int userId = 0;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        log.debug("Running SuccessCase");
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                        userInfo.getUserName(), userInfo.getPassword());
        relationAdminServiceClient = new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        infoServiceAdminClient = new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(), userInfo.getUserName(), userInfo.getPassword()
        );
    }

    @Test(groups = {"wso2.greg"})
    public void addResource() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException, RemoteException {
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                "GREG" + File.separator +
                "policy" + File.separator + resourceName;
        resourceAdminServiceClient.addResource(policyPath + resourceName,
                "application/policy+xml", "test resource",
                new DataHandler(new URL("file:///" + resource)));
        // wait for sometime until the resource has been added. The activity logs are written
        // every 10 seconds, so you'll need to wait until that's done.
        Thread.sleep(20000);
        assertTrue(resourceAdminServiceClient.getResource(policyPath + resourceName)[0].getAuthorUserName().
                contains(userInfo.getUserName()));
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addResource")
    public void addTag() throws RegistryException, AxisFault, RegistryExceptionException {
        infoServiceAdminClient.addTag("test tag added", policyPath + resourceName, environment.getGreg().getSessionCookie());
        TagBean tagBean = infoServiceAdminClient.getTags(policyPath + resourceName, environment.getGreg().getSessionCookie());
        Tag[] tag = tagBean.getTags();
        for (int i = 0; i <= tag.length - 1; i++) {
            if (!tag[i].getTagName().equalsIgnoreCase("test tag added")) {
                log.error("The given tag not found");
                fail("Tag not found");
            }
        }
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addTag"})
    public void addComment() throws RegistryException, AxisFault, RegistryExceptionException {
        //adding comment to a resource
        infoServiceAdminClient.addComment("added a test comment", policyPath + resourceName, environment.getGreg().getSessionCookie());
        CommentBean commentBean = infoServiceAdminClient.getComments(policyPath + resourceName, environment.getGreg().getSessionCookie());
        Comment[] comment = commentBean.getComments();
        assertTrue(comment[0].getDescription().equalsIgnoreCase("added a test comment"));
        //removing comment from the resource
        infoServiceAdminClient.removeComment(comment[0].getCommentPath(), environment.getGreg().getSessionCookie());
        assertFalse(comment[0].getDescription().equalsIgnoreCase("added test comment"));
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addComment"})
    public void addRate() throws RegistryException, RegistryExceptionException {
        String rateValue = "2";
        infoServiceAdminClient.rateResource(rateValue, policyPath + resourceName, environment.getGreg().getSessionCookie());
        RatingBean ratingBean = infoServiceAdminClient.getRatings(policyPath + resourceName, environment.getGreg().getSessionCookie());
        assertEquals(ratingBean.getUserRating(), Integer.parseInt(rateValue));
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addRate"})
    public void addLifeCycle() throws Exception {
        String[] lifeCycleItem = {"Code Completed", "WSDL, Schema Created", "QoS Created"};
        lifeCycleAdminServiceClient.addAspect(policyPath + resourceName, "ServiceLifeCycle");
        lifeCycleAdminServiceClient.invokeAspect(policyPath + resourceName, "ServiceLifeCycle", "Promote", lifeCycleItem);
        LifecycleBean lifecycleBean = lifeCycleAdminServiceClient.getLifecycleBean(policyPath + resourceName);
        Property[] lifecycleProperties = lifecycleBean.getLifecycleProperties();
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals("registry.lifecycle.ServiceLifeCycle.state")) {
                assertTrue("Testing".equalsIgnoreCase(property.getValues()[0]));
            }
        }

    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addLifeCycle"})
    public void addAssociation() throws AddAssociationRegistryExceptionException, RemoteException {
        AssociationTreeBean associationTreeBean = null;
        relationAdminServiceClient.addAssociation(policyPath + resourceName, "asso", "/_system/governance/trunk/policies/", "add");
        //check for added association
        associationTreeBean = relationAdminServiceClient.getAssociationTree("/_system/governance/trunk/policies/", "asso");
        assertTrue(associationTreeBean.getAssoType().equals("asso"));
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addAssociation"})
    public void addDependency() throws AddAssociationRegistryExceptionException, RemoteException {
        AssociationTreeBean associationTreeBean = null;
        relationAdminServiceClient.addAssociation(policyPath + resourceName, "depends", "/_system/governance/trunk/policies/", "add");
        //check for added dependencies
        associationTreeBean = relationAdminServiceClient.getAssociationTree("/_system/governance/trunk/policies/", "depends");
        assertTrue(associationTreeBean.getAssoType().equals("depends"));
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addDependency"})
    public void getVersion() throws ResourceAdminServiceExceptionException, RemoteException {
        VersionPath[] versionPath = null;
        long versionNoBefore = 0L;
        long versionNoAfter = 0L;
        resourceAdminServiceClient.createVersion(policyPath + resourceName);
        versionPath = resourceAdminServiceClient.getVersionsBean(policyPath + resourceName).getVersionPaths();
        versionNoBefore = versionPath[0].getVersionNumber();
        /**
         * update resource content and checking the version number update
         */
        updatePolicyFromFile();
        versionPath = resourceAdminServiceClient.getVersionsBean(policyPath + resourceName).getVersionPaths();
        versionNoAfter = versionPath[0].getVersionNumber();
        assertEquals(versionNoAfter, versionNoBefore);
    }


    private void updatePolicyFromFile() throws ResourceAdminServiceExceptionException, RemoteException {
        String resourceName = "policy.xml";
        String resContent = "<?xml version=\"1.0\"?>\n" +
                "\n" +
                "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                "  <wsp:ExactlyOne>\n" +
                "    <wsp:All>\n" +
                "      <wsrmp10:RMAssertion\n" +
                "       xmlns:wsrmp10=\"http://schemas.xmlsoap.org/ws/2005/02/rm/policy\">\n" +
                "        <!--wsrmp10:InactivityTimeout Milliseconds=\"600000\"/-->\n" +
                "        <wsrmp10:InactivityTimeout/>\n" +
                "        <wsrmp10:BaseRetransmissionInterval Milliseconds=\"3000\"/>\n" +
                "        <wsrmp10:ExponentialBackoff/>\n" +
                "        <wsrmp10:AcknowledgementInterval Milliseconds=\"200\"/>\n" +
                "      </wsrmp10:RMAssertion>\n" +
                "    </wsp:All>\n" +
                "    <wsp:All>\n" +
                "      <wsrmp:RMAssertion\n" +
                "           xmlns:wsrmp=\"http://docs.oasis-open.org/ws-rx/wsrmp/200702\">\n" +
                "        <wsrmp:SequenceSTR/>\n" +
                "        <wsrmp:DeliveryAssurance>\n" +
                "          <wsp:Policy>\n" +
                "            <wsrmp:ExactlyOnce/>\n" +
                "          </wsp:Policy>\n" +
                "        </wsrmp:DeliveryAssurance>\n" +
                "      </wsrmp:RMAssertion>\n" +
                "    </wsp:All>\n" +
                "  </wsp:ExactlyOne>\n" +
                "</wsp:Policy>"; //to update


        /**
         *  update policy and check the content
         */
        resourceAdminServiceClient.updateTextContent(policyPath + resourceName, resContent);
        Assert.assertTrue(resourceAdminServiceClient.getTextContent(policyPath + resourceName).contains("InactivityTimeout"));
    }


    @AfterClass(groups = "wso2.greg")
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException, RegistryException, RegistryExceptionException, AddAssociationRegistryExceptionException, CustomLifecyclesChecklistAdminServiceExceptionException {
        infoServiceAdminClient.removeTag("test tag added", policyPath + resourceName, environment.getGreg().getSessionCookie());
        relationAdminServiceClient.addAssociation(policyPath + resourceName, "asso", "/_system/governance/trunk/policies/", "remove");
        lifeCycleAdminServiceClient.removeAspect(policyPath + resourceName, "ServiceLifeCycle");

//        relationAdminServiceClient.cleanUp();
        resourceAdminServiceClient.deleteResource(policyPath + resourceName);
        resourceAdminServiceClient = null;
        relationAdminServiceClient = null;
        policyPath = null;
        infoServiceAdminClient = null;
        environment = null;
        userInfo = null;
        lifeCycleAdminServiceClient = null;


    }

}



