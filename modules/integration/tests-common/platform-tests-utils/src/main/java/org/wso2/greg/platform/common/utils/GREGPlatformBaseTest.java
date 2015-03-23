/*
 *Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.greg.platform.common.utils;

import java.util.HashMap;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.beans.Instance;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;

public class GREGPlatformBaseTest {

	protected Log log = LogFactory.getLog(GREGPlatformBaseTest.class);
	protected Map<String, AutomationContext> contextMap;

	/**
	 * create automation context objects for every node under "GREG_CLUSTER"
	 * 
	 * @param userMode
	 * @throws XPathExpressionException
	 */
	protected void initCluster(TestUserMode userMode)
			throws XPathExpressionException {
		contextMap = new HashMap<String, AutomationContext>();
		AutomationContext automationContext = new AutomationContext(
				"GREG_Cluster", userMode);
		log.info("Cluster instance loading");
		Map<String, Instance> instanceMap = automationContext.getProductGroup()
				.getInstanceMap();
		if (instanceMap != null && instanceMap.size() > 0) {
			for (Map.Entry<String, Instance> entry : instanceMap.entrySet()) {
				String instanceKey = entry.getKey();
				contextMap.put(instanceKey, new AutomationContext(
						"GREG_Cluster", instanceKey, userMode));
				log.info(instanceKey);
			}
		}
	}

	/**
	 * get automation context object with given node key
	 * 
	 * @param key
	 * @return
	 */
	protected AutomationContext getAutomationContextWithKey(String key) {
		if (contextMap != null && contextMap.size() > 0) {
			for (Map.Entry<String, AutomationContext> entry : contextMap
					.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(key)) {
					return entry.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * login and provide session cookie for node
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	protected String getSessionCookie(AutomationContext context)
			throws Exception {
		LoginLogoutClient loginLogoutClient = new LoginLogoutClient(context);
		return loginLogoutClient.login();
	}

	/**
	 * provide backEndUrl for the context
	 * 
	 * @param context
	 * @return
	 * @throws XPathExpressionException
	 */
	protected String getBackEndUrl(AutomationContext context)
			throws XPathExpressionException {
		String resultUrl = null;
		if (context != null) {
			resultUrl = context.getContextUrls().getBackEndUrl();
		}
		return resultUrl;
	}
}
