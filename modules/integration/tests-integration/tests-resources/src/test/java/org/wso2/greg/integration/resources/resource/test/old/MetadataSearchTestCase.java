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

package org.wso2.greg.integration.resources.resource.test.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.activation.DataHandler;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry metadata search operation
 */

public class MetadataSearchTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(MetadataSearchTestCase.class);

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private SearchAdminServiceClient searchAdminServiceClient;

    private String WSDL_PATH = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/";
    private String RESOURCE_NAME = "sample.wsdl";

    public static final int RERTY_CYCLES = 30;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        log.info("Initializing Tests for Meta-data Search");
        log.debug("Meta-data Search Test Initialised");

        log.debug("Running SuccessCase");

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(getBackendURL(),
                        automationContext.getUser().getUserName(), automationContext.getUser().getPassword());

        searchAdminServiceClient =
                new SearchAdminServiceClient(getBackendURL(),
                        automationContext.getUser().getUserName(),
                        automationContext.getUser().getPassword());
    }

    @Test(groups = {"wso2.greg"})
    public void testAddResource()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException, XPathExpressionException {

        String resource =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                        + "GREG" + File.separator + "wsdl" + File.separator + RESOURCE_NAME;

        resourceAdminServiceClient.addResource(WSDL_PATH + RESOURCE_NAME,
                "application/wsdl+xml", "test resource",
                new DataHandler(new URL("file:///" + resource)));

        String authorUserName =
                resourceAdminServiceClient.getResource(WSDL_PATH +
                        RESOURCE_NAME)[0].getAuthorUserName();
        assertTrue(automationContext.getUser().getUserName().equalsIgnoreCase(authorUserName),
                (WSDL_PATH + RESOURCE_NAME) + " creation failure");
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = "testAddResource")
    public void testSearchMetadata()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            InterruptedException {

        //advanced search
        CustomSearchParameterBean parameterBean = new CustomSearchParameterBean();

        ArrayOfString[] finalArray = new ArrayOfString[3];

        ArrayOfString nameArray = new ArrayOfString();
        nameArray.addArray("resourcePath");
        nameArray.addArray("sample.wsdl");
        finalArray[0] = nameArray;

        ArrayOfString authorArray = new ArrayOfString();
        authorArray.addArray("author");
        authorArray.addArray("admin");
        finalArray[1] = authorArray;

        ArrayOfString contentArray = new ArrayOfString();
        contentArray.addArray("content");
        contentArray.addArray("ArrayOftCountrySelectedTopScorer");
        finalArray[2] = contentArray;

        parameterBean.setParameterValues(finalArray);

        log.info("### Metadata search retry cycle started .. ");
        boolean searchSuccess = false;
        for (int i = 0; i < RERTY_CYCLES; i++) {
            log.info("#### MetaData search cycle # " + i);
            AdvancedSearchResultsBean bean =
                    searchAdminServiceClient.getAdvancedSearchResults(parameterBean);
            if (bean.getResourceDataList() != null) {
                bean.setResourceDataList(new ResourceData[0]);
                searchSuccess = true;
                log.info("#### MetaData search PASSED .. in seconds : " + i * 15);
                break;
            } else {
                Thread.sleep(15000);
            }
        }

        assertTrue(searchSuccess, "### Content search test failed .. ");
    }

    @AfterClass
    public void cleanup() throws ResourceAdminServiceExceptionException, RemoteException {

        resourceAdminServiceClient.deleteResource(WSDL_PATH + RESOURCE_NAME);

        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/services/eu/" +
                "dataaccess/footballpool/Info");

        resourceAdminServiceClient = null;
        searchAdminServiceClient = null;
    }
}
