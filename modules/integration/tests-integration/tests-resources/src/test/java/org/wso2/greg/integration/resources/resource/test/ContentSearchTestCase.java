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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.indexing.stub.generated.xsd.ResourceData;
import org.wso2.carbon.registry.indexing.stub.generated.xsd.SearchResultsBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ContentSearchAdminClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.activation.DataHandler;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry  content search operation
 */
public class ContentSearchTestCase extends GREGIntegrationBaseTest{

    private static final Log log = LogFactory.getLog(ContentSearchTestCase.class);

    private static final int RERTY_CYCLES = 30;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ContentSearchAdminClient contentSearchAdminClient;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        log.info("Initializing Tests for Community Feature in Registry Policy");
        log.debug("Community Feature in Registry Policy Test Initialised");

        log.debug("Running SuccessCase");

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(getBackendURL(),
                                               automationContext.getContextTenant().getContextUser().getUserName(), automationContext.getContextTenant().getContextUser().getPassword());

        contentSearchAdminClient = new ContentSearchAdminClient(
                getBackendURL(),
                automationContext.getContextTenant().getContextUser().getUserName(), automationContext.getContextTenant().getContextUser().getPassword());
    }


    @Test(groups = {"wso2.greg"})
    public void testAddResource()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException, XPathExpressionException {

        String RESOURCE_NAME = "sample.wsdl";
        String resource = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                          File.separator + "GREG" + File.separator + "wsdl" + File.separator +
                RESOURCE_NAME;

        String WSDL_PATH = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/1.0.0/";
        resourceAdminServiceClient.addResource(WSDL_PATH + RESOURCE_NAME,
                                               "application/wsdl+xml", "test resource",
                                               new DataHandler(new URL("file:///" + resource)));

        String authorUserName =
                resourceAdminServiceClient.getResource(WSDL_PATH +
                        RESOURCE_NAME)[0].getAuthorUserName();
        assertTrue(automationContext.getContextTenant().getContextUser().getUserName().equalsIgnoreCase(authorUserName),
                   WSDL_PATH + RESOURCE_NAME + " creation failure");

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "testAddResource")
    public void testSearchContent() throws InterruptedException, RemoteException {

        //sleep before searching
        Thread.sleep(15000);

        SearchResultsBean bean;

        boolean searchSuccess = false;
        // Retrying until test passes
        log.info("### Content search retry cycle started .. ");
        for (int i = 0; i < RERTY_CYCLES; i++) {
            log.info("#### Content search cycle # " + i);
            bean =
                    contentSearchAdminClient.getContentSearchResults(
                            "ArrayOftCountrySelectedTopScorer");

            if (bean.getResourceDataList() != null) {
                bean.setResourceDataList(new ResourceData[0]);
                searchSuccess = true;
                log.info("#### Content search PASSED .. in seconds : " + i * 15);
                break;

            } else {
                Thread.sleep(15000);
            }
        }

        assertTrue(searchSuccess, "# MetaData search test failed .. ");
    }

    //cleanup code
    @AfterClass
    public void cleanup()
            throws Exception {

       resourceAdminServiceClient.deleteResource( "/_system/governance/trunk/services/" +
                                                            "eu/dataaccess/footballpool/1.0.0-SNAPSHOT/Info");
        resourceAdminServiceClient=null;
        contentSearchAdminClient=null;
    }
}
