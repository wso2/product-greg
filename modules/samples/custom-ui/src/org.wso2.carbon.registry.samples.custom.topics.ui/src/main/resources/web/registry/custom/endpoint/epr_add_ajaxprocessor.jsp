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

<%
    String parentPath = request.getParameter("parentPath");
%>

<br/>
<script type="text/javascript">
submitEPRForm = function() {
    sessionAwareFunction(function() {
        var rForm = document.forms["eprAddForm"];
        /* Validate the form before submit */
        var reason = "";
        if (reason == "") {
            reason += validateEmpty(rForm.resourceName, "Resource name");
        }
        if (reason == "") {
            reason += validateIllegal(rForm.resourceName, "Resource name");
        }
        if (reason == "") {
            reason += validateResourcePathAndLength(rForm.resourceName);
        }
        if (reason == "") {
            reason += validateEmpty(rForm.eprName, "Endpoint name");
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
            rForm.submit();
        }
    }, "Session Timed Out");
    return true;
}
</script>

<form id="eprAddForm" name="eprAddForm" action="../registry/custom/endpoint/epr_add_handler_ajaxprocessor.jsp" method="post">
<input type="hidden" name="parentPath" value="<%=parentPath%>"/>
<table style="width:100%" class="styledLeft">
	<thead>
		<tr>
			<th colspan="2">
<strong>Add Resource</strong>
</th>
		</tr>
	</thead>
	<tbody>
    <tr>
        <td class="leftCol-med">Resource name <span class="required">*</span></td>
        <td><input type="text" name="resourceName"/></td>
    </tr>
    <tr>
        <td>Endpoint name  <span class="required">*</span></td>
        <td><input type="text" name="eprName"/></td>
    </tr>
    <tr>
        <td>URI <span class="required">*</span></td>
        <td><input type="text" name="uri"/></td>
    </tr>
    <tr>
        <td>Format</td>
        <td><input type="text" name="format"/></td>
    </tr>
    <tr>
        <td>Optimization method</td>
        <td><input type="text" name="optimize"/></td>
    </tr>
    <tr>
        <td>Duration to suspend this endpoint on failure</td>
        <td><input type="text" name="sd"/></td>
    </tr>
    <tr>
        <%--<td><input type="button" value="Save" onclick="submitCustomAddUIForm('eprAddForm', '../registry/custom/topics/epr_add_handler_ajaxprocessor.jsp')"/></td>--%>
        <td colspan="2" class="buttonRow"><input class="button" type="button" onclick="submitEPRForm();" value="Save"/></td>
    </tr>
    </tbody>
</table>
</form>

<br/>