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

package org.wso2.carbon.registry.metadata.test.schema;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceStub;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.LifecycleActions;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.registry.info.stub.InfoAdminServiceStub;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.RatingBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.metadata.test.util.RegistryConsts;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import static org.testng.Assert.*;

import static org.wso2.carbon.registry.metadata.test.util.TestUtils.isResourceExist;

/**
 * A test case which tests registry Schema add meta data operation
 */

public class SchemaAddMetadataTestCase {

    private static final Log log = LogFactory.getLog(SchemaValidateTestCase.class);
    private String schemaPath = "/_system/governance/trunk/schemas/";

    private ResourceAdminServiceStub resourceAdminServiceStub;
    private CustomLifecyclesChecklistAdminServiceStub customLifecyclesChecklistAdminServiceStub;
    private InfoAdminServiceStub infoAdminServiceStub;
    private RelationAdminServiceStub relationAdminServiceStub;
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();


    @BeforeClass(groups = {"wso2.greg.schema.c"})
    public void init() throws Exception {
        log.info("Initializing SchemaAddMetadata Tests");
        log.debug("SchemaAddMetadataTestCase Initialised");
        loggedInSessionCookie = util.login();
    }

    @Test(groups = {"wso2.greg.schema.c"}, dependsOnGroups = {"wso2.greg.schema.b"})
    public void runSuccessCase() {
        log.debug("Running SuccessCase");
        relationAdminServiceStub = TestUtils.getRelationAdminServiceStub(loggedInSessionCookie);
        infoAdminServiceStub = TestUtils.getInfoAdminServiceStub(loggedInSessionCookie);
        customLifecyclesChecklistAdminServiceStub = TestUtils.getCustomLifecyclesChecklistAdminServiceStub(loggedInSessionCookie);
        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);

        addSchemaMultipleImports();

    }

    private void addSchemaMultipleImports() {
        String resourceUrl =
//                "http://ww2.wso2.org/~qa/greg/calculator.xsd";
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/company.xsd";

        String resourceName = "company.xsd";
        String referenceSchemaFile = "person.xsd";

        try {
            resourceAdminServiceStub.importResource(schemaPath, resourceName,
                    RegistryConsts.APPLICATION_X_XSD_XML, "schemaFile", resourceUrl, null, null);

            String textContent = resourceAdminServiceStub.getTextContent(schemaPath +
                    "org/charitha/" + resourceName);

            if (textContent.indexOf("xmlns:tns=\"http://charitha.org/\"") != -1) {
                log.info("Schema content found");

            } else {
                log.error("Schema content not found");
                fail("Schema content not found");
            }

            String textContentImportedSchema = resourceAdminServiceStub.getTextContent(schemaPath +
                    "org1/charitha/" + referenceSchemaFile);

            if (textContentImportedSchema.indexOf("xmlns:tns=\"http://charitha.org1/\"") != -1) {
                log.info("Schema content found");

            } else {
                log.error("Schema content not found");
                fail("Schema content not found");
            }

            //check dependencies
            dependencyTest(schemaPath + "org/charitha/" + resourceName, schemaPath +
                    "org1/charitha/" + referenceSchemaFile);
            //check associations
            associationTest(schemaPath +
                    "org1/charitha/" + referenceSchemaFile, schemaPath + "org/charitha/" + resourceName);

            addCommentTest(schemaPath + "org/charitha/" + resourceName, "This is a sample comment for main " +
                    "schema file");
            addCommentTest(schemaPath + "org1/charitha/" + referenceSchemaFile, "This is a sample comment for " +
                    "imported schema file");

            //add tags into each schema
            tagTest(schemaPath + "org/charitha/" + resourceName, "TestTag");
            tagTest(schemaPath + "org1/charitha/" + referenceSchemaFile, "TestTag");

            //rate schemas
            rateTest(schemaPath + "org/charitha/" + resourceName, "3");
            rateTest(schemaPath + "org1/charitha/" + referenceSchemaFile, "5");

            //add lifecycles to schemas
            lifeCycleTest(schemaPath + "org/charitha/" + resourceName);
            lifeCycleTest(schemaPath + "org1/charitha/" + referenceSchemaFile);


            //delete the added resource
            resourceAdminServiceStub.delete(schemaPath +
                    "org/charitha/" + resourceName);

            resourceAdminServiceStub.delete(schemaPath +
                    "org1/charitha/" + referenceSchemaFile);

            //check if the deleted file exists in registry
            if (!isResourceExist(loggedInSessionCookie, schemaPath +
                    "org/charitha/", resourceName, resourceAdminServiceStub)) {
                log.info("Resource successfully deleted from the registry");

            } else {
                log.error("Resource not deleted from the registry");
                fail("Resource not deleted from the registry");
            }

            if (!isResourceExist(loggedInSessionCookie, schemaPath +
                    "org1/charitha/", referenceSchemaFile, resourceAdminServiceStub)) {
                log.info("Resource successfully deleted from the registry");

            } else {
                log.error("Resource not deleted from the registry");
                fail("Resource not deleted from the registry");
            }
        } catch (Exception e) {
            fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());
        }
    }


    /**
     * Check associations of uploaded schema
     */
    private void associationTest(String path, String association) {
        AssociationTreeBean associationTreeBean = null;
        try {
            //check association is in position
            associationTreeBean = relationAdminServiceStub.getAssociationTree(path, "association");
            if (!associationTreeBean.getAssociationTree().contains(association)) {
                fail("Expected association information not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while checking associations : " + e);
        }

    }

    /**
     * Check dependencies of uploaded schema
     */
    private void dependencyTest(String path, String association) {
        AssociationTreeBean associationTreeBean = null;
        try {
            associationTreeBean = relationAdminServiceStub.getAssociationTree(path, "depends");
            if (!associationTreeBean.getAssociationTree().contains(association)) {
                fail("Expected dependency information not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while checking dependencies : " + e);
        }

    }

    private void addCommentTest(String path, String commentText) {
        try {
            //add comement
            infoAdminServiceStub.addComment(commentText, path, loggedInSessionCookie);
            CommentBean commentBean = infoAdminServiceStub.getComments(path, loggedInSessionCookie);
            Comment[] comment = commentBean.getComments();

            if (!comment[0].getDescription().equalsIgnoreCase(commentText)) {
                log.error("Added comment not found");
                org.junit.Assert.fail("Added comment not found");
            }

            //remove comment
            infoAdminServiceStub.removeComment(comment[0].getCommentPath(), loggedInSessionCookie);
            commentBean = infoAdminServiceStub.getComments(path, loggedInSessionCookie);
            try {
                comment = commentBean.getComments();
            } catch (NullPointerException e) {
                log.info("Comment deleted successfully");
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occurred while put and get comment :" + e.getMessage());
            org.junit.Assert.fail("Exception occurred while put and get comment  :" + e.getMessage());
        }

    }

    private void tagTest(String path, String tagName) {
        TagBean tagBean;
        try {
            infoAdminServiceStub.addTag(tagName, path, loggedInSessionCookie);
            tagBean = infoAdminServiceStub.getTags(path, loggedInSessionCookie);
            Tag[] tag = tagBean.getTags();
            for (int i = 0; i <= tag.length - 1; i++) {
                if (!tag[i].getTagName().equalsIgnoreCase(tagName)) {
                    log.error("The given tag not found");
                    fail("Tag not found");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception thrown while adding tag : " + e);
        }
    }

    private void rateTest(String path, String rateValue) {
        RatingBean ratingBean;
        try {
            infoAdminServiceStub.rateResource(rateValue, path, loggedInSessionCookie);
            ratingBean = infoAdminServiceStub.getRatings(path, loggedInSessionCookie);
            int rateIntValue = Integer.parseInt(rateValue);
            if (ratingBean.getUserRating() != rateIntValue) {
                fail("Rating value not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while adding rate : " + e);
        }
    }

    private void lifeCycleTest(String path) throws Exception {
        String[] lifeCycleItem = {"Code Completed", "WSDL, Schema Created", "QoS Created"};
        customLifecyclesChecklistAdminServiceStub.addAspect(path, "ServiceLifeCycle");
        customLifecyclesChecklistAdminServiceStub.invokeAspect(path, "ServiceLifeCycle", "Promote", lifeCycleItem);
        LifecycleBean lifecycleBean = customLifecyclesChecklistAdminServiceStub.getLifecycleBean(path);
        Property[] lifecycleProperties = lifecycleBean.getLifecycleProperties();
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals("registry.lifecycle.ServiceLifeCycle.state")) {
                if (!"Testing".equalsIgnoreCase(property.getValues()[0])) {
                    fail("Life-cycle not promoted");
                }
            }
        }
        customLifecyclesChecklistAdminServiceStub.removeAspect(path, "ServiceLifeCycle");

    }
}
