/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.samples.custom.topics.ui.utils;

import org.wso2.carbon.registry.samples.custom.topics.ui.beans.EndpointBean;
import org.wso2.carbon.registry.common.ui.UIException;
import org.wso2.carbon.registry.resource.ui.clients.CustomUIServiceClient;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;

public class GetEndpointUtil {

    private static final Log log = LogFactory.getLog(GetEndpointUtil.class);

    public static EndpointBean getEndpointBean(
            String path, ServletConfig config, HttpSession session) throws UIException {

        try {
            CustomUIServiceClient customUIServiceClient =
                    new CustomUIServiceClient(config, session);

            String content = customUIServiceClient.getTextContent(path);
            OMElement endpointElement = AXIOMUtil.stringToOM(content);

            String name = endpointElement.getAttributeValue(new QName(null, "name"));

            OMElement addressElement = endpointElement.getFirstChildWithName(new QName(null, "address"));
            String uri = addressElement.getAttributeValue(new QName(null, "uri"));
            String format = addressElement.getAttributeValue(new QName(null, "format"));
            String optimize = addressElement.getAttributeValue(new QName(null, "optimize"));

            OMElement sdElement = addressElement.getFirstChildWithName(new QName(null, "suspendDurationOnFailure"));
            String sd = sdElement.getText();

            EndpointBean endpointBean = new EndpointBean();
            endpointBean.setName(name);
            endpointBean.setUri(uri);
            endpointBean.setFormat(format);
            endpointBean.setOptimize(optimize);
            endpointBean.setSuspendDurationOnFailure(sd);

            return endpointBean;

        } catch (Exception e) {
            String msg = "Failed to get end point details. " + e.getMessage();
            log.error(msg, e);
            throw new UIException(msg, e);
        }
    }
}
