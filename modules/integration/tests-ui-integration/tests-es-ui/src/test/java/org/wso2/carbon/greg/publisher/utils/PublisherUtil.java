/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.greg.publisher.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import java.net.MalformedURLException;
import javax.xml.xpath.XPathExpressionException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class PublisherUtil extends GREGIntegrationUIBaseTest {
    private static final Log log = LogFactory.getLog(PublisherUtil.class);
    private static final int POOL_WAIT_SECONDS = 2;
    private ESWebDriver driver;
    private String globalUUID;
    private UIElementMapper uiElementMapper;

    public PublisherUtil(ESWebDriver driver) throws MalformedURLException, XPathExpressionException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
    }

    /**
     * Create New Asset
     *
     * @param overviewURL     Asset Overview URL
     * @param overviewName    Asset Overview Name
     * @param overviewVersion Asset Version
     * @param serviceType     Asset Type (Swaggers,Schemas,WSDLs,WADLs, etc)
     */
    public void createGenericAsset(String overviewURL, String overviewName, String overviewVersion, String serviceType)
    {
        driver.findElement(By.id(uiElementMapper.getElement("publisher.ninedot"))).click();
        driver.findElementWD(By.linkText(serviceType)).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.defaults.add.link"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.url"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.url"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.url"))).sendKeys(overviewURL);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.name"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.name"))).sendKeys(overviewName);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.version"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.version"))).sendKeys(overviewVersion);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.defaults.create.button"))).click();
        // wait until asset visible and do refresh in WAIT_SECONDS interval
        driver.findElementPoll(By.id(overviewName), WAIT_SECONDS);
        driver.findElementWD(By.linkText(serviceType)).click();
        // if page has more asset to scroll , scroll into end of page until asset available
        driver.findElementByDynamicScroll(By.id(overviewName));
        assertTrue(isElementPresent(driver, By.id(overviewName)), overviewName + " " + serviceType +
                " not created for test case " + log.getClass().getName());
    }

    public void clickOnContentTypeAsset(String overviewName, String serviceType) {
        driver.findElementWD(By.linkText(serviceType)).click();
        // wait until asset visible and do refresh in WAIT_SECONDS interval
        driver.findElementPoll(By.id(overviewName), WAIT_SECONDS);
        // if page has more asset to scroll , scroll into end of page until asset available
        driver.findElementByDynamicScroll(By.id(overviewName));
        assertTrue(isElementPresent(driver, By.id(overviewName)), overviewName +
                " not available in listing page for test case. " + log.getClass().getName());
        driver.findElement(By.id(overviewName)).click();
    }

    /**
     * Create New Asset
     *
     * @param overviewName        Asset Overview Name
     * @param overviewNamespace   Asset Version
     * @param overviewVersion     Asset Version
     * @param overviewDescription Asset Version
     * @param serviceType         Asset Type (SOAP Services,REST Services)
     */
    public void createGenericTypeAsset(String overviewName, String overviewNamespace, String overviewVersion,
            String overviewDescription, String serviceType) {
        driver.findElement(By.id(uiElementMapper.getElement("publisher.ninedot"))).click();
        driver.findElementWD(By.linkText(serviceType)).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.defaults.add.link"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.name"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.name"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.name"))).sendKeys(overviewName);

        if (uiElementMapper.getElement("publisher.restservices").equals(serviceType)) {
            driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.context"))).clear();
            driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.context")))
                    .sendKeys(overviewNamespace);
        } else if (uiElementMapper.getElement("publisher.soapservices").equals(serviceType)) {
            driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.namespace"))).clear();
            driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.namespace")))
                    .sendKeys(overviewNamespace);
        }

        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.version"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.version"))).sendKeys(overviewVersion);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.description"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.description")))
                .sendKeys(overviewDescription);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.defaults.create.button"))).click();

        assertTrue(isElementPresent(driver, By.linkText(overviewName)),
                overviewName + " " + serviceType + " is not created for test case. " + log.getClass().getName());
        String URL = driver.findElement(By.linkText(overviewName)).getAttribute("href");
        this.globalUUID = URL.split("details/")[1];

        driver.findDynamicElement(By.linkText(overviewName), POOL_WAIT_SECONDS);
        driver.findElementWD(By.linkText(serviceType)).click();
        // wait until asset visible and do refresh in WAIT_SECONDS interval
        driver.findElementPoll(By.id(globalUUID), WAIT_SECONDS);
        // if page has more asset to scroll , scroll into end of page until asset available
        driver.findElementByDynamicScroll(By.id(globalUUID));
        assertTrue(isElementPresent(driver, By.id(globalUUID)), overviewName + " " + serviceType +
                " is not listed for test case " + log.getClass().getName());
    }

    /**
     * This method will click on given asset by taking html id
     *
     * @param serviceType String type of the service
     * @param id          String element html id
     */
    public void clickOnGenericTypeAsset(String serviceType, String id) {
        driver.findElementWD(By.linkText(serviceType)).click();
        // wait until asset visible and do refresh in WAIT_SECONDS interval
        driver.findElementPoll(By.id(id), WAIT_SECONDS);
        // if page has more asset to scroll , scroll into end of page until asset available
        driver.findElementByDynamicScroll(By.id(id));
        assertTrue(isElementPresent(driver, By.id(id)), serviceType + " not available in listing page for test case " +
                log.getClass().getName());
        driver.findElement(By.id(id)).click();
    }

    /**
     * This method will update with  a given asset by taking service type and values
     *
     * @param className   String name of the class
     * @param serviceType String type of the service
     */
    public void updateAsset(String serviceType, String className, String elementID, String uniqueName) {
        driver.findElement(By.id(uiElementMapper.getElement("publisher.defaults.edit.link"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.description"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.description"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.description")))
                .sendKeys("desc" + uniqueName);

        if (uiElementMapper.getElement("publisher.soapservices").equals(serviceType)) {
            driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.namespace"))).click();
            driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.namespace"))).clear();
            driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.namespace")))
                    .sendKeys("/" + uniqueName);
        } else if (uiElementMapper.getElement("publisher.restservices").equals(serviceType)) {
            driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.context"))).click();
            driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.context"))).clear();
            driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.context"))).sendKeys("/" + uniqueName);
        }

        driver.findElement(By.id("editAssetButton")).click();
        assertTrue(isElementPresent(driver, By.id(uiElementMapper.getElement("publisher.defaults.collapse"))),
                serviceType + " Not Updated for Test Case " +
                        className);
        driver.findElementWD(By.linkText(serviceType)).click();
        // wait until asset visible and do refresh in WAIT_SECONDS interval
        driver.findElementPoll(By.id(elementID), WAIT_SECONDS);
        // if page has more asset to scroll , scroll into end of page until asset available
        driver.findElementByDynamicScroll(By.id(elementID));
        assertTrue(isElementPresent(driver, By.id(elementID)), serviceType + " : " + elementID +
                " not available in listing page for test case " + log.getClass().getName());
    }

    /**
     * This method will delete an asset
     *
     * @param elementID   String name of the class
     * @param serviceType String type of the service
     */
    public void deleteAndValidateAsset(String serviceType, String elementID) {
        driver.findElement(By.id(uiElementMapper.getElement("publisher.defaults.delete.link"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.defaults.delete.button"))).click();
        assertTrue(isElementPresent(driver, By.linkText(uiElementMapper.getElement("publisher.defaults.home"))),
                serviceType + " not deleted for Test Case");
        driver.findElementWD(By.linkText(serviceType)).click();
        // wait until asset visible and do refresh in WAIT_SECONDS interval
        driver.findElementPoll(By.id(elementID), WAIT_SECONDS);
        // if page has more asset to scroll , scroll into end of page until asset available
        driver.findElementByDynamicScroll(By.id(elementID));
        assertFalse(isElementPresent(driver, By.id(elementID)), elementID + " " + serviceType +
                " not available in listing page for test case " + log.getClass().getName());
    }

    /**
     * Returns UUID of current asset instance
     *
     * @return String
     */
    public String getUUID() {
        return this.globalUUID;
    }

    /**
     * Add a comment to lifecycle comment text area
     *
     * @param comment String
     */
    public void addLCComment(String comment) {
        driver.findElement(By.id(uiElementMapper.getElement("publisher.lifecycle.comment"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.lifecycle.comment"))).sendKeys(comment);
    }

    /**
     * Do general search in publisher for a given string
     *
     * @param keyword String
     */
    public void generalSearch(String keyword) {
        driver.findElement(By.id(uiElementMapper.getElement("publisher.search.input"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.search.input"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.search.input"))).sendKeys(keyword);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.service.search.button"))).click();
        //
    }

    /**
     * Do Landing page search in publisher for a given string
     *
     * @param keyword String
     */
    public void landingPageSearch(String keyword) {
        driver.findElement(By.cssSelector(uiElementMapper.getElement("publisher.app.title"))).click();
        generalSearch(keyword);
    }

    /**
     * Do advanced search in publisher for a given string
     *
     * @param keyword String search keyword as string
     * @param version String version
     * @param lcState String lifecycle state
     * @param tag     String tags as separated commas ("hr,engineering")
     */
    public void advancedSearch(String keyword, String version, String lcState, String tag) {
        driver.findElement(By.id(uiElementMapper.getElement("publisher.advanced.search"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.ad-search.name"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.ad-search.name"))).sendKeys(keyword);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.ad-search.version"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.ad-search.version"))).sendKeys(version);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.ad-search.lcstate"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.ad-search.lcstate"))).sendKeys(lcState);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.ad-search.tags"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.ad-search.tags"))).sendKeys(tag);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.ad-search.btn"))).click();
    }

    /**
     * Create a version for any asset
     * @param version String version
     */
    public void createVersion(String version) {
        driver.findElement(By.id(uiElementMapper.getElement("publisher.general.version"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.version.input"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.version.input"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.version.input"))).sendKeys(version);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.version.button"))).click();
        String URL = driver.findElement(By.linkText(uiElementMapper.getElement("publisher.defaults.view")))
                .getAttribute("href");
        // String URL = driver.findElement(By.linkText(overviewName)).getAttribute("href");
        this.globalUUID = URL.split("details/")[1];
    }

    /**
     * Add given type of association to asset
     *
     * @param uniqueName      String unique name
     * @param associationType String association type
     */
    public void addAssociation(String uniqueName, String associationType) {
        driver.findElement(By.id(uiElementMapper.getElement("publisher.associations"))).click();
        int currentCount = driver
                .findElements(By.className(uiElementMapper.getElement("publisher.associations.operations"))).size();
        driver.findElement(By.id(associationType)).click();

        driver.findElements(By.id("search-for-nothing-for-wait")).size();
        String assetName = driver.findElement(By.className(uiElementMapper.getElement("publisher.select2.render")))
                .findElement(By.className(uiElementMapper.getElement("publisher.resource.name"))).getText();

        driver.findElement(By.id(uiElementMapper.getElement("publisher.associations.addAssociation"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.associations"))).click();

        String addedResName = driver
                .findElements(By.className(uiElementMapper.getElement("publisher.associations.desc"))).get(currentCount)
                .findElement(By.className(uiElementMapper.getElement("publisher.resource.name"))).getText();
        String addedResType = driver
                .findElements(By.className(uiElementMapper.getElement("publisher.associations.desc"))).get(currentCount)
                .findElement(By.className(uiElementMapper.getElement("publisher.association.type"))).getText();

        assertTrue(driver.findElements(By.className(uiElementMapper.getElement("publisher.associations.operations")))
                .size() > currentCount, "association is not added for test case " + log.getClass().getName());

        assertTrue(addedResName.equals(assetName + " " + associationType),
                "Asset : " + uniqueName + " association is not added for test case " + log.getClass().getName());

        assertTrue(associationType.equals(addedResType),
                "Association type : " + associationType + " association is not valid " + log.getClass().getName());

    }

    /**
     * Validate any given asset details from asset overview page
     *
     * @param input String content
     * @param type  String Name/Version/Context .. etc
     */
    public void validateDetailsContentType(String input, String type) {
        String elementValue = driver.findElement(By.id(uiElementMapper.getElement("publisher.defaults.collapse")))
                .findElement(By.id(type + uiElementMapper.getElement("publisher.id.tail"))).getText();
        assertTrue(elementValue.equals(input), type + " is not matched to :" + input + " for test case " +
                log.getClass().getName());

    }

    /**
     * Validate any given asset details from asset overview page
     *
     * @param input String content
     * @param type  String Name/Version/Context .. etc
     */
    public void validateDetailsGenericType(String input, String type) {
        String elementValue = driver.findElement(By.id(type))
                .findElement(By.id(type + uiElementMapper.getElement("publisher.id.tail"))).getText();
        assertTrue(elementValue.equals(input), type + " is not matched to :" + input + " for test case " +
                log.getClass().getName());

    }
}