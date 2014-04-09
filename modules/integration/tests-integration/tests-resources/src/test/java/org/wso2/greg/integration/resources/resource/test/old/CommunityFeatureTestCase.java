/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.greg.integration.resources.resource.test.old;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry community feature operation
 */
public class CommunityFeatureTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(CommunityFeatureTestCase.class);

    private static final String PARENT_PATH = "/TestAutomation";
    private static final String CHILD_COLL_NAME = "InfoAdminTest";

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private String loggedInSessionCookie = "";

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        loggedInSessionCookie = getSessionCookie();
        log.debug("Running SuccessCase");

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(getBackendURL(),
                        automationContext.getUser().getUserName(), automationContext.getUser().getPassword());

        infoServiceAdminClient = new InfoServiceAdminClient(
                getBackendURL(),
                automationContext.getUser().getUserName(), automationContext.getUser().getPassword());
    }

    @Test(groups = {"wso2.greg"})
    public void testCreateCollections()
            throws ResourceAdminServiceExceptionException, RemoteException, XPathExpressionException {

        log.debug("Running SuccessCase");

        CollectionContentBean collectionContentBean =
                resourceAdminServiceClient.getCollectionContent("/");

        if (collectionContentBean.getChildCount() > 0) {
            String[] childPath = collectionContentBean.getChildPaths();
            for (int i = 0; i <= childPath.length - 1; i++) {
                if (childPath[i].equalsIgnoreCase(PARENT_PATH)) {
                    resourceAdminServiceClient.deleteResource(PARENT_PATH);
                }
            }
        }
        resourceAdminServiceClient.addCollection("/", "TestAutomation", "", "");

        String authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH)[0].getAuthorUserName();

        assertTrue(automationContext.getUser().getUserName().equalsIgnoreCase(authorUserName),
                PARENT_PATH + " creation failure");
        log.info("collection added to " + PARENT_PATH);

        resourceAdminServiceClient.addCollection(PARENT_PATH, CHILD_COLL_NAME, "", "");
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH + "/" +
                        CHILD_COLL_NAME)[0].getAuthorUserName();
        assertTrue(automationContext.getUser().getUserName().equalsIgnoreCase(authorUserName),
                PARENT_PATH + "/" + CHILD_COLL_NAME + " creation failure");
        log.info("collection added to " + PARENT_PATH + "/" + CHILD_COLL_NAME);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "testCreateCollections")
    public void testComments() throws RegistryException, AxisFault, RegistryExceptionException {

        String comment1 = "this is sample comment";
        String comment2 = "this is sample comment2";

        infoServiceAdminClient.addComment(comment1,
                PARENT_PATH + "/" + CHILD_COLL_NAME,
                loggedInSessionCookie);
        infoServiceAdminClient.addComment(comment2,
                PARENT_PATH + "/" + CHILD_COLL_NAME,
                loggedInSessionCookie);
        CommentBean commentBean =
                infoServiceAdminClient.getComments(PARENT_PATH + "/" + CHILD_COLL_NAME,
                        loggedInSessionCookie);
        Comment[] comment = commentBean.getComments();

        assertTrue(comment[0].getDescription().equalsIgnoreCase(comment1),
                "Added comment not found - " + comment1);

        assertTrue(comment[1].getDescription().equalsIgnoreCase(comment2),
                "Added comment not found - " + comment2);

        infoServiceAdminClient.removeComment(comment[0].getCommentPath(),
                loggedInSessionCookie);
        commentBean = infoServiceAdminClient.getComments(PARENT_PATH + "/" + CHILD_COLL_NAME,
                loggedInSessionCookie);
        comment = commentBean.getComments();

        assertFalse(comment[0].getDescription().equalsIgnoreCase(comment1),
                "Comment not deleted - " + comment1);
    }

    //cleanup code
    @AfterClass
    public void cleanup()
            throws Exception {
        resourceAdminServiceClient.deleteResource("/TestAutomation");
        resourceAdminServiceClient = null;
        infoServiceAdminClient = null;
    }
}
