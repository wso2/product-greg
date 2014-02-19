/*
*Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.properties.test;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;

import java.rmi.RemoteException;
import java.util.Properties;

/**
 *Test class to test the <code>PropertiesAdminService</code>
 */
public class PropertiesAdminServiceTestCase {

    private static final Log log = LogFactory.getLog(PropertiesAdminServiceTestCase.class);

    private PropertiesAdminServiceStub propertiesAdminServiceStub;
    private String loggedInSessionCookie;
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String frameworkPath;

    @BeforeClass(groups = {"wso2.greg.prop.admin.service"})
    public void init() throws Exception {
        log.info("Initializing Properties Admin Service Tests");
        loggedInSessionCookie = util.login();
        frameworkPath = FrameworkSettings.getFrameworkPath();
        log.debug("Running SuccessCase");
        propertiesAdminServiceStub = TestUtils.getPropertiesAdminServiceStub(loggedInSessionCookie);
    }

    @Test(groups = {"wso2.greg.prop.admin.service"}, expectedExceptions = AxisFault.class,
            expectedExceptionsMessageRegExp = "Property cannot start with the \"registry.\" prefix.*")
    public void addHiddenPropertiesToResourceThrowException() throws PropertiesAdminServiceRegistryExceptionException, RemoteException {

        propertiesAdminServiceStub.setProperty("/", "registry.somehidden.property_name",
                         "somehidden.property_value");
    }

    @Test(groups = {"wso2.greg.prop.admin.service"}, expectedExceptions = AxisFault.class,
            expectedExceptionsMessageRegExp = "Property cannot start with the \"registry.\" prefix.*")
    public void updateHiddenPropertiesToResourceThrowException() throws PropertiesAdminServiceRegistryExceptionException, RemoteException {

        propertiesAdminServiceStub.updateProperty("/", "registry.somehidden.property_name",
                "somehidden.property_value", "some.old.prop.name");
    }

    @Test(groups = {"wso2.greg.prop.admin.service"}, expectedExceptions = AxisFault.class,
            expectedExceptionsMessageRegExp = "Cannot duplicate property name.*")
    public void addDuplicatePropertiesToResourceThrowException() throws PropertiesAdminServiceRegistryExceptionException, RemoteException {

        propertiesAdminServiceStub.setProperty("/", "add_property_name", "some.property_value");
        //trying to add an existing property name,thus, this should fail.
        propertiesAdminServiceStub.setProperty("/", "add_property_name", "some.property_value1");
    }


    @Test(groups = {"wso2.greg.prop.admin.service"}, expectedExceptions = AxisFault.class,
            expectedExceptionsMessageRegExp = "Cannot duplicate property name.*")
    public void updateDuplicatePropertiesToResourceThrowException() throws PropertiesAdminServiceRegistryExceptionException, RemoteException {

        propertiesAdminServiceStub.setProperty("/", "update_property_name_1", "some.property_value1");

        propertiesAdminServiceStub.setProperty("/", "update_property_name_2", "some.property_value2");
        //trying to rename existing property(update_property_name_1) to another existing property(update_property_name_2)
        // and thus, this should fail.
        propertiesAdminServiceStub.updateProperty("/", "update_property_name_2",
                         "some.property_value", "update_property_name_3");
    }

    @AfterMethod
    public void removeProperties() throws PropertiesAdminServiceRegistryExceptionException, RemoteException {
        System.out.println("x");
        PropertiesBean propertiesBean = propertiesAdminServiceStub.getProperties("/", "yes");
        if(propertiesBean.getProperties() != null)
           for (Property prop: propertiesBean.getProperties()) {
              propertiesAdminServiceStub.removeProperty("/", prop.getKey());
           }
    }
}
