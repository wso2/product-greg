/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.samples.populator.utils;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.*;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;

import javax.activation.DataHandler;
import java.rmi.RemoteException;

/**
 * This class can be used to upload swagger docs from url. And can be extended to import any other content
 * type resource.
 */
public class SwaggerImportClient {
    private static final Log log = LogFactory.getLog(SwaggerImportClient.class);

    private final String serviceName = "ResourceAdminService";
    private ResourceAdminServiceStub resourceAdminServiceStub;
    private static final String MEDIA_TYPE_SWAGGER = "application/swagger+json";

    public SwaggerImportClient(String serviceUrl, String sessionCookie) throws AxisFault {
        String endPoint = serviceUrl + serviceName;
        resourceAdminServiceStub = new ResourceAdminServiceStub(endPoint);
        setCookie(resourceAdminServiceStub,sessionCookie);
    }

    /**
     *
     * @param resourceName  name of the resource.
     * @param description   description for the resource.
     * @param fetchURL      URL of the swagger doc.
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     */
    public void addSwagger(String resourceName, String description, String fetchURL)
            throws RemoteException, ResourceAdminServiceExceptionException {
        resourceAdminServiceStub
                .importResource("/", resourceName, MEDIA_TYPE_SWAGGER, description, fetchURL, null, null);
    }

    private static void setCookie(Stub stub, String cookie) {
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        if (cookie != null) {
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        }
    }
}
