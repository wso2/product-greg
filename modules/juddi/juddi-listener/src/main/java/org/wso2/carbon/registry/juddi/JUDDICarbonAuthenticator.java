package org.wso2.carbon.registry.juddi;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.juddi.model.UddiEntityPublisher;
import org.apache.juddi.v3.auth.JUDDIAuthenticator;
import org.apache.juddi.v3.error.AuthenticationException;
import org.apache.juddi.v3.error.ErrorMessage;
import org.uddi.api_v3.DispositionReport;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.uddi.utils.GovernanceUtil;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class JUDDICarbonAuthenticator extends JUDDIAuthenticator {

    private static final Log log = LogFactory.getLog(org.wso2.carbon.registry.juddi.JUDDICarbonAuthenticator.class);
    private static final String UDDIPublisherPermission = "/permission/admin/manage/uddipublish";
    private static final String JUDDI_USERNAME="root";
    private static final String JUDDI_PASSWORD="root";


    public JUDDICarbonAuthenticator() {
        super();
    }

    @Override
    public String authenticate(String s, String s1) throws org.apache.juddi.v3.error.AuthenticationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (userName == null) {
            // [REGISTRY-1602] When service added from JAXR client, username value of the PrivilegedCarbonContext is null
            // Therefore we are setting up username as value which is coming via JAXR client.
            userName = MultitenantUtils.getTenantAwareUsername(s);;
        }
        RegistryService registryService = GovernanceUtil.getRegistryService();
        String authKey = null;

        AuthorizationManager authorizationManager;
        try {
            authorizationManager = registryService.getUserRealm(tenantId).getAuthorizationManager();
        } catch (UserStoreException e) {
           throw new AuthenticationException(new ErrorMessage(e.getMessage()), new DispositionReport());
        } catch (RegistryException e) {
            throw new AuthenticationException(new ErrorMessage(e.getMessage()), new DispositionReport());
        }
        try {
            if (authorizationManager.isUserAuthorized(userName,
                        UDDIPublisherPermission, UserMgtConstants.EXECUTE_ACTION)) {
                    authKey = super.authenticate(JUDDI_USERNAME, JUDDI_PASSWORD);

                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("User is not authorized to publish artifacts to UDDI repository");
                    }
                }
        } catch (UserStoreException e) {
            throw new AuthenticationException(new ErrorMessage(e.getMessage()), new DispositionReport());
        }
        return authKey;
    }

    @Override
    public UddiEntityPublisher identify(String s, String s1) throws org.apache.juddi.v3.error.AuthenticationException {
        return super.identify(s, s1);
    }
}
