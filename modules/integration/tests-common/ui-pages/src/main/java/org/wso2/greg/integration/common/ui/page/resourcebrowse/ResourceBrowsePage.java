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
package org.wso2.greg.integration.common.ui.page.resourcebrowse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;
import java.util.List;

public class ResourceBrowsePage {

    private static final Log log = LogFactory.getLog(ResourceBrowsePage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public ResourceBrowsePage(WebDriver driver) throws IOException {

        this.driver = driver;
        uiElementMapper = UIElementMapper.getInstance();

        //UIElementMapper uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains("resources"))) {
            // Alternatively, we could navigate to the login page, perhaps logging out first
            throw new IllegalStateException("This is not the resource Browse page");
        }
    }

    public ResourceBrowsePage addLifeCycle(String lifeCycleName) throws IOException{
        driver.findElement(By.xpath(uiElementMapper.getElement("resource.lifecycle.minimized"))).click();
        driver.findElement(By.xpath(uiElementMapper.getElement("resource.lifecycle.add"))).click();

        Select lifeCycles = new Select(driver.findElement(By.id(uiElementMapper.getElement("resource.lifecycle.add.select.id"))));
        List<WebElement> lifeCycleList = lifeCycles.getOptions();
        boolean isALifeCycle = false;
        for (WebElement webElement : lifeCycleList) {
            String name = webElement.getText();
            if(name.equals(lifeCycleName)){
                isALifeCycle = true;
                break;
            }


        }

        if (isALifeCycle) {
            driver.findElement(By.xpath(uiElementMapper.getElement("resource.lifecycle.add.button.add"))).click();
        }else{
            log.error("Life cycle "+lifeCycleName+" does not exist" );
        }

        return new ResourceBrowsePage(driver);
    }
}
