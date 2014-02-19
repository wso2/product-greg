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
package org.wso2.carbon.registry.xpath.query.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.registry.xpath.query.test.utils.XpathQueryUtil;

import java.net.MalformedURLException;
import java.rmi.RemoteException;


public class QueryFromRootTestCase {


    public WSRegistryServiceClient registry;

    @BeforeClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void init()
            throws MalformedURLException, RegistryException, LoginAuthenticationExceptionException,
                   RemoteException {
        int userId = ProductConstant.ADMIN_USER_ID;
        RegistryProviderUtil providerUtil = new RegistryProviderUtil();
        registry = providerUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

    }

    /*
     * Test any resource form root
     */
    @Test(groups = {"wso2.greg"}, description = "Test Query From Root")
    public void queryResourceInRootTestCase() throws RegistryException {
        Collection collection = registry.newCollection();
        collection.setDescription("Testing Collection");
        collection.setChildren(new String[]{"test"});

        registry.put("/collection", collection);
        boolean ok = false;

        for (String path : XpathQueryUtil.searchFromXpathQuery(registry, "/", "/")) {
            if (path.equals("/collection")) {
                ok = true;
                break;
            }

        }
        Assert.assertTrue(ok);


    }

    /*
     * Test  resources or collections form root with having large
     * number of collections
     */
    @Test(groups = {"wso2.greg"}, description = "Test Query With Large Number OF Collections From Root")
    public void queryResourcesWithLargeCollectionsTestCase() throws RegistryException {
        for (int i = 0; i < 15; i++) {
            Collection collection = registry.newCollection();
            registry.put("/collection" + i, collection);

        }
        String[] paths = XpathQueryUtil.searchFromXpathQuery(registry, "/", "/");
        int count = 0;
        for (int i = 0; i < 15; i++) {
            for (String path : paths) {
                if (path.equals("/collection" + i)) {
                    count++;
                }
            }
        }
        Assert.assertEquals(15, count);
    }

    /*
     * Query by specifying any resource by name
     */
    @Test(groups = {"wso2.greg"}, description = "Test Query From Any Resource")
    public void queryFromAnyResourceName() throws RegistryException {
        String[] paths = XpathQueryUtil.searchFromXpathQuery(registry, null, "//collection");
        for (String path : paths) {
            Assert.assertEquals(path, "/_system/config/repository/components/org.wso2.carbon.governance" +
                                      "/media-types/index/collection"
            );
        }
    }


    @AfterClass(groups = {"wso2.greg"})
    public void deleteResources() throws RegistryException {
        registry.delete("/collection");
        for (int i = 0; i < 15; i++) {
            registry.delete("/collection" + i);
        }
        registry = null;
    }


}


