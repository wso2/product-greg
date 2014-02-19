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
    boolean loggedIn = false;
    try {
        org.wso2.governance.samples.shutterbug.ui.ShutterbugAdminClient client =
                new org.wso2.governance.samples.shutterbug.ui.ShutterbugAdminClient(config,
                        session);
        loggedIn = client.isLoggedIn();
    } catch (Exception e) {
        response.setStatus(500);
    }
%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <title>Image gallery</title>

    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/swfobject/2.2/swfobject.js"></script>

    <script type="text/javascript" src="javascript/shutter.js"></script>
    <script type="text/javascript" src="javascript/ajax-request.js"></script>
    <script type="text/javascript" src="javascript/jquery-1.3.2.js"></script>
    <script type="text/javascript" src="javascript/jquery-ui-1.7.2.custom.min.js"></script>
    <link type="text/css" rel="stylesheet" href="css/shutterbug-styles.css"/>

    <script type="text/javascript">
        var flashvars = {
            feed: "imagefeed-rss-ajaxprocessor.jsp"
        };
        var params = {
            allowscriptaccess: "always"
        };

        swfobject.embedSWF("http://apps.cooliris.com/embed/cooliris.swf", "wall", "100%", "450", "9.0.0", "", flashvars, params);
    </script>
    <script type="text/javascript">
        function onItemSelected(item) {
            if (item == null) {
                $('#voteing').html("");
            }
            else {
                // alert(item.data.vote); // replace with your own code
                if (item.data.vote == "false") {
                    var html_1 = "<input type=\"button\" value=\"Vote\" onclick=\"AjaxRequestService.voteForImage('" + item.data.imagePath + "');\"/>";
                    $('#voteing').html(html_1);
                } else {
                    var html_2 = "<input type=\"button\" value=\"Withdraw Vote\" onclick=\"AjaxRequestService.withdrawVoteForImage('" + item.data.imagePath + "');\"/>";
                    $('#voteing').html(html_2);
                }
            }
        }

        var cooliris = {
            onEmbedInitialized: function() {
                cooliris.embed.setCallbacks({
                    select: onItemSelected
                });
            }
        };

    </script>
</head>
<body class="pageBack">
<div class="pageBaner">
</div>
<div class="pageContent">
    <div style="position:absolute;margin-top:-25px;padding-left:5px"><a href="#" style="color:#3c3c3c;font-size:14px;">Browse</a> | <a href="shutterbug-ajaxprocessor.jsp" style="color:#3c3c3c;font-size:14px">Upload</a>
        <% if (loggedIn) {%>
        | <a href="../admin/logout_action.jsp" style="color:#3c3c3c;font-size:14px">Sign-out</a>
        <% } else {%>
        | <a href="../admin/login.jsp" style="color:#3c3c3c;font-size:14px">Sign-in</a>
        <% } %></div>
    <div class="pageContent-inside">
        <div class="middle-header" colspan="2">Uploaded Images</div>
        <div id="gallary" width="100%">
            <span id="wall"></span>
        </div>
        <div id="voteing" align="center">

        </div>
    </div>
</div>
<div class="footer">&copy; 2009 WSO2 Inc.</div>
</body>
</html>
