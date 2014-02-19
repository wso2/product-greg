/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.ws.client.test.general.old;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryClientUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.registry.ws.client.resource.OnDemandContentCollectionImpl;
import org.wso2.carbon.registry.ws.client.resource.OnDemandContentResourceImpl;
import org.wso2.carbon.registry.ws.stub.WSRegistryServiceStub;
import org.wso2.carbon.registry.ws.stub.xsd.WSCollection;
import org.wso2.carbon.registry.ws.stub.xsd.WSResourceData;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class GetAllWSTestCase {

    private WSRegistryServiceClient registry;
    private final String PARENT_PATH = "/testgetall";
    private WSRegistryServiceStub stub;
    private final String PATH = PARENT_PATH + "/mytext1.txt";

    @BeforeClass(groups = {"wso2.greg"})
    public void initGetAllTest() throws RegistryException, AxisFault {
        int userId = 0;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        stub = registry.getStub();
        Resource resource1 = registry.newResource();
        resource1.setContent("my text1");
        Resource resource2 = registry.newResource();
        resource2.setContent("my text2");
        registry.put(PATH, resource1);
        registry.put(PARENT_PATH + "/mytext2.txt", resource2);
        registry.addAssociation(PATH, PARENT_PATH + "/mytext2.txt", "relatesTo");
        registry.rateResource(PATH, 2);
        registry.addComment(PATH, new Comment("some comment"));
        registry.applyTag(PATH, "foo");

        registry.addAssociation(PARENT_PATH, PARENT_PATH + "/mytext2.txt", "relatesTo");
        registry.rateResource(PARENT_PATH, 3);
        registry.addComment(PARENT_PATH, new Comment("some other comment"));
        registry.applyTag(PARENT_PATH, "bar");
    }

    @Test(groups = {"wso2.greg"})
    public void testGetAllForResource() throws Exception {
        WSResourceData resourceData = stub.getAll(PATH);

        assertNotNull(resourceData.getResource());

        Resource resource;
        resource = WSRegistryClientUtils.transformWSResourcetoResource(
                registry, resourceData.getResource(), null);
        ((OnDemandContentResourceImpl) resource).setPathWithVersion(PATH);

        assertNotNull(resource.getContent(), "Unable to retrieve resource content");

        assertEquals("my text1", new String((byte[]) resource.getContent()), "Invalid content");
        assertEquals("text/plain", resource.getMediaType(), "Invalid media type");

        assertEquals(2.0F, resourceData.getAverageRating(), "Invalid average rating");
        assertEquals(2, resourceData.getRating(), "Invalid rating");
        assertEquals(1, resourceData.getTags().length, "Invalid tag length");
        assertEquals("foo", resourceData.getTags()[0].getTagName(), "Invalid tag name");
        assertEquals(1L, resourceData.getTags()[0].getTagCount(), "Invalid tag count");
        assertEquals(1, resourceData.getComments().length, "Invalid comment length");
        assertEquals("some comment", resourceData.getComments()[0].getText(),
                     "Invalid comment text");

        assertEquals(1, resourceData.getAssociations().length, "Invalid association length");
        assertEquals(PARENT_PATH + "/mytext2.txt",
                     resourceData.getAssociations()[0].getDestinationPath(), "Invalid destination path");
        assertEquals(PATH, resourceData.getAssociations()[0].getSourcePath(),
                     "Invalid source path");
        assertEquals("relatesTo", resourceData.getAssociations()[0].getAssociationType(),
                     "Invalid association type");
    }

    @Test(groups = {"wso2.greg"})
    public void testGetAllForCollection() throws Exception {
        WSResourceData resourceData = stub.getAll(PARENT_PATH);

        assertNotNull(resourceData.getResource());

        Resource resource;
        resource = WSRegistryClientUtils.transformWSCollectiontoCollection(
                registry, (WSCollection) resourceData.getResource(),
                null);
        ((OnDemandContentCollectionImpl) resource).setPathWithVersion(PARENT_PATH);

        Collection collection = (Collection) resource;
        assertEquals(2, collection.getChildCount(), "Invalid child count");

        assertEquals(3.0F, resourceData.getAverageRating(), "Invalid average rating");
        assertEquals(3, resourceData.getRating(), "Invalid rating");
        assertEquals(1, resourceData.getTags().length, "Invalid tag length");
        assertEquals("bar", resourceData.getTags()[0].getTagName(), "Invalid tag name");
        assertEquals(1L, resourceData.getTags()[0].getTagCount(), "Invalid tag count");
        assertEquals(1, resourceData.getComments().length, "Invalid comment length");
        assertEquals("some other comment", resourceData.getComments()[0].getText(),
                     "Invalid comment text");

        assertEquals(1, resourceData.getAssociations().length, "Invalid association length");
        assertEquals(PARENT_PATH + "/mytext2.txt",
                     resourceData.getAssociations()[0].getDestinationPath(), "Invalid destination path");
        assertEquals(PARENT_PATH, resourceData.getAssociations()[0].getSourcePath(),
                     "Invalid source path");
        assertEquals("relatesTo", resourceData.getAssociations()[0].getAssociationType(),
                     "Invalid association type");
    }

    @AfterClass
    public void cleanup() throws RegistryException {
        registry.delete(PARENT_PATH);
    }

}
