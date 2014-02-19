<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%
    try {
        org.wso2.governance.samples.shutterbug.ui.ShutterbugAdminClient client =
                new org.wso2.governance.samples.shutterbug.ui.ShutterbugAdminClient(config,
                        session);
        if (request.getParameter("vote") != null) {
            // ex:- https://localhost:9443/carbon/shutterbug/vote-ajaxprocessor.jsp?vote=1&imagePath=/shutterbug/3845e15c-d7fb-420c-b56f-e623f30d0388/img01.JPG
            client.vote(request);
        } else if (request.getParameter("withdrawVote") != null) {
            // ex:- https://localhost:9443/carbon/shutterbug/vote-ajaxprocessor.jsp?withdrawVote=1&imagePath=/shutterbug/3845e15c-d7fb-420c-b56f-e623f30d0388/img01.JPG
            client.withdrawVote(request);
        }
    } catch (Exception e) {
        response.setStatus(500);
    }
%>