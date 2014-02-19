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

package org.wso2.carbon.registry.resource.test;

import static org.testng.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceStub;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A test case which tests registry metadata search operation
 */

public class MetadataSearchTestCase {
    private static final Log log = LogFactory.getLog(MetadataSearchTestCase.class);

    private String wsdlPath = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/";
    private String resourceName = "sample.wsdl";
    private SearchAdminServiceStub searchAdminServiceStub;
    private ResourceAdminServiceStub resourceAdminServiceStub;
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String frameworkPath = "";
    public static final int RERTY_CYCLES = 30;

    @BeforeClass(groups = {"wso2.greg.resource"})
    public void init() throws Exception {
        log.info("Initializing Tests for Meta-data Search");
        log.debug("Meta-data Search Test Initialised");
        loggedInSessionCookie = util.login();
        frameworkPath = FrameworkSettings.getFrameworkPath();

        log.debug("Running SuccessCase");
        searchAdminServiceStub = TestUtils.getSearchAdminServiceStub(loggedInSessionCookie);
        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);

    }

    public void addResource() {
        try {
            String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                    File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                    "resources" + File.separator + resourceName;

            resourceAdminServiceStub.addResource(wsdlPath + resourceName,
                    "application/wsdl+xml", "test resource", new DataHandler(new URL("file:///" + resource)), null, null);

        } catch (Exception e) {
            fail("Unable to get file content: " + e);
            log.error("Unable to get file content: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.resource"})
    public void searchContentTest() {
        addResource();
        searchMetadata();
    }


    private void doSleep() {
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
        }
    }

    public void searchMetadata() {

        AdvancedSearchResultsBean bean = null;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {

            // searchAdminCommand.getSearchResultsSuccessCase("sample.wsdl", "admin", dateFormat.format(calendar.getTime()), dateFormat.format(calendar.getTime()));
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
                bean = searchAdminServiceStub.getAdvancedSearchResults(parameterBean);
                if (bean.getResourceDataList() != null) {
                    bean.setResourceDataList(new ResourceData[0]);
                    searchSuccess = true;
                    log.info("#### MetaData search PASSED .. in seconds : " + i * 15);
                    break;
                } else {
                    doSleep();
                }
            }
            if (!searchSuccess) {
                fail("### Content search test failed .. ");
                log.info("### Content search test failed .. ");
                log.error("### Content search test failed .. ");
            }

        } catch (Exception e) {
            fail("Failed to get search results from the search service: " + e);
            log.error("Failed to get search results from the search service: " + e);

        }


    }

}
