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
<%@ page import="org.wso2.carbon.registry.samples.custom.topics.ui.utils.AddEndpointUtil" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIException" %>

<%
    String parentPath = request.getParameter("parentPath");
    try {
        AddEndpointUtil.addEndpointBean(request, config, session);
    } catch (UIException e) {
        %>
Could not add endpoint
<%
        return;
    }

    String resourcePagePath = "../../../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" + parentPath;
    
    response.sendRedirect(resourcePagePath);
%>



