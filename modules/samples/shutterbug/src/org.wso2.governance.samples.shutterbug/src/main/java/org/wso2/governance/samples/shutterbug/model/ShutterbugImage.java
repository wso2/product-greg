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
package org.wso2.governance.samples.shutterbug.model;

public class ShutterbugImage {

    private String resourcePath;
    private String thumbnailPath;
    private int votes;
    private String description;
    private String title;
    private boolean voted;

    public ShutterbugImage(String resourcePath, String thumbnailPath, int votes) {
        this.resourcePath = resourcePath;
        this.thumbnailPath = thumbnailPath;
        this.votes = votes;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public int getVotes() {
        return votes;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isVoted() {
        return voted;
    }

    public void setVoted(boolean voted) {
        this.voted = voted;
    }
}
