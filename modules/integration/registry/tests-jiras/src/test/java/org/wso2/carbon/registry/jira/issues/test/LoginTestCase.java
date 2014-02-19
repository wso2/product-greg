/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.jira.issues.test;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class LoginTestCase {
    int userId;

    /**
     * Try Loggin as admin
     *
     * @throws org.apache.axis2.AxisFault
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    @Test(groups = "wso2.greg", description = "Create a service")
    public void testAdminLogin() throws RegistryException, AxisFault {
        userId = ProductConstant.ADMIN_USER_ID;;
        new RegistryProviderUtil().getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Assert.assertTrue(true);
    }

    /**
     * Try Login as testuser1
     *
     * @throws org.apache.axis2.AxisFault
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    @Test(groups = "wso2.greg", description = "Create a service")
    public void testUserLogin() throws RegistryException, AxisFault {
        new RegistryProviderUtil().getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Assert.assertTrue(true);
    }
}