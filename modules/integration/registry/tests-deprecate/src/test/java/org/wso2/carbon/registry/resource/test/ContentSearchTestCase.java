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
import org.wso2.carbon.registry.indexing.stub.generated.ContentSearchAdminServiceStub;
import org.wso2.carbon.registry.indexing.stub.generated.xsd.ResourceData;
import org.wso2.carbon.registry.indexing.stub.generated.xsd.SearchResultsBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

/**
 * A test case which tests registry  content search operation
 */
public class ContentSearchTestCase {
    private static final Log log = LogFactory.getLog(ContentSearchTestCase.class);
    private String wsdlPath = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/";
    private String resourceName = "sample.wsdl";
    private ResourceAdminServiceStub resourceAdminServiceStub;
    private ContentSearchAdminServiceStub contentSearchAdminServiceStub;
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String frameworkPath = "";
    public static final int RERTY_CYCLES = 30;

    @BeforeClass(groups = {"wso2.greg.resource"})
    public void init() throws Exception {
        log.info("Initializing Tests for Community Feature in Registry Policy");
        log.debug("Community Feature in Registry Policy Test Initialised");
        loggedInSessionCookie = util.login();
        frameworkPath = FrameworkSettings.getFrameworkPath();

        log.debug("Running SuccessCase");
        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);
        contentSearchAdminServiceStub = TestUtils.getContentSearchAdminServiceStub(loggedInSessionCookie);

    }


    @Test(groups = {"wso2.greg.resource"})
    public void searchContentTest() {
        addResource();
        searchContent();
    }

    public void searchContent() {
        SearchResultsBean bean = null;

        try {
            boolean searchSuccess = false;
            // Retrying until test passes
            log.info("### Content search retry cycle started .. ");
            for (int i = 0; i < RERTY_CYCLES; i++) {
                log.info("#### Content search cycle # " + i);
                bean = contentSearchAdminServiceStub.getContentSearchResults("ArrayOftCountrySelectedTopScorer");
                if (bean.getResourceDataList() != null) {
                    bean.setResourceDataList(new ResourceData[0]);
                    searchSuccess = true;
                    log.info("#### Content search PASSED .. in seconds : " + i * 15);
                    break;

                } else {
                    doSleep();
                }
            }
            if (!searchSuccess) {
                fail("# MetaData search test failed .. ");
                log.info("# MetaData search test failed .. ");
                log.error("# MetaData search test failed .. ");
            }


        } catch (Exception e) {
            fail("Content search failed: " + e);
            log.error("Content search failed: " + e.getMessage());
            String msg = "Failed to get search results from the search service. " +
                    e.getMessage();
            log.error(msg, e);
        }
    }

    private void doSleep() {
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
        }
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

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
        }
    }


}
