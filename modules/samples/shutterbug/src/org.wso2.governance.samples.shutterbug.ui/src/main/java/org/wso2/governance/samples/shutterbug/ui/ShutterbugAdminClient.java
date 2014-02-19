/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.governance.samples.shutterbug.ui;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.CarbonConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;
import javax.xml.stream.XMLStreamReader;
import javax.xml.namespace.QName;

public class ShutterbugAdminClient {

    private static final Log log = LogFactory.getLog(ShutterbugAdminClient.class);

    private ShutterbugAdminServiceStub stub;
    private ServletConfig config;
    private HttpSession session;

    public ShutterbugAdminClient(ServletConfig config, HttpSession session)
            throws RegistryException {
        this.config = config;
        this.session = session;
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String epr = backendServerURL + "ShutterbugAdminService";

        try {
            stub = new ShutterbugAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate resource service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public boolean vote(HttpServletRequest request) {
        String imagePath = request.getParameter("imagePath");
        if (request.getParameter("vote") == null) {
            return false;
        }
        try {
            return stub.vote(imagePath);
        } catch (Exception e) {
            String msg = "Failed to vote. " + e.getMessage();
            log.error(msg, e);
        }
        return false;
    }

    public boolean isLoggedIn() {
        try {
            return stub.getImageFeed() != null;
        } catch (Exception e) {
            String msg = "Error occured while getting image feed. " + e.getMessage();
            log.error(msg, e);
        }
        return false;
    }

    public boolean withdrawVote(HttpServletRequest request) {
        String imagePath = request.getParameter("imagePath");
        if (request.getParameter("withdrawVote") == null) {
            return false;
        }
        try {
            return stub.withdrawVote(imagePath);
        } catch (Exception e) {
            String msg = "Failed to withdraw vote. " + e.getMessage();
            log.error(msg, e);
        }
        return false;
    }

    public String getImageFeed(HttpServletRequest request) {
        try {
            return fixFeed(stub.getImageFeed());
        } catch (Exception e) {
            String msg = "Failed to get image feed. " + e.getMessage();
            log.error(msg, e);
        }
        return null;
    }

    public String getMyImageFeed(HttpServletRequest request) {
        try {
            return fixFeed(stub.getMyImageFeed());
        } catch (Exception e) {
            String msg = "Failed to get image feed. " + e.getMessage();
            log.error(msg, e);
        }
        return null;
    }

    private String fixFeed(String feed) {
        String output = feed.replace("${startData}", "<![CDATA[{").replace("${endData}", "}]]>");
        String registryURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session).replace("/services", "/registry");
        if (registryURL.endsWith("/")) {
            registryURL = registryURL.substring(0, registryURL.length() - 1);
        }
        return output.replace("${registryURL}", registryURL);
    }

    public List<String> getMyImageUrls(HttpServletRequest request) {
        List<String> myImageList = new ArrayList<String>();
        try {
            OMElement myOm = AXIOMUtil.stringToOM(getMyImageFeed(request));
            if (myOm == null) {
                return myImageList;
            }

            for (Object node : (new AXIOMXPath("//item/link")).selectNodes(myOm)) {
                myImageList.add(((OMElement) node).getText());
            } 

        } catch (Exception e) {
            String msg = "Failed to get image URLs. " + e.getMessage();
            log.error(msg, e);
        }

        return myImageList;
    }

    public List<String> getMyImageThumbnails(HttpServletRequest request) {
        List<String> myImageList = new ArrayList<String>();
        try {

            OMElement myOm = AXIOMUtil.stringToOM(getMyImageFeed(request));
            if (myOm == null) {
                return myImageList;
            }
            
            AXIOMXPath ap = new AXIOMXPath("//item/media:thumbnail");
            ap.addNamespace("media", "http://search.yahoo.com/mrss/");
            for (Object node : ap.selectNodes(myOm)) {
                myImageList.add(((OMElement) node).getAttributeValue(new QName("url")));
            }

        } catch (Exception e) {
            String msg = "Failed to get image thumbs. " + e.getMessage();
            log.error(msg, e);
        }

        return myImageList;
    }
}
