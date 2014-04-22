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
package org.wso2.greg.integration.common.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Instance;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;

public class GREGIntegrationBaseTest {
    protected Log log = LogFactory.getLog(GREGIntegrationBaseTest.class);
    protected AutomationContext automationContext;

    protected void init(TestUserMode userMode) throws XPathExpressionException {
        automationContext = new AutomationContext("GREG", userMode);
    }

    protected void initPublisher(String productGroupName, String instanceName, TestUserMode userMode, String userKey) throws XPathExpressionException {
        automationContext = new AutomationContext(productGroupName, instanceName, userMode);
    }

    protected String getBackendURL() throws XPathExpressionException {
        return automationContext.getContextUrls().getBackEndUrl();
    }

    protected String getSessionCookie() throws XPathExpressionException, LoginAuthenticationExceptionException, RemoteException {
        return automationContext.login();

    }

    protected String getServiceURL() throws XPathExpressionException {
        return automationContext.getContextUrls().getServiceUrl();
    }

    /*    protected String getRemoteRegistryURLOfProducts(String httpsPort, String hostName,
                                                        String webContextRoot) {
        String remoteRegistryURL;
        boolean webContextEnabled = Boolean.parseBoolean(prop.getProperty("carbon.web.context.enable"));

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot + "/" + "registry/";
            } else if (webContextRoot == null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "registry/";
            } else if (webContextRoot == null) {
                remoteRegistryURL = "https://" + hostName + "/" + "services/";
            } else {
                remoteRegistryURL = "https://" + hostName + "/" + webContextRoot + "/" + "registry/";
            }
        } else if (!portEnabled && webContextEnabled) {
            remoteRegistryURL = "https://" + hostName + "/" + webContextRoot + "/" + "registry/";
        } else if (portEnabled && !webContextEnabled) {
            remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "registry/";
        } else {
            remoteRegistryURL = "https://" + hostName + "/" + "registry/";
        }
        return remoteRegistryURL;
    }
    */
    protected String getRemoteRegistryURLOfProducts(String httpsPort, String hostName,
                                                    Instance productInstance) {
        String remoteRegistryURL=null;
        boolean webContextEnabled = productInstance.getProperties().containsKey(
                ContextXpathConstants.PRODUCT_GROUP_WEBCONTEXT);

        if (webContextEnabled) {
            if (productInstance != null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" +
                        productInstance + "/" + "registry/";
            } else if (productInstance == null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "registry/";
            } else if (productInstance == null) {
                remoteRegistryURL = "https://" + hostName + "/" + "services/";
            } else {
                remoteRegistryURL = "https://" + hostName + "/" + productInstance + "/" + "registry/";
            }
        }  else{
            remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "registry/";
        }

       /* if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot + "/" + "registry/";
            } else if (webContextRoot == null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "registry/";
            } else if (webContextRoot == null) {
                remoteRegistryURL = "https://" + hostName + "/" + "services/";
            } else {
                remoteRegistryURL = "https://" + hostName + "/" + webContextRoot + "/" + "registry/";
            }
        } else if (!portEnabled && webContextEnabled) {
            remoteRegistryURL = "https://" + hostName + "/" + webContextRoot + "/" + "registry/";
        } else if (portEnabled && !webContextEnabled) {
            remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "registry/";
        } else {
            remoteRegistryURL = "https://" + hostName + "/" + "registry/";
        }*/
        return remoteRegistryURL;
    }
}

