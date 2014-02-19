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
import org.apache.juddi.model.BindingTemplate;
import org.apache.juddi.model.BusinessService;
import org.wso2.carbon.registry.juddi.util.UDDIGovernanceUtil;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import java.util.List;

public class JUDDIEntityEventListener {

    private static final Log log = LogFactory.getLog(org.wso2.carbon.registry.juddi.JUDDIEntityEventListener.class);

    @PrePersist
    private void prePersist(Object entity) {
    }


    @PostPersist
    private void postPersist(Object entity) {
        if (entity instanceof BusinessService) {
            BusinessService businessService = (BusinessService) entity;
            List<BindingTemplate> bindingTemplates = businessService.getBindingTemplates();

            for (BindingTemplate bindingTemplate : bindingTemplates) {
                String url = bindingTemplate.getAccessPointUrl();
                //Skips the Apache JUDDI internal business service insertions
                if (url != null && !url.contains("{juddi.server.baseurl}")) {
                    if(UDDIGovernanceUtil.isURLAlive(url)) {
                        UDDIGovernanceUtil.createWSDL(url);
                    } else {
                        UDDIGovernanceUtil.createService(bindingTemplate,url);
                    }
                }

            }
        }
    }






}
