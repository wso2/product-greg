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
package org.wso2.governance.samples.shutterbug.handlers;

import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.governance.samples.shutterbug.ShutterbugConstants;
import org.wso2.governance.samples.shutterbug.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShutterbugCollectionHandler extends Handler implements ShutterbugConstants {

    private int voteLimit = DEFAULT_VOTE_LIMIT;

    private String shutterbugHome = DEFAULT_SHUTTERBUG_HOME;

    private static final Log log = LogFactory.getLog(ShutterbugCollectionHandler.class);

    public void init() {
        try {
            Registry registry =  Utils.getRegistryService().getSystemRegistry();
            if (!registry.resourceExists(shutterbugHome)) {
                Collection col = registry.newCollection();
                registry.put(shutterbugHome, col);
            }
        } catch (Exception e) {
            log.error("An error occured while initializing the Shutterbug Collection Handler", e);
        }
    }

    public void addAssociation(RequestContext requestContext) throws RegistryException {
        String type = requestContext.getAssociationType();
        if (!type.equals(ASSOCIATION_TYPE_VOTED)) {
            log.debug("Non-voted association added to votes resource");
            return;
        }

        Registry registry = Utils.getRegistryService().getSystemRegistry();
        Resource shutterbugCollection = registry.get(shutterbugHome);
        String tenantUser = Utils.getTenantUser();
        String uuid = shutterbugCollection.getProperty(tenantUser);
        if (uuid == null) {
            throw new RegistryException("You need to upload an image before you vote");
        }
        String destination = requestContext.getTargetPath();

        if (!registry.resourceExists(destination)) {
            throw new RegistryException("Provided image path is invalid");    
        }

        String source = shutterbugHome + RegistryConstants.PATH_SEPARATOR + uuid +
                RegistryConstants.PATH_SEPARATOR + VOTE_PATH;

        Association[] associations = registry.getAssociations(source,
                ASSOCIATION_TYPE_VOTED);

        if (voteLimit < associations.length + 1) {
            throw new RegistryException("You have reached the vote limit of " + voteLimit);
        }

        registry.addAssociation(destination, source, ASSOCIATION_TYPE_USED_BY);
        registry.addAssociation(source, destination, ASSOCIATION_TYPE_VOTED);

        requestContext.setProcessingComplete(true);
    }

    public void removeAssociation(RequestContext requestContext) throws RegistryException {
        String type = requestContext.getAssociationType();
        if (!type.equals(ASSOCIATION_TYPE_VOTED)) {
            log.debug("Non-voted association added to votes resource");
            return;
        }

        Registry registry = Utils.getRegistryService().getSystemRegistry();
        Resource shutterbugCollection = registry.get(shutterbugHome);
        String tenantUser = Utils.getTenantUser();
        String uuid = shutterbugCollection.getProperty(tenantUser);
        if (uuid == null) {
            throw new RegistryException("You need to upload an image before you vote");
        }
        String destination = requestContext.getTargetPath();

        if (!registry.resourceExists(destination)) {
            throw new RegistryException("Provided image path is invalid");
        }

        String source = shutterbugHome + RegistryConstants.PATH_SEPARATOR + uuid +
                RegistryConstants.PATH_SEPARATOR + VOTE_PATH;

        registry.removeAssociation(destination, source, ASSOCIATION_TYPE_USED_BY);
        registry.removeAssociation(source, destination, ASSOCIATION_TYPE_VOTED);

        requestContext.setProcessingComplete(true);
    }

    public void setshutterbugHome(String shutterbugHome) {
        this.shutterbugHome = shutterbugHome;
    }

    public void setVoteLimit(int voteLimit) {
        this.voteLimit = voteLimit;
    }
}
