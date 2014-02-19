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
package org.wso2.governance.samples.shutterbug.internal;

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.governance.samples.shutterbug.handlers.ShutterbugImageHandler;
import org.wso2.governance.samples.shutterbug.handlers.ShutterbugCollectionHandler;
import org.wso2.governance.samples.shutterbug.ShutterbugConstants;
import org.wso2.governance.samples.shutterbug.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

/**
 * @scr.component name="org.wso2.governance.samples.shutterbug" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class ShutterbugSampleServiceComponent implements ShutterbugConstants {

    private static final Log log = LogFactory.getLog(ShutterbugSampleServiceComponent.class);

    protected void unsetRegistryService(RegistryService registryService) {
        Utils.setRegistryService(null);
    }

    protected void activate(ComponentContext context) {
        try {
            URLMatcher um = new URLMatcher();
            um.setAddAssociationPattern(DEFAULT_SHUTTERBUG_HOME + RegistryConstants.PATH_SEPARATOR +
                    VOTE_PATH);
            um.setRemoveAssociationPattern(DEFAULT_SHUTTERBUG_HOME + RegistryConstants.PATH_SEPARATOR +
                    VOTE_PATH);
            ShutterbugCollectionHandler sbHandler = new ShutterbugCollectionHandler();
            sbHandler.init();
            Utils.getRegistryService().getSystemRegistry().getRegistryContext()
                    .getHandlerManager().addHandler(null, um, sbHandler);
        } catch (Exception e) {
            log.error("Shutterbug Collection Handler registration failed.", e);
        }
        try {
            Utils.getRegistryService().getSystemRegistry().getRegistryContext()
                    .getHandlerManager().addHandler(null,
                    new MediaTypeMatcher(SHUTTERBUG_MEDIA_TYPE), new ShutterbugImageHandler());
        } catch (Exception e) {
            log.error("Shutterbug Image Handler registration failed.", e);
        }
        log.info("Successfully started the Shutterbug Sample.");
    }

    protected void setRegistryService(RegistryService registryService) {
        Utils.setRegistryService(registryService);

    }
}
