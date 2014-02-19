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
package org.wso2.governance.samples.shutterbug.utils;

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.governance.samples.shutterbug.model.ShutterbugUser;
import org.wso2.governance.samples.shutterbug.model.ShutterbugImage;
import org.wso2.governance.samples.shutterbug.ShutterbugConstants;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNamespace;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.*;

import javax.imageio.ImageIO;

public class Utils implements ShutterbugConstants {

    private static RegistryService registryService;

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        Utils.registryService = registryService;
    }

    public static String getTenantUser() {
        return TENANT_USER_PREFIX + CurrentSession.getTenantId() + "." + CurrentSession.getUser();
    }

    public static OMElement buildImageFeed(java.util.List<ShutterbugUser> userList) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement rss = factory.createOMElement("rss", null);
        OMNamespace nsMedia = factory.createOMNamespace("http://search.yahoo.com/mrss/", "media");
        OMNamespace nsAtom = factory.createOMNamespace("http://www.w3.org/2005/Atom", "atom");
        OMNamespace nsCooliris = factory.createOMNamespace(
                "http://schemas.cooliris.com/syndication/2009", "cooliris");

        rss.declareNamespace(nsMedia);
        rss.declareNamespace(nsAtom);
        rss.declareNamespace(nsCooliris);
        rss.addAttribute("version", "2.0", null);
        OMElement channel = factory.createOMElement("channel", null, rss);
        factory.createOMElement("title", null, channel).setText(FEED_TITLE);
        factory.createOMElement("link", null, channel).setText("${registryURL}");
        factory.createOMElement("description", null, channel);
        boolean isAdmin = false;
        try {
            String userName = CommonUtil.getUserRegistry(Utils.getRegistryService()).getUserName();
            String[] roles = CommonUtil.getUserRegistry(Utils.getRegistryService()).getUserRealm()
                    .getUserStoreManager().getRoleListOfUser(userName);
            for (String role : roles) {
                if (RegistryConstants.ADMIN_ROLE.equals(role)) {
                    isAdmin = true;
                    break;
                }
            }
        } catch (Exception e) {
            isAdmin = false;
        }
        Map<Integer, java.util.List<OMElement>> items = isAdmin ?
                new TreeMap<Integer, java.util.List<OMElement>>(Collections.reverseOrder()) :
                new HashMap<Integer, java.util.List<OMElement>>();
        for (ShutterbugUser user : userList) {
            for (ShutterbugImage image : user.getImages()) {
                OMElement item = factory.createOMElement("item", null);
                factory.createOMElement("title", null, item).setText(image.getTitle());
                factory.createOMElement("description", nsMedia, item).setText(
                        image.getDescription());
                factory.createOMElement("link", null, item).setText("${registryURL}/resource" +
                        image.getResourcePath());
                factory.createOMElement("thumbnail", nsMedia, item).addAttribute("url",
                        "${registryURL}/resource" + image.getThumbnailPath(), null);
                OMElement content = factory.createOMElement("content", nsMedia, item);
                content.addAttribute("url", "${registryURL}/resource" + image.getResourcePath(),
                        null);
                content.addAttribute("type", "", null);
                boolean mine;
                try {
                    mine = user.getId().equals(getCurrentUserID());
                } catch (Exception e) {
                    mine = false;
                }
                factory.createOMElement("data", nsCooliris, item).setText(
                        "${startData} \"vote\" : \"" + image.isVoted() + "\", \"mine\" : \"" +
                                Boolean.toString(mine) + "\", \"imagePath\" : \"" +
                                image.getResourcePath() + (isAdmin ? "\", \"votes\" : \"" +
                                image.getVotes()  + "\", \"owner\" : \"" +
                                user.getUserId(): "") + "\" ${endData}");
                java.util.List<OMElement> itemList = items.get(image.getVotes());
                if (itemList == null) {
                    itemList = new LinkedList<OMElement>();
                }
                itemList.add(item);
                items.put(image.getVotes(), itemList);
            }
        }
        for (java.util.List<OMElement> itemList : items.values()) {
            for (OMElement item : itemList) {
                channel.addChild(item);
            }
        }
        return rss;
    }

    public static String getCurrentUserID() throws Exception {
        Registry registry = getRegistryService().getSystemRegistry();
        Resource shutterbugCollection = registry.get(DEFAULT_SHUTTERBUG_HOME);
        UserRegistry userRegistry = CommonUtil.getUserRegistry(Utils.getRegistryService());
        String tenantUser = TENANT_USER_PREFIX + userRegistry.getTenantId() + "." +
                userRegistry.getUserName();
        return shutterbugCollection.getProperty(tenantUser);
    }

    public static InputStream scaleImage(InputStream input, int height, int width, int quality)
            throws Exception {

        Image image = ImageIO.read(new BufferedInputStream(input));

        // Maintain Aspect ratio
        int thumbHeight = height;
        int thumbWidth = width;
        double thumbRatio = (double)width / (double)height;
        double imageRatio = (double)image.getWidth(null) / (double)image.getHeight(null);
        if (thumbRatio < imageRatio) {
            thumbHeight = (int)(thumbWidth / imageRatio);
        } else {
            thumbWidth = (int)(thumbHeight * imageRatio);
        }

        BufferedImage thumb =
                new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumb.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        JPEGTranscoder transcoder = new JPEGTranscoder();
        transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(
                Math.max(0, Math.min(quality, 100)) / 100.0f));
        transcoder.writeImage(thumb, new TranscoderOutput(output));

        return new ByteArrayInputStream(output.toByteArray());
    }
}
