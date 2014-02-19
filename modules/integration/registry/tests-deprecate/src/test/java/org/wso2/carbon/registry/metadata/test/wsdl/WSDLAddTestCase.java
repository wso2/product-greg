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
package org.wso2.carbon.registry.metadata.test.wsdl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.metadata.test.util.RegistryConsts;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;

import static org.testng.Assert.*;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

/**
 * This class used to add WSDL files in to the governance registry using resource-admin command.
 */
public class WSDLAddTestCase {
    private static final Log log = LogFactory.getLog(WSDLAddTestCase.class);
    private boolean isFound;
    private ResourceAdminServiceStub resourceAdminServiceStub;
    private RelationAdminServiceStub relationAdminServiceStub;

    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String frameworkPath = "";


    @BeforeClass(groups = {"wso2.greg.wsdl.a"})
    public void init() throws Exception {
        loggedInSessionCookie = util.login();
        frameworkPath = FrameworkSettings.getFrameworkPath();
        Registry governance =
                GregTestUtils.getGovernanceRegistry(GregTestUtils.getRegistry());
        org.wso2.carbon.registry.governance.api.test.TestUtils.cleanupResources(governance);
    }

    /**
     * runSuccessCase having two different of test-cases.adding wsdl from local file system and adding wsdl from global url.
     */
    @Test(groups = {"wso2.greg.wsdl.a"})
    public void runSuccessCase() {

        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);
        relationAdminServiceStub = TestUtils.getRelationAdminServiceStub(loggedInSessionCookie);
        try {
            addWSDL();
        } catch (Exception e) {
            fail("Community feature test failed : " + e);
            log.error("Community feature test failed: " + e.getMessage());
        }
    }

    private void addWSDL() throws Exception {
        String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".."
                + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                + "resources" + File.separator + "sample.wsdl";

        resourceAdminServiceStub.addResource("/_system/governance/trunk/wsdls/sample.wsdl",
                RegistryConsts.APPLICATION_WSDL_XML, "txtDesc", new DataHandler(new URL("file:///" + resource)), null, null);
        resourceAdminServiceStub.importResource("/_system/governance/trunk/wsdls", "WeatherForecastService.wsdl",
                RegistryConsts.APPLICATION_WSDL_XML, "txtDesc",
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/wsdl/WeatherForecastService.wsdl", null, null);
        ResourceTreeEntryBean searchFile1 = resourceAdminServiceStub.getResourceTreeEntry
                ("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool");
        ResourceTreeEntryBean searchFile2 = resourceAdminServiceStub.getResourceTreeEntry
                ("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01");
        String[] resourceChild1 = searchFile1.getChildren();
        String[] resourceChild2 = searchFile2.getChildren();
        for (int childCount = 0; childCount <= resourceChild1.length; childCount++) {
            if (resourceChild1[childCount].equalsIgnoreCase("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/sample.wsdl")) {
                isFound = true;
                break;
            }
        }
        if (isFound = false) {
            fail("uploaded resource not found in /_system/governance/trunk/wsdls/eu/dataaccess/footballpool/sample.wsdl");
        }
        for (int childCount = 0; childCount <= resourceChild2.length; childCount++) {
            if (resourceChild2[childCount].equalsIgnoreCase("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl")) {
                isFound = true;
                break;
            }
        }
        if (isFound = false) {
            fail("uploaded resource not found in /_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl");
        }
    }
}
