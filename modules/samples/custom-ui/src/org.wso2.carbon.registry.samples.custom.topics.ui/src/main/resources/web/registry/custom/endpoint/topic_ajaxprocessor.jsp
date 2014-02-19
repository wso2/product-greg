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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.wso2.carbon.registry.samples.custom.topics.ui.clients.TopicServiceClient" %>
<%@ page import="org.wso2.carbon.registry.samples.custom.topics.stub.beans.xsd.TopicBean" %>
<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>
<%@ page import="org.wso2.carbon.registry.samples.custom.topics.stub.beans.xsd.MapEntry" %>
<%
    MapEntry[] endpoints = new MapEntry[0];
    MapEntry[] topics = new MapEntry[0];
    try {
        String path = RegistryUtil.getPath(request);
        TopicServiceClient client = new TopicServiceClient(config, session);
        TopicBean bean = client.getTopicBean(path);

        endpoints = bean.getEndpoints();
        if (endpoints == null) {
            endpoints = new MapEntry[0];
        }

        topics = bean.getSubtopics();
        if (topics == null) {
            topics = new MapEntry[0];
        }
        
    } catch (Exception e) {
        %>Error occured while processing the topic details<br/>Error: <%=e.getMessage()%><%
        return;
    }
%>

<h3>Subscribed endpoints</h3>
<br/>

<% if (endpoints.length == 0) { %>
No endpoints are subscribed for this topic
<%
    } else {
        %><ul><%
        for (MapEntry endpoint: endpoints) {

            %><li><a href="./resource.jsp?viewType=std&path=<%=endpoint.getValue()%>"><%=endpoint.getName()%></a></li><%
        }
        %></ul><%
    }
%>

<br/>

<h3>Subtopics</h3>
<br/>

<% if (topics.length == 0) { %>
No subtopics are defined under this topic
<%
    } else {
        %><ul><%
        for (MapEntry topic: topics) {

            %><li><a href="./resources.jsp?path=<%=topic.getValue()%>"><%=topic.getName()%></a></li><%
        }
        %></ul><%
    }
%>

