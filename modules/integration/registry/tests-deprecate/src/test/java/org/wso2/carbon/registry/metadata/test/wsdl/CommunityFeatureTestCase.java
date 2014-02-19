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
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceStub;
//import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.ExceptionException;
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
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;

import static org.testng.Assert.*;

/**
 * This class used to add WSDL files in to the governance registry using resource-admin command.
 */
public class CommunityFeatureTestCase {
    private static final Log log = LogFactory.getLog(CommunityFeatureTestCase.class);
    private RelationAdminServiceStub relationAdminServiceStub;
    private InfoAdminServiceStub infoAdminServiceStub;
    private CustomLifecyclesChecklistAdminServiceStub customLifecyclesChecklistAdminServiceStub;
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();


    @BeforeClass(groups = {"wso2.greg.wsdl.c"})
    public void init() throws Exception {
        loggedInSessionCookie = util.login();
        relationAdminServiceStub = TestUtils.getRelationAdminServiceStub(loggedInSessionCookie);
        infoAdminServiceStub = TestUtils.getInfoAdminServiceStub(loggedInSessionCookie);
        customLifecyclesChecklistAdminServiceStub = TestUtils.getCustomLifecyclesChecklistAdminServiceStub(loggedInSessionCookie);

    }

    /**
     * runSuccessCase having two different of test-cases.adding wsdl from local file system and adding wsdl from global url.
     */
//    @Test(groups = {"wso2.greg.wsdl.c"},dependsOnGroups = {"wso2.greg.wsdl.b"})
//    public void runSuccessCase() {
//
//        try {
//            associationTest();
//            dependencyTest();
//            tagTest();
//            commentTest();
//            rateTest();
//            lifeCycleTest();
//        } catch (Exception e) {
//            fail("Community feature test failed : " + e);
//            log.error("Community feature test failed: " + e.getMessage());
//        }
//
//    }
    @Test(groups = {"wso2.greg.wsdl.c"}, dependsOnGroups = {"wso2.greg.wsdl.b"})
    public void associationTest() {
        AssociationTreeBean associationTreeBean = null;
        try {
            //check association is in position
            associationTreeBean = relationAdminServiceStub.getAssociationTree("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "association");
            if (!associationTreeBean.getAssociationTree().contains("usedBy")) {
                fail("Expected association information not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while checking associations : " + e);
        }

    }

    @Test(groups = {"wso2.greg.wsdl.c"}, dependsOnGroups = {"wso2.greg.wsdl.b"}, dependsOnMethods = {"associationTest"})
    public void dependencyTest() {
        AssociationTreeBean associationTreeBean = null;
        try {
            //check dependency information is in position
            associationTreeBean = relationAdminServiceStub.getAssociationTree("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "depends");
            if (!associationTreeBean.getAssociationTree().contains("/_system/governance/trunk/endpoints/net/restfulwebservices/www/wcf/ep-WeatherForecastService-svc")) {
                fail("Expected dependency information not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while checking dependencies : " + e);
        }

    }

    @Test(groups = {"wso2.greg.wsdl.c"}, dependsOnGroups = {"wso2.greg.wsdl.b"}, dependsOnMethods = {"tagTest"})
    public void commentTest() {
        try {
            infoAdminServiceStub.addComment("this is sample comment", "/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", loggedInSessionCookie);
            infoAdminServiceStub.addComment("this is sample comment2", "/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", loggedInSessionCookie);
            CommentBean commentBean = infoAdminServiceStub.getComments("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", loggedInSessionCookie);
            Comment[] comment = commentBean.getComments();
            if (!comment[0].getDescription().equalsIgnoreCase("this is sample comment")) {
                log.error("Added comment not found");
                fail("Added comment not found");
            }
            if (!comment[1].getDescription().equalsIgnoreCase("this is sample comment2")) {
                log.error("Added comment not found");
                fail("Added comment not found");
            }
            infoAdminServiceStub.removeComment(comment[0].getCommentPath(), loggedInSessionCookie);
            commentBean = infoAdminServiceStub.getComments("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", loggedInSessionCookie);
            comment = commentBean.getComments();
            if (comment[0].getDescription().equalsIgnoreCase("this is sample comment")) {
                log.error("Comment not deleted");
                fail("Comment not deleted");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occured while adding/getting comment :" + e.getMessage());
            fail("Exception occured while adding/getting comment :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.wsdl.c"}, dependsOnGroups = {"wso2.greg.wsdl.b"}, dependsOnMethods = {"dependencyTest"})
    public void tagTest() {

        TagBean tagBean;
        try {
            infoAdminServiceStub.addTag("SampleTag", "/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", loggedInSessionCookie);
            tagBean = infoAdminServiceStub.getTags("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", loggedInSessionCookie);
            Tag[] tag = tagBean.getTags();
            for (int i = 0; i <= tag.length - 1; i++) {
                if (!tag[i].getTagName().equalsIgnoreCase("SampleTag")) {
                    fail("Tag not found : SampleTag");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception thrown while adding tag : " + e);
        }
    }

    @Test(groups = {"wso2.greg.wsdl.c"}, dependsOnGroups = {"wso2.greg.wsdl.b"}, dependsOnMethods = {"commentTest"})
    public void rateTest() {
        RatingBean ratingBean;
        try {
            infoAdminServiceStub.rateResource("2", "/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", loggedInSessionCookie);
            ratingBean = infoAdminServiceStub.getRatings("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", loggedInSessionCookie);
            if (ratingBean.getUserRating() != 2) {
                fail("Required user rating not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while adding rate : " + e);
        }
    }

    @Test(groups = {"wso2.greg.wsdl.c"}, dependsOnGroups = {"wso2.greg.wsdl.b"}, dependsOnMethods = {"rateTest"})
    public void lifeCycleTest() throws Exception {
        String[] lifeCycleItem = {"Requirements Gathered", "Architecture Finalized", "High Level Design Completed"};
        customLifecyclesChecklistAdminServiceStub.addAspect("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "ServiceLifeCycle");
        customLifecyclesChecklistAdminServiceStub.invokeAspect("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "ServiceLifeCycle", "Promote", lifeCycleItem);
        LifecycleBean lifecycleBean = customLifecyclesChecklistAdminServiceStub.getLifecycleBean("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl");
        Property[] lifecycleProperties = lifecycleBean.getLifecycleProperties();
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals("registry.lifecycle.ServiceLifeCycle.state")) {
                if (!"Testing".equalsIgnoreCase(property.getValues()[0])) {
                    fail("Life-cycle not promoted");
                }
            }
        }
        customLifecyclesChecklistAdminServiceStub.removeAspect("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "ServiceLifeCycle");
    }
}
