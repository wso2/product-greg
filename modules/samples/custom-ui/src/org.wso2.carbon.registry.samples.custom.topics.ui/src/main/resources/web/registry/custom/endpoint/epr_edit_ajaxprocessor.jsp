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
<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>
<%@ page import="org.wso2.carbon.registry.samples.custom.topics.ui.utils.GetEndpointUtil" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIException" %>
<%@ page import="org.wso2.carbon.registry.samples.custom.topics.ui.beans.EndpointBean" %>
<%
    String cPath = request.getParameter("path");
    EndpointBean bean;
    try {
        bean = GetEndpointUtil.getEndpointBean(cPath, config, session);
    } catch (UIException e) {
        %>Error occured while retrieving endpoint details<%
        return;
    }
%>

<br/>
<script type="text/javascript">
submitEPREditForm = function() {
    sessionAwareFunction(function() {
        var rForm = document.forms["eprEditForm"];
        /* Validate the form before submit */
        var reason = "";
        if (reason == "") {
            reason += validateEmpty(rForm.name, "Endpoint name");
        }
        if (reason == "") {
            reason += validateEmpty(rForm.uri, "URI");
        }
        if (reason == "") {
            reason += validateUrl(rForm.uri, "URI");
        }

        if (reason != "") {
            CARBON.showWarningDialog(reason);
            return false;
        } else {
            submitCustomViewUIForm('eprEditForm', '../registry/custom/endpoint/epr_edit_handler_ajaxprocessor.jsp');
        }
    }, "Session Timed Out");
    return true;
}
</script>

<form id="eprEditForm" name="eprEditForm">
<input type="hidden" name="path" value="<%=cPath%>"/>
<table style="width:100%" class="styledLeft">
<thead>
		<tr>
			<th colspan="2">
<strong>Edit Endpoint</strong>
</th>
		</tr>
	</thead>
	<tbody>
    <tr><td class="leftCol-med">Endpoint Name <span class="required">*</span></td>
        <td><input type="text" name="name" value="<%=bean.getName()%>"/></td>
    </tr>
    <tr>
          <td>URI <span class="required">*</span></td>
        <td><input type="text" name="uri" value="<%=bean.getUri()%>"/></td>
    </tr>
    <tr>
        <td>Format</td>
        <td><input type="text" name="format" value="<%=bean.getFormat()%>"/></td>
    </tr>
    <tr>
        <td>Optimization method</td>
        <td><input type="text" name="optimize" value="<%=bean.getOptimize()%>"/></td>
    </tr>
    <tr>
        <td>Duration to suspend this endpoint on failure</td>
        <td><input type="text" name="sd" value="<%=bean.getSuspendDurationOnFailure()%>"/></td>
    </tr>
    <tr>
        <td colspan="2" class="buttonRow"><input class="button" type="button" value="Save" onclick="submitEPREditForm();"/></td>
    </tr>
       </tbody>
</table>
</form>

<br/>