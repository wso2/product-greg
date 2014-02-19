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
package org.wso2.governance.samples.shutterbug.services;

import org.wso2.governance.samples.shutterbug.utils.Utils;
import org.wso2.governance.samples.shutterbug.ShutterbugConstants;
import org.wso2.governance.samples.shutterbug.model.ShutterbugUser;
import org.wso2.governance.samples.shutterbug.model.ShutterbugImage;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Map;

public class ShutterbugService implements ShutterbugConstants {

    private static final Log log = LogFactory.getLog(ShutterbugService.class);

    public boolean vote(String imagePath) {
        try {
            CommonUtil.getUserRegistry(Utils.getRegistryService()).addAssociation(DEFAULT_SHUTTERBUG_HOME +
                    RegistryConstants.PATH_SEPARATOR + VOTE_PATH, imagePath,
                    ASSOCIATION_TYPE_VOTED);
            return true;
        } catch (Exception e) {
            log.error("Failed to vote", e);
        }
        return false;
    }

    public boolean withdrawVote(String imagePath) {
        try {
            CommonUtil.getUserRegistry(Utils.getRegistryService()).removeAssociation(DEFAULT_SHUTTERBUG_HOME +
                    RegistryConstants.PATH_SEPARATOR + VOTE_PATH, imagePath,
                    ASSOCIATION_TYPE_VOTED);
            return true;
        } catch (Exception e) {
            log.error("Failed to withdraw vote", e);
        }
        return false;
    }

    public String getImageFeed() {
        try {
            return Utils.buildImageFeed(getUserList()).toStringWithConsume();
        } catch (Exception e) {
            log.error("Failed to serialize payload", e);
            return null;
        }
    }

    public String getMyImageFeed() {
        if (getCurrentUserId() == null) {
            return null;
        }
        List<ShutterbugUser> userList = getUserList();
        for (ShutterbugUser user : userList) {
            if (user.getId().equals(getCurrentUserId())) {
                List<ShutterbugUser> currentUser = new LinkedList<ShutterbugUser>();
                currentUser.add(user);
                try {
                    return Utils.buildImageFeed(currentUser).toStringWithConsume();
                } catch (Exception e) {
                    log.error("Failed to serialize payload", e);
                    return null;
                }

            }
        }
        try {
            return Utils.buildImageFeed(new LinkedList<ShutterbugUser>()).toStringWithConsume();
        } catch (Exception e) {
            log.error("Failed to serialize payload", e);
            return null;
        }
    }

    private String getCurrentUserId() {
        try {
            return Utils.getCurrentUserID();
        } catch (Exception e) {
            log.debug("Failed to get current user id", e);
        }
        return null;
    }

    private List<ShutterbugUser> getUserList() {
        try {
            Registry registry = Utils.getRegistryService().getSystemRegistry();
            Collection collection = (Collection)registry.get(DEFAULT_SHUTTERBUG_HOME);
            String[] users = collection.getChildren();
            if (users == null || users.length == 0) {
                log.error("No Shutterbug users found");
                return null;
            }
            Properties userIdMap = collection.getProperties();
            List<ShutterbugUser> userList = new LinkedList<ShutterbugUser>();
            for(String user : users) {
                String id = user.substring(user.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
                ShutterbugUser sbUser = new ShutterbugUser(id);
                for(Map.Entry<Object, Object> e : userIdMap.entrySet()) {
                    if (e.getValue() instanceof List
                            && ((List<String>)e.getValue()).get(0).equals(id)) {
                        String key = (String) e.getKey();
                        if (key.indexOf(".") > 0) {
                            sbUser.setUserId(key.substring(key.lastIndexOf(".") + 1));
                            break;
                        }
                    }
                }
                Collection userCollection = (Collection)registry.get(user);
                String[] images = userCollection.getChildren();
                if (images == null || images.length == 0) {
                    continue;
                }
                for(String image : images) {
                    if (image.endsWith(RegistryConstants.PATH_SEPARATOR + VOTE_PATH) ||
                            image.endsWith(RegistryConstants.PATH_SEPARATOR + THUMBNAIL_PATH)) {
                        continue;
                    }
                    String thumbnail = null;
                    Association[] thumbnailAssociations = registry.getAssociations(image,
                            ASSOCIATION_TYPE_THUMBNAIL);
                    if (thumbnailAssociations != null && thumbnailAssociations.length != 0) {
                        thumbnail = thumbnailAssociations[0].getDestinationPath();
                    }
                    int votes = 0;
                    Association[] voteAssociations = registry.getAssociations(image,
                            ASSOCIATION_TYPE_USED_BY);
                    boolean voted = false;
                    if (voteAssociations != null && voteAssociations.length != 0) {
                        votes = voteAssociations.length;
                        for (Association voteAssociation : voteAssociations) {
                            if (voteAssociation.getDestinationPath().equals(DEFAULT_SHUTTERBUG_HOME
                                    + RegistryConstants.PATH_SEPARATOR + Utils.getCurrentUserID()
                                    + RegistryConstants.PATH_SEPARATOR + VOTE_PATH)) {
                                voted = true;
                                break;
                            }
                        }
                    }
                    ShutterbugImage sbImage = new ShutterbugImage(image, thumbnail, votes);
                    sbImage.setVoted(voted);
                    Resource resource = registry.get(image);
                    String name = image.substring(image.lastIndexOf(RegistryConstants.PATH_SEPARATOR)
                            + 1);
                    if (name.indexOf(".") > 0) {
                        name = name.substring(0, name.indexOf("."));
                    }
                    sbImage.setTitle(name);
                    sbImage.setDescription(resource.getDescription());
                    sbUser.addImage(sbImage);
                }
                userList.add(sbUser);
            }
            return userList;
        } catch (Exception e) {
            log.error("Unable to get Image Feed", e);
        }
        return null;
    }
}
