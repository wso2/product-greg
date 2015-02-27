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
<%@ page import="java.net.URLDecoder" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%
    List<String> imageUrlList = null;
    List<String> thumbUrlList = null;
    boolean loggedIn = false;
    try {
        org.wso2.governance.samples.shutterbug.ui.ShutterbugAdminClient client =
                new org.wso2.governance.samples.shutterbug.ui.ShutterbugAdminClient(config,
                        session);
        imageUrlList = client.getMyImageUrls(request);
        thumbUrlList = client.getMyImageThumbnails(request);
        loggedIn = client.isLoggedIn();
    } catch (Exception e) {
        response.setStatus(500);
    }

    String error = request.getParameter("error");
    String errorMsg = null;
    if (request.getParameter("msg") != null) {
        errorMsg = URLDecoder.decode(request.getParameter("msg"), "UTF-8");
    }
%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <link type="text/css" rel="stylesheet" href="css/shutterbug-styles.css"/>
    <link type="text/css" href="css/ui-lightness/jquery-ui-1.7.2.custom.css" rel="stylesheet" />
    <title>User Area</title>

    <script type="text/javascript" src="javascript/shutter.js"></script>
    <script type="text/javascript" src="javascript/ajax-request.js"></script>
    <script type="text/javascript" src="javascript/jquery-1.3.2.js"></script>
    <script type="text/javascript" src="javascript/jquery-ui-1.7.2.custom.min.js"></script>
</head>
<body class="pageBack">
<%
    if (error != null && error.equals("true")) {
%>
<script type="text/javascript">
    $.ui.dialog.defaults.bgiframe = true;
    $(function() {
        $("#dialog").dialog();
    });
</script>
<%
    } else {
        errorMsg = "";
    }
%>
<div class="pageBaner">
</div>
<div class="pageContent">
    <div id="dialog" title="Warning">
        <p><%=errorMsg%>
        </p>
    </div>
    <div style="position:absolute;margin-top:-25px;padding-left:5px"><a href="shutterbugBrowser-ajaxprocessor.jsp"
                                                                        style="color:#3c3c3c;font-size:14px;">Browse</a>
        | <a href="#" style="color:#3c3c3c;font-size:14px">Upload</a>
        <% if (loggedIn) {%>
        | <a href="../admin/logout_action.jsp" style="color:#3c3c3c;font-size:14px">Sign-out</a>
        <% } else {%>
        | <a href="../admin/login.jsp" style="color:#3c3c3c;font-size:14px">Sign-in</a>
        <% } %></div>
    <div class="pageContent-inside">
        <div id="imageUploadx">
            <form onsubmit="true" method="post" name="resourceUploadForm"
                  id="resourceUploadForm"
                  action="../../fileupload/resource" enctype="multipart/form-data" target="_self">

                <input type="hidden" id="path" name="path" value="/"/>
                <input type="hidden" id="redirect" name="redirect" value="shutterbug/shutterbug-ajaxprocessor.jsp"/>
                <input id="uResourceMediaType" type="hidden" name="mediaType" value="application/vnd.wso2.shutterbug"/>
                <input type="hidden" id="errorRedirect" name="errorRedirect"
                       value="shutterbug/shutterbug-ajaxprocessor.jsp?error=true"/>


                <table class="styledLeft">
                    <tr>
                        <td class="middle-header" colspan="2">Upload Image</td>
                    </tr>
                    <tr>
                        <td valign="top" style="width:120px;">

                            <span>File <span class="required">*</span></span></td>
                        <td>
                            <input id="uResourceFile" type="file" name="upload" style="background-color:#cccccc"
                                   onchange="fillResourceUploadDetails()"/>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top">Name <span class="required">*</span></td>

                        <td><input id="uResourceName" type="text" name="filename"
                                   style="margin-bottom:10px;"/></td>
                        <% if (imageUrlList != null && imageUrlList.size() != 0) {
                            for (int i = 0; i < imageUrlList.size(); i++) {%>
                        <td id="td-<%=i%>"><img src="<%=thumbUrlList.get(i)%>" alt="photo" height="50px" width="50px">
                            <input type="button"
                                   onclick="AjaxRequestService.deleteImage('<%=imageUrlList.get(i)%>', '<%=i%>');"
                                   value="Delete">
                        </td>
                        <%
                                }
                            }
                        %>

                    </tr>
                    <tr>
                        <td valign="top">Description <span class="required">*</span></td>
                        <td><textarea name="description" id="description" rows="7" cols="50"></textarea></td>

                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" onClick="if (validateDesc()) resourceUploadForm.submit()" class="button" value="Add"
                                    />
                            <input type="button" class="button" value="Cancel"/>
                        </td>
                    </tr>
                </table>

            </form>
        </div>
    </div>
</div>
<div class="footer">&copy; 2009 WSO2 Inc.</div>
</body>

</body>
</html>
