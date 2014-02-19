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

package org.wso2.carbon.registry.jira.issues.test;


import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.util.HashMap;
import java.util.Map;

public class WSRegistryExecuteQueryTestCase {

    private WSRegistryServiceClient wsRegistryServiceClient;
    private static final String SYSTEM_CONFIG_QS_Q1 = "/_system/config/qs/q1";


    @BeforeClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int userId = ProductConstant.ADMIN_USER_ID;
        RegistryProviderUtil providerUtil = new RegistryProviderUtil();
        wsRegistryServiceClient = providerUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
    }


    @Test(groups = {"wso2.greg"}, description = "WSRegistryExecuteQueryTesting")
    public void executeQueryTestCase() throws RegistryException {
        Resource resource = wsRegistryServiceClient.newResource();
        resource.setContent("Hello Out there!");
        String resourcePath = "/abc";
        wsRegistryServiceClient.put(resourcePath, resource);
        wsRegistryServiceClient.rateResource(resourcePath, 3);
        Comment comment = new Comment();
        comment.setText("Wow! A comment out there");
        wsRegistryServiceClient.addComment(resourcePath, comment);
        Resource getResource = wsRegistryServiceClient.get("/abc");
        Assert.assertNotNull(getResource);
        /*executeQuery test */
        String sql = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE R WHERE R.REG_MEDIA_TYPE LIKE ?";
        Resource resourceTemp = wsRegistryServiceClient.newResource();
        resourceTemp.setContent(sql);
        resourceTemp.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        resourceTemp.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                                 RegistryConstants.RESOURCES_RESULT_TYPE);
        wsRegistryServiceClient.put(SYSTEM_CONFIG_QS_Q1, resourceTemp);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("1", RegistryConstants.SQL_QUERY_MEDIA_TYPE); // media type
        Collection collection = wsRegistryServiceClient.executeQuery(SYSTEM_CONFIG_QS_Q1, parameters);
        Assert.assertNotNull(collection);
        String[] content = (String[]) collection.getContent();
        Assert.assertNotNull(content);
        boolean pass = false;
        for (String result : content) {
            if (result.equals(SYSTEM_CONFIG_QS_Q1)) {
                pass = true;
                break;
            }

        }
        Assert.assertTrue(pass);
    }


    @AfterClass(groups = {"wso2.greg"})
    public void deleteResources() throws RegistryException {
        wsRegistryServiceClient.delete("/abc");
        wsRegistryServiceClient.delete(SYSTEM_CONFIG_QS_Q1);
        wsRegistryServiceClient = null;
    }

}





