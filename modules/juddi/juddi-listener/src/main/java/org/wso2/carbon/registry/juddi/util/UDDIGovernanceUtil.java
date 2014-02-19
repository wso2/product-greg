package org.wso2.carbon.registry.juddi.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.juddi.model.BindingTemplate;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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
public class UDDIGovernanceUtil {

    private static final Log log = LogFactory.getLog(org.wso2.carbon.registry.juddi.util.UDDIGovernanceUtil.class);
    private static RegistryService registryService;
    private static final String SERVICE_VERSION = "1.0.0";
    private static final String SERVICE_NAMEAPSCE = "http://uddi.com/services";


    public static RegistryService getRegistryService() {
        registryService = (RegistryService) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(RegistryService.class);
        return registryService;
    }

    public static void acquireUDDIExternalInvokeLock() {
        CommonConstants.isExternalUDDIInvoke.set(true);
    }

    public static void releaseUDDIExternalInvokeLock() {
        CommonConstants.isExternalUDDIInvoke.set(false);
    }

    public static boolean isURLAlive(String strUrl) {
        try {
            URL url = new URL(strUrl);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.connect();
            return HttpURLConnection.HTTP_OK == urlConn.getResponseCode();
        } catch (Exception e) {
            return false;
        }
    }

    public static UserRegistry getGovernanceRegistry() throws RegistryException {
        UserRegistry userRegistry = null;
        try {
            RegistryService registryService = org.wso2.carbon.registry.uddi.utils.GovernanceUtil.getRegistryService();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            userRegistry = registryService.getGovernanceSystemRegistry(tenantId);
        } catch (NumberFormatException e) {
            log.error("Unable to convert the tenantID to integer", e);
        } catch (RegistryException e) {
            log.error("unable to create registry instance for the respective enduser", e);
        } finally {
            return userRegistry;
        }
    }

    public static void createWSDL(String url) {
        try {
            WsdlManager wsdlManager = new WsdlManager(getGovernanceRegistry());
            Wsdl wsdl = wsdlManager.newWsdl(url);
            wsdl.addAttribute("creator", "UDDI");
            wsdl.addAttribute("version", "1.0.0");
            org.wso2.carbon.registry.uddi.utils.GovernanceUtil.acquireUDDIExternalInvokeLock();
            wsdlManager.addWsdl(wsdl);
            log.info("[UDDI] WSDL successfully created in G-REG: URI - " + url);

        } catch (RegistryException e) {
            log.error("Error while persisting the business entity to UDDI registry.." + e.getMessage());
        } finally {
            org.wso2.carbon.registry.uddi.utils.GovernanceUtil.releaseUDDIExternalInvokeLock();
        }

    }


    public static void createService(BindingTemplate bindingTemplate, String ep) {
        String serviceName = null;
        String serviceDescr = null;
        try {
            if (bindingTemplate.getBusinessService().getServiceNames().size() != 0
                    && bindingTemplate.getBusinessService().getServiceNames().get(0) != null) {
                serviceName = bindingTemplate.getBusinessService().getServiceNames().get(0).getName();
            } else {
                serviceName = new StringBuilder("service_").append(bindingTemplate.getNodeId()).toString();
            }

            if (bindingTemplate.getBusinessService().getServiceDescrs().size() != 0
                    && bindingTemplate.getBusinessService().getServiceDescrs().get(0) != null) {
                serviceDescr = bindingTemplate.getBusinessService().getServiceDescrs().get(0).getDescr();
            } else {
                serviceDescr = "This service is created due to external business service insertion to G-Reg UDDI";
            }
            ServiceManager serviceManager = new ServiceManager(getGovernanceRegistry());

            if(serviceAlreadyExists(serviceManager,serviceName)){
             return;
            }
            EndpointManager endpointManager = new EndpointManager(getGovernanceRegistry());
            Endpoint endpoint = endpointManager.newEndpoint(ep);
            endpointManager.addEndpoint(endpoint);
            Service service = serviceManager.newService(new QName(SERVICE_NAMEAPSCE, serviceName));
            service.addAttribute("overview_version", SERVICE_VERSION);
            service.addAttribute("overview_description", serviceDescr);
            serviceManager.addService(service);
            Service serviceNew =  serviceManager.getService(service.getId());
            serviceNew.attachEndpoint(endpoint);
            log.info("Service " + serviceName + " added Successfully.!");
        } catch (Exception e) {
            log.error("Exception occurred while adding UDDI Service " + serviceName + e.getMessage());
        }
    }

    private static boolean serviceAlreadyExists(ServiceManager serviceManager, String serviceName) throws GovernanceException {
        final String _serviceName = serviceName;
        Service[] results = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String name = service.getAttribute("overview_name");
                String version = service.getAttribute("overview_version");
                String namespace = service.getAttribute("overview_namespace");
                if (name != null && version != null && namespace != null
                        && name.equals(_serviceName)
                        && version.equals(SERVICE_VERSION)
                        && namespace.equals(SERVICE_NAMEAPSCE)) {
                    return true;
                }
                return false;
            }
        });

        if (results != null && results.length > 0) {
            return true;
        } else {
            return false;
        }
    }
}
