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

package org.wso2.carbon.registry.samples.custom.topics.util;

import org.wso2.carbon.registry.samples.custom.topics.beans.TopicBean;
import org.wso2.carbon.registry.samples.custom.topics.beans.MapEntry;
import org.wso2.carbon.registry.samples.custom.topics.TopicException;
import org.wso2.carbon.registry.samples.custom.topics.TopicConstants;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;

public class TopicUtil {

    private static final Log log = LogFactory.getLog(TopicUtil.class);

    public static TopicBean getTopicBean(String path) throws Exception {

        try {
            UserRegistry registry = CommonUtil.getRegistry();

            Resource r = registry.get(path);
            if (!(r instanceof Collection)) {
                String msg = path + " is not a collection. Topics should be collections.";
                log.error(msg);
                throw new TopicException(msg);
            }

            Collection topic = (Collection) r;

            List<MapEntry> endpointList = new ArrayList<MapEntry> ();
            List<MapEntry> subtopicList = new ArrayList<MapEntry> ();

            String[] children = (String[]) topic.getContent();
            for(String childPath: children) {
                processTopicChild(childPath, endpointList, subtopicList, registry);
            }
            r.discard();

            TopicBean topicBean = new TopicBean();
            topicBean.setEndpoints(endpointList.toArray(new MapEntry[endpointList.size()]));
            topicBean.setSubtopics(subtopicList.toArray(new MapEntry[subtopicList.size()]));
            return topicBean;

        } catch (Exception e) {
            String msg = "Failed to get topic details. " + e.getMessage();
            log.error(msg, e);
            throw new TopicException(msg, e);
        }
    }

    private static void processTopicChild(
            String childPath, List<MapEntry> endpointList, List<MapEntry> topicList, UserRegistry registry)
            throws Exception {

        Resource r = registry.get(childPath);
        if (TopicConstants.TOPIC_MEDIA_TYPE.equals(r.getMediaType())) {
            MapEntry topicEntry = new MapEntry(RegistryUtils.getResourceName(childPath), childPath);
            topicList.add(topicEntry);

        } else if (TopicConstants.ENDPOINT_MEDIA_TYPE.equals(r.getMediaType())) {
            MapEntry endpointEntry = new MapEntry(RegistryUtils.getResourceName(childPath), childPath);
            endpointList.add(endpointEntry);

        } else if (TopicConstants.SUBSCRIPTIONS_MEDIA_TYPE.equals(r.getMediaType())) {

            Collection subcriptions = (Collection) registry.get(childPath);
            String[] subscriptionPaths = (String[]) subcriptions.getContent();
            subcriptions.discard();
            for (String subscription: subscriptionPaths) {
                processTopicChild(subscription, endpointList, topicList, registry);
            }
        }
        r.discard();
    }
}
