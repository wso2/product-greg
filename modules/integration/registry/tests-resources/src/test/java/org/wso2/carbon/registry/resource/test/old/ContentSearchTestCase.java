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

package org.wso2.carbon.registry.resource.test.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ContentSearchAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.indexing.stub.generated.xsd.ResourceData;
import org.wso2.carbon.registry.indexing.stub.generated.xsd.SearchResultsBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry  content search operation
 */
public class ContentSearchTestCase {
    private static final Log log = LogFactory.getLog(ContentSearchTestCase.class);
    private String WSDL_PATH = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/";
    private String RESOURCE_NAME = "sample.wsdl";
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ContentSearchAdminClient contentSearchAdminClient;
    public static final int RERTY_CYCLES = 30;
    private UserInfo userInfo;

    @BeforeClass(groups = {"wso2.greg.resource"})
    public void init() throws Exception {
        log.info("Initializing Tests for Community Feature in Registry Policy");
        log.debug("Community Feature in Registry Policy Test Initialised");


        int userId = 0;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        userInfo = UserListCsvReader.getUserInfo(userId);
        log.debug("Running SuccessCase");

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());


        contentSearchAdminClient = new ContentSearchAdminClient(
                environment.getGreg().getBackEndUrl(),
                userInfo.getUserName(), userInfo.getPassword());


    }


    @Test(groups = {"wso2.greg.resource"})
    public void testAddResource()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {


        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                          File.separator + "GREG" + File.separator + "wsdl" + File.separator +
                          RESOURCE_NAME;

        resourceAdminServiceClient.addResource(WSDL_PATH + RESOURCE_NAME,
                                               "application/wsdl+xml", "test resource",
                                               new DataHandler(new URL("file:///" + resource)));

        String authorUserName =
                resourceAdminServiceClient.getResource(WSDL_PATH +
                                                       RESOURCE_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   WSDL_PATH + RESOURCE_NAME + " creation failure");


    }

    @Test(groups = {"wso2.greg.resource"}, dependsOnMethods = "testAddResource")
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

        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/services/eu/" +
                                                  "dataaccess/footballpool/Info");

        resourceAdminServiceClient=null;
        contentSearchAdminClient=null;
        userInfo=null;
    }


}
