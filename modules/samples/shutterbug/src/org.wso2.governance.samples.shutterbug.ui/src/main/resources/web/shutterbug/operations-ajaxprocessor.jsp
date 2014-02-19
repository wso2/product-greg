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
<%@ page import="javax.xml.stream.XMLStreamReader" %>
<%@ page import="javax.xml.stream.XMLInputFactory" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="org.apache.axiom.om.impl.builder.StAXOMBuilder" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.governance.samples.shutterbug.ui.ShutterbugAdminClient" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%
    List<String> imageUrlList = null;

    ShutterbugAdminClient client =
            new ShutterbugAdminClient(config,
                    session);
    imageUrlList = client.getMyImageUrls(request);

    String funcName = request.getParameter("func");

    if (funcName.equals("vote")) {
        //out.print("Hello");
        out.print(client.vote(request));
    } else if (funcName.equals("withdrawVote")) {
        client.withdrawVote(request);
    }


%>