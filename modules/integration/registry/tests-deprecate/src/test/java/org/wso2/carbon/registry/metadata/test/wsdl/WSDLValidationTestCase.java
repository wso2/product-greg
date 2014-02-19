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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;

import static org.testng.Assert.*;

/**
 * This class used to add WSDL files in to the governance registry using resource-admin command.
 */
public class WSDLValidationTestCase {
    //private static final Log log = LogFactory.getLog(WSDLValidationTestCase.class);

    private PropertiesAdminServiceStub propertiesAdminServiceStub;
    private RelationAdminServiceStub relationAdminServiceStub;
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();


    @BeforeClass(groups = {"wso2.greg.wsdl.b"})
    public void init() throws Exception {
        loggedInSessionCookie = util.login();
    }

    /**
     * runSuccessCase having two different of test-cases.adding wsdl from local file system and adding wsdl from global url.
     */
    @Test(groups = {"wso2.greg.wsdl.b"}, dependsOnGroups = {"wso2.greg.wsdl.a"})
    public void runSuccessCase() {

        propertiesAdminServiceStub = TestUtils.getPropertiesAdminServiceStub(loggedInSessionCookie);
        relationAdminServiceStub = TestUtils.getRelationAdminServiceStub(loggedInSessionCookie);

        try {
            PropertiesBean propertiesBean = propertiesAdminServiceStub.getProperties("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "yes");
            Property[] property = propertiesBean.getProperties();
            for (int i = 0; i <= property.length - 1; i++) {
                if (property[i].getKey().equalsIgnoreCase("WSDL Validation")) {
                    assertTrue(property[i].getValue().equalsIgnoreCase("valid"), "WSDL validation not matched with expected result");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while validating wsdl : " + e);
        }
        addPropertyTest();
    }


    private void addPropertyTest() {
        try {
            propertiesAdminServiceStub.setProperty("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "TestProperty", "sample-value");
            PropertiesBean propertiesBean = propertiesAdminServiceStub.getProperties("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "yes");
            Property[] property = propertiesBean.getProperties();
            for (int i = 0; i <= property.length - 1; i++) {
                if (property[i].getKey().equalsIgnoreCase("TestProperty")) {
                    assertTrue(property[i].getValue().equalsIgnoreCase("sample-value"), "Newly added property not found");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while adding property " + e);
        }
    }
}
