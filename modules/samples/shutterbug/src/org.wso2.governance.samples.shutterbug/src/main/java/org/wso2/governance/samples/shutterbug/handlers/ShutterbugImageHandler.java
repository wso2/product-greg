/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.governance.samples.shutterbug.handlers;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.governance.samples.shutterbug.utils.Utils;
import org.wso2.governance.samples.shutterbug.ShutterbugConstants;

public class ShutterbugImageHandler extends Handler implements ShutterbugConstants {

	private int uploadLimit = DEFAULT_UPLOAD_LIMIT;

    private String shutterbugHome = DEFAULT_SHUTTERBUG_HOME;

    private static final Log log = LogFactory.getLog(ShutterbugImageHandler.class);

    public void put(RequestContext requestContext) throws RegistryException {
        Registry registry = Utils.getRegistryService().getSystemRegistry();
		Resource shutterbugCollection = registry.get(shutterbugHome);
        String tenantUser = Utils.getTenantUser();
        String uuid = shutterbugCollection.getProperty(tenantUser);
        if (uuid == null) {
            uuid = UUIDGenerator.generateUUID();
            shutterbugCollection.setProperty(tenantUser, uuid);
            registry.put(shutterbugHome, shutterbugCollection);
            try {
                CurrentSession.getUserRealm().getAuthorizationManager().authorizeUser(
                        CurrentSession.getUser(), shutterbugHome + RegistryConstants.PATH_SEPARATOR
                                + uuid, ActionConstants.DELETE);
                CurrentSession.getUserRealm().getAuthorizationManager().authorizeUser(
                        CurrentSession.getUser(), shutterbugHome + RegistryConstants.PATH_SEPARATOR
                                + uuid, ActionConstants.PUT);
                CurrentSession.getUserRealm().getAuthorizationManager().authorizeUser(
                        CurrentSession.getUser(), shutterbugHome + RegistryConstants.PATH_SEPARATOR
                                + uuid, ActionConstants.GET);
            } catch (UserStoreException ignore) {
                log.warn("Not setting authorizations");
            }
            registry.put(shutterbugHome + RegistryConstants.PATH_SEPARATOR + uuid +
                    RegistryConstants.PATH_SEPARATOR + THUMBNAIL_PATH, registry.newCollection());
            registry.put(shutterbugHome + RegistryConstants.PATH_SEPARATOR + uuid +
                    RegistryConstants.PATH_SEPARATOR + VOTE_PATH, registry.newResource());
        } else {
            Collection home = (Collection)registry.get(shutterbugHome +
                    RegistryConstants.PATH_SEPARATOR + uuid);
            // We should have no more than uploadLimit. Child Count == Number of Images * 2 + 1
            // since we also have a thumbs collection with images.
            if (uploadLimit * 2 < home.getChildCount()) {
                throw new RegistryException("You have reached the upload limit of " + uploadLimit);
            }
        }

        String resourceName = requestContext.getResourcePath().getPath();
        resourceName = resourceName.substring(resourceName.lastIndexOf(
                RegistryConstants.PATH_SEPARATOR) + 1);

        requestContext.getRepository().put(shutterbugHome + RegistryConstants.PATH_SEPARATOR +
                uuid + RegistryConstants.PATH_SEPARATOR + resourceName,
                requestContext.getResource());

        Resource resource = requestContext.getRegistry().newResource();
        resource.setMediaType(JPEG_MIME);
        try {
            resource.setContentStream(Utils.scaleImage(requestContext.getResource().getContentStream(),
                    HEIGHT, WIDTH, QUALITY_PERCENTAGE));
        } catch (Exception e) {
            log.error("ThumbNail generation failed.", e);
        }
        String thumnailName = resourceName.indexOf(".") > 0?
                resourceName.substring(0, resourceName.lastIndexOf(".")) : resourceName;
        thumnailName += ".jpg";
        // Add Thumbnail
        requestContext.getRepository().put(shutterbugHome + RegistryConstants.PATH_SEPARATOR +
                uuid + RegistryConstants.PATH_SEPARATOR + THUMBNAIL_PATH +
                RegistryConstants.PATH_SEPARATOR + thumnailName, resource);

        registry.addAssociation(shutterbugHome + RegistryConstants.PATH_SEPARATOR +
                uuid + RegistryConstants.PATH_SEPARATOR + resourceName, shutterbugHome +
                RegistryConstants.PATH_SEPARATOR +
                uuid + RegistryConstants.PATH_SEPARATOR + THUMBNAIL_PATH +
                RegistryConstants.PATH_SEPARATOR + thumnailName, ASSOCIATION_TYPE_THUMBNAIL);

        requestContext.setProcessingComplete(true);
	}

    /*associations.add(new Association(home,
                                     image,
                                     "voted"));
    associations.add(new Association(image,
                                     home,
                                     "usedby"));*/

    public void delete(RequestContext requestContext) throws RegistryException {
        String resourcePath = requestContext.getResourcePath().getPath();
        String prefix = resourcePath.substring(0, resourcePath.lastIndexOf(
                RegistryConstants.PATH_SEPARATOR));
        String resourceName = resourcePath.substring(resourcePath.lastIndexOf(
                RegistryConstants.PATH_SEPARATOR));
        String thumnailName = resourceName.indexOf(".") > 0?
                resourceName.substring(0, resourceName.lastIndexOf(".")) : resourceName;
        thumnailName += ".jpg";
        // Remove Thumbnail
        Registry registry = Utils.getRegistryService().getSystemRegistry();
        registry.delete(prefix + RegistryConstants.PATH_SEPARATOR +
                THUMBNAIL_PATH + RegistryConstants.PATH_SEPARATOR + thumnailName);
        // Remove votes and associations
        Association[] associations = registry.getAssociations(resourcePath,
                ASSOCIATION_TYPE_USED_BY);
        if (associations != null && associations.length != 0) {
            for(Association association : associations) {
                String destination = association.getDestinationPath();
                registry.removeAssociation(destination, resourcePath, ASSOCIATION_TYPE_VOTED);
                registry.removeAssociation(resourcePath, destination, ASSOCIATION_TYPE_USED_BY);
            }
        }
        // Remove thumbnail associations
        Association[] thumbnailAssociations = registry.getAssociations(resourcePath,
                ASSOCIATION_TYPE_THUMBNAIL);
        if (thumbnailAssociations != null && thumbnailAssociations.length != 0) {
            registry.removeAssociation(thumbnailAssociations[0].getSourcePath(),
                    thumbnailAssociations[0].getDestinationPath(), ASSOCIATION_TYPE_THUMBNAIL);
        }
	}

    public void setUploadLimit(String uploadLimit) {
        this.uploadLimit = Integer.parseInt(uploadLimit);
    }

    public void setshutterbugHome(String shutterbugHome) {
        this.shutterbugHome = shutterbugHome;
    }
}
