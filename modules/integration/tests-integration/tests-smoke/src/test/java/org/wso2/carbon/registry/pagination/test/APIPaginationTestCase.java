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

package org.wso2.carbon.registry.pagination.test;


import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * This test is to very the pagination of the  getTags() , getComments() , getLogs().
 * https://wso2.org/jira/browse/REGISTRY-2171
 */
public class APIPaginationTestCase extends GREGIntegrationBaseTest {

    private WSRegistryServiceClient registry;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
    }

    @Test(groups = {"wso2.greg"})
    public void resourceTag() throws Exception {
        String path = "/_system/governance/paginationTag";
        Resource resource = registry.newResource();
        resource.setContent("PagingTagCheck");
        registry.put("/_system/governance/paginationTag", resource);

        for (int i = 0; i < 20; i++) {
            registry.applyTag("/_system/governance/paginationTag", Integer.toString(i));
        }
        Tag[] tags = registry.getTags(path);
        assertEquals((tags.length), 20, "Tag count is should be 20");
        try {

            PaginationContext.init(0, 5, "", "", 100);
            Tag[] tagPaginated = registry.getTags(path);
            assertEquals((tagPaginated.length), 5, "Paginated Tag count is should be 5");

        } finally {
            PaginationContext.destroy();
        }
    }

    @Test(groups = {"wso2.greg"})
    public void resourceComment() throws Exception {
        String path = "/_system/governance/paginationComment";
        Resource resource = registry.newResource();
        resource.setContent("PagingCommentCheck");
        registry.put(path, resource);

        for (int i = 0; i < 20; i++) {
            registry.addComment(path, new Comment(Integer.toString(i)));
        }
        //comments are returend as pages of 5 items by default
        Comment[] comments = registry.getComments(path);
        assertEquals((comments.length), 5, "Comment count is should be 5");
        try {

            PaginationContext.init(0, 10, "", "", 100);
            Comment[] commentPaginated = registry.getComments(path);
            assertEquals((commentPaginated.length), 10, "Paginated comment count is should be 10");

        } finally {
            PaginationContext.destroy();
        }
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"resourceComment", "resourceTag"})
    public void resourceLog() throws Exception {
        try {
            PaginationContext.init(0, 60, "", "", 100);

            LogEntry[] logEntries = registry.getLogs(null, -1, "admin", null, null, false);
            assertTrue((logEntries.length) > 40, "Log entries   should be more than 40 records ");

            PaginationContext.init(0, 5, "", "", 100);
            LogEntry[] paginatedLogEntries = registry.getLogs(null, -1, "admin", null, null, false);
            assertEquals((paginatedLogEntries.length), 5, "Log entries   should 5");

        }
        finally {
            PaginationContext.destroy();
        }
    }
}
