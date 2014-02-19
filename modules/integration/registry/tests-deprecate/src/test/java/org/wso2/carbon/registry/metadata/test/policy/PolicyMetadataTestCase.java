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
package org.wso2.carbon.registry.metadata.test.policy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceStub;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.registry.info.stub.InfoAdminServiceStub;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.RatingBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;

import static org.testng.Assert.fail;

/**
 * A test case which tests registry policy meta data operation
 */

public class PolicyMetadataTestCase {
    private static final Log log = LogFactory.getLog(PolicyMetadataTestCase.class);

    private String policyPath = "/_system/governance/trunk/policies/";
    private String resourceName = "sample_policy.xml";

    private ResourceAdminServiceStub resourceAdminServiceStub;
    private RelationAdminServiceStub relationAdminServiceStub;
    private CustomLifecyclesChecklistAdminServiceStub customLifecyclesChecklistAdminServiceStub;
    private InfoAdminServiceStub infoAdminServiceStub;
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();


    @BeforeClass(groups = {"wso2.greg.policy.b"})
    public void init() throws Exception {
        log.info("Initializing Tests for Community Features in Registry Policy");
        log.debug("Community Features in Registry Policy Tests Initialised");
        loggedInSessionCookie = util.login();

        log.debug("Running SuccessCase");
        infoAdminServiceStub = TestUtils.getInfoAdminServiceStub(loggedInSessionCookie);
        customLifecyclesChecklistAdminServiceStub = TestUtils.getCustomLifecyclesChecklistAdminServiceStub(loggedInSessionCookie);
        relationAdminServiceStub = TestUtils.getRelationAdminServiceStub(loggedInSessionCookie);
        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);

    }

//    @Test(groups = {"wso2.greg"})
//    public void runSuccessCase() {
//        try {
//
//            addTag();
//            addComment();
//            addRate();
//            addLifeCycle();
//            addAssociation();
//            addDependency();
//            getVersion();
//        } catch (Exception e) {
//            fail("Unable to run policy meta data test: " + e);
//            log.error("Unable to run policy meta data test: " + e.getMessage());
//        }
//
//    }

    @Test(groups = {"wso2.greg.policy.b"}, dependsOnGroups = {"wso2.greg.policy.a"})
    public void addTag() {

        try {
            infoAdminServiceStub.addTag("test tag added", policyPath + resourceName, loggedInSessionCookie);
            TagBean tagBean = infoAdminServiceStub.getTags(policyPath + resourceName, loggedInSessionCookie);
            Tag[] tag = tagBean.getTags();
            for (int i = 0; i <= tag.length - 1; i++) {
                if (!tag[i].getTagName().equalsIgnoreCase("test tag added")) {
                    log.error("The given tag not found");
                    fail("Tag not found");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception thrown while adding tag : " + e);
        }
    }

    @Test(groups = {"wso2.greg.policy.b"}, dependsOnGroups = {"wso2.greg.policy.a"}, dependsOnMethods = {"addTag"})
    public void addComment() {

        try {
            //adding comment to a resource
            infoAdminServiceStub.addComment("added a test comment", policyPath + resourceName, loggedInSessionCookie);
            CommentBean commentBean = infoAdminServiceStub.getComments(policyPath + resourceName, loggedInSessionCookie);
            Comment[] comment = commentBean.getComments();

            if (!comment[0].getDescription().equalsIgnoreCase("added a test comment")) {
                log.error("comment not found");
                fail("comment not found");
            }

            //removing comment from the resource
            infoAdminServiceStub.removeComment(comment[0].getCommentPath(), loggedInSessionCookie);

            if (comment[0].getDescription().equalsIgnoreCase("added test comment")) {
                log.error("comment can not be deleted");
                fail("comment can not be deleted");
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occurred while put and get comment :" + e.getMessage());
            fail("Exception occurred while put and get comment  :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.policy.b"}, dependsOnGroups = {"wso2.greg.policy.a"}, dependsOnMethods = {"addComment"})
    public void addRate() {
        String rateValue = "2";
        try {
            infoAdminServiceStub.rateResource(rateValue, policyPath + resourceName, loggedInSessionCookie);
            RatingBean ratingBean = infoAdminServiceStub.getRatings(policyPath + resourceName, loggedInSessionCookie);
            if (ratingBean.getUserRating() != Integer.parseInt(rateValue)) {
                log.error("Rating value not found");
                fail("Rating value not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while adding rate : " + e);
        }
    }

    @Test(groups = {"wso2.greg.policy.b"}, dependsOnGroups = {"wso2.greg.policy.a"}, dependsOnMethods = {"addRate"})
    public void addLifeCycle() throws Exception {

        String[] lifeCycleItem = {"Code Completed", "WSDL, Schema Created", "QoS Created"};

        customLifecyclesChecklistAdminServiceStub.addAspect(policyPath + resourceName, "ServiceLifeCycle");
        customLifecyclesChecklistAdminServiceStub.invokeAspect(policyPath + resourceName, "ServiceLifeCycle", "Promote", lifeCycleItem);
        LifecycleBean lifecycleBean = customLifecyclesChecklistAdminServiceStub.getLifecycleBean(policyPath + resourceName);
        Property[] lifecycleProperties = lifecycleBean.getLifecycleProperties();
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals("registry.lifecycle.ServiceLifeCycle.state")) {
                if (!"Testing".equalsIgnoreCase(property.getValues()[0])) {
                    fail("Life-cycle not promoted");
                }
            }
        }

    }


    @Test(groups = {"wso2.greg.policy.b"}, dependsOnGroups = {"wso2.greg.policy.a"}, dependsOnMethods = {"addLifeCycle"})
    public void addAssociation() {

        AssociationTreeBean associationTreeBean = null;
        try {
            relationAdminServiceStub.addAssociation(policyPath + resourceName, "asso", "/_system/governance/trunk/policies/", "add");

            //check for added association
            associationTreeBean = relationAdminServiceStub.getAssociationTree("/_system/governance/trunk/policies/", "asso");
            if (!(associationTreeBean.getAssoType().equals("asso"))) {
                log.error("Required Association Information Not Found");
                fail("Required Association Information Not Found");
            }

        } catch (Exception e) {
            throw new RuntimeException("Exception thrown while adding an association : " + e);
        }

    }

    @Test(groups = {"wso2.greg.policy.b"}, dependsOnGroups = {"wso2.greg.policy.a"}, dependsOnMethods = {"addAssociation"})
    public void addDependency() {

        AssociationTreeBean associationTreeBean = null;

        try {
            relationAdminServiceStub.addAssociation(policyPath + resourceName, "depends", "/_system/governance/trunk/policies/", "add");

            //check for added dependencies
            associationTreeBean = relationAdminServiceStub.getAssociationTree("/_system/governance/trunk/policies/", "depends");
            if (!(associationTreeBean.getAssoType().equals("depends"))) {
                log.error("Required Dependency Information Not Found");
                fail("Required Dependency Information Not Found");
            }

        } catch (Exception e) {
            throw new RuntimeException("Exception thrown while adding a dependency : " + e);
        }
    }


    @Test(groups = {"wso2.greg.policy.b"}, dependsOnGroups = {"wso2.greg.policy.a"}, dependsOnMethods = {"addDependency"})
    public void getVersion() {

        VersionPath[] versionPath = null;
        long versionNoBefore = 0L;
        long versionNoAfter = 0L;

        try {
            resourceAdminServiceStub.createVersion(policyPath + resourceName);
            versionPath = resourceAdminServiceStub.getVersionsBean(policyPath + resourceName).getVersionPaths();
            versionNoBefore = versionPath[0].getVersionNumber();

            /**
             * update resource content and checking the version number update
             */

            updatePolicyFromFile();

            versionPath = resourceAdminServiceStub.getVersionsBean(policyPath + resourceName).getVersionPaths();
            versionNoAfter = versionPath[0].getVersionNumber();

            if (versionNoBefore != versionNoAfter) {
                fail("New Version has not been created: ");
                log.error("New Version has not been created: ");

            }

        } catch (Exception e) {
            throw new RuntimeException("Exception thrown when getting resource version : " + e);
        }

    }

    @Test(groups = {"wso2.greg.policy.b"}, dependsOnGroups = {"wso2.greg.policy.a"})
    public void updatePolicyFromFile() {

        String resourceName = "sample_policy.xml";
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

        try {

            /**
             *  update policy and check the content
             */
            resourceAdminServiceStub.updateTextContent(policyPath + resourceName, resContent);

            if (resourceAdminServiceStub.getTextContent(policyPath + resourceName).contains("InactivityTimeout")) {
                log.info("Policy file successfully updated");
            } else {
                log.error("Policy File has not been updated in the registry");
                fail("Policy File has not been updated in the registry");
            }

        } catch (Exception e) {
            fail("Unable to get file content: " + e);
            log.error("Unable to get file content: " + e.getMessage());
        }

    }


}
