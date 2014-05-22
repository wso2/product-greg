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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import javax.xml.xpath.XPathExpressionException;
import java.net.MalformedURLException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class Carbon2163TestCase extends GREGIntegrationBaseTest{

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception{
        super.init(TestUserMode.SUPER_TENANT_USER);
    }

    @Test(groups = "wso2.greg", description = "Try to Login from invalid username and add resource to RemoteRegistry",
          expectedExceptions = RegistryException.class)
    public void testAPIArtifact() throws MalformedURLException, RegistryException, XPathExpressionException {

        String registryURL = UrlGenerationUtil.getRemoteRegistryURL(automationContext
                .getDefaultInstance());

        //invalid user name and password
        RemoteRegistry remoteRegistry = new RemoteRegistry(registryURL, "admin9999Invalid",
                "invalidPassword");

        Resource r2 = remoteRegistry.newResource();
        r2.setContent("this is the content".getBytes());
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");
        r2.setProperty("test1", "value1");


        remoteRegistry.put("/text2", r2);

        String path = "/pathimport";
        String url = "http://shortwaveapp.com/waves.txt";

        Resource r4 = remoteRegistry.newResource();
        r4.setMediaType("text/plain");
        remoteRegistry.importResource(path, url, r4);


        String comment1 = "this is qa comment 4";
        String comment2 = "this is qa comment 5";
        Comment c1 = new Comment();
        c1.setResourcePath("/text2");
        c1.setText("This is default comment");
        c1.setUser(automationContext.getContextTenant().getContextUser().getUserName());

        remoteRegistry.addComment("/text2", c1);
        remoteRegistry.addComment("/text2", new Comment(comment1));
        remoteRegistry.addComment("/text2", new Comment(comment2));


        r2.discard();

        Resource r3 = remoteRegistry.get("/text2");
        assertNotNull(new String((byte[]) r3.getContent()), "Resource 3 does not exist");

        boolean b = remoteRegistry.resourceExists("/");
        assertTrue(b, "Resource does not exist");
    }

}
