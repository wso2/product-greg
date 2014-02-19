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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%
    boolean canAddService = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/resources/govern/service/add") && CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/resources/govern/generic/add");
    boolean canAddWSDL = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/resources/govern/wsdl/add") && CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/resources/govern/generic/add");
    boolean canAddSchema = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/resources/govern/schema/add") && CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/resources/govern/generic/add");
    boolean canAddPolicy = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/resources/govern/policy/add") && CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/resources/govern/generic/add");
    boolean canSearchMetadata = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/search/advanced-search");
    boolean canSearchActivities = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/search/activities");
    boolean canManageNotifications = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/resources/notifications");
    boolean canAddExtensions = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/extensions/add");
%>


<style type="text/css">
    .tip-table td.service {
        background-image: url(../../carbon/tenant-dashboard/images/service.png);
    }

    .tip-table td.wsdl {
        background-image: url(../../carbon/tenant-dashboard/images/wsdl.png);
    }
    .tip-table td.schema {
        background-image: url(../../carbon/tenant-dashboard/images/schema.png);
    }
    .tip-table td.policy {
        background-image: url(../../carbon/tenant-dashboard/images/policy.png);
    }


    .tip-table td.search {
        background-image: url(../../carbon/tenant-dashboard/images/search.png);
    }
    .tip-table td.activities {
        background-image: url(../../carbon/tenant-dashboard/images/activities.png);
    }
    .tip-table td.notifications {
        background-image: url(../../carbon/tenant-dashboard/images/notifications.png);
    }
    .tip-table td.extensions {
        background-image: url(../../carbon/tenant-dashboard/images/extensions.png);
    }
</style>
        <table class="tip-table">
            <tr>
                <td class="tip-top service"></td>
                <td class="tip-empty"></td>
                <td class="tip-top wsdl"></td>
                <td class="tip-empty "></td>
                <td class="tip-top schema"></td>
                <td class="tip-empty "></td>
                <td class="tip-top policy"></td>
            </tr>
            <tr>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canAddService) { %><a class="tip-title" href="../generic/list.jsp?region=region3&item=governance_list_service_menu&key=service&breadcrumb=Services&singularLabel=Service&pluralLabel=Services"><% } %>Service<% if (canAddService) { %></a><% } %> <br/>


                <p>Service is the basic entity of your SOA platform. You can manage service metadata and the
               service lifecycle as well as maintain multiple versions of a given service, and much more.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canAddWSDL) { %><a class="tip-title" href="../generic/list_content.jsp?region=region3&item=governance_list_wsdl_menu&key=wsdl&lifecycleAttribute=null&breadcrumb=WSDL&mediaType=application/wsdl+xml&singularLabel=WSDL&pluralLabel=WSDLs&hasNamespace=true"><% } %>WSDL<% if (canAddWSDL) { %></a><% } %> <br/>


                <p>WSDL defines the interface of a web service. You can store, validate and manage WSDLs with ease, keeping track
               of dependencies and associations such as services, schema and policies.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canAddSchema) { %><a class="tip-title" href="../generic/list_content.jsp?region=region3&item=governance_list_schema_menu&key=schema&lifecycleAttribute=null&breadcrumb=Schema&mediaType=application/x-xsd+xml&singularLabel=Schema&pluralLabel=Schemas&hasNamespace=true"><% } %>Schema<% if (canAddSchema) { %></a><% } %>  <br/>

                       

                <p>XML Schema defines data types in a WSDL. As in the case of WSDLs, you can keep associations of schema
                    which helps in the impact analysis process, when maintaining the data models associated with your SOA.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canAddPolicy) { %><a class="tip-title" href="../generic/list_content.jsp?region=region3&item=governance_list_policy_menu&key=policy&lifecycleAttribute=null&breadcrumb=Policy&mediaType=application/policy+xml&singularLabel=Policy&pluralLabel=Policies&hasNamespace=false"><% } %>Policy<% if (canAddPolicy) { %></a><% } %><br/>


                <p>Policies help standardize SOA behaviour. You can keep track of the policies bound to a service.
                   It also supports policy enforcement to control how SOA behaves as desired by both IT and business personnel. </p>

                    </div>
                </td>
            </tr>
            <tr>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
            </tr>
        </table>
	<div class="tip-table-div"></div>
        <table class="tip-table">
            <tr>
                <td class="tip-top search"></td>
                <td class="tip-empty"></td>
                <td class="tip-top activities"></td>
                <td class="tip-empty "></td>
                <td class="tip-top notifications"></td>
                <td class="tip-empty "></td>
                <td class="tip-top extensions"></td>
            </tr>
            <tr>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canSearchMetadata) { %><a class="tip-title" href="../search/advancedSearch.jsp?region=region3&item=metadata_search_menu"><% } %>Search<% if (canSearchMetadata) { %></a><% } %><br/>


                        <p>The repository can store any arbitrary type of resource. You can search for resources by name, author, time created or updated. You also can search for resources by media type.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canSearchActivities) { %><a class="tip-title" href="../activities/activity.jsp?region=region3&item=registry_activity_menu"><% } %>Activities<% if (canSearchActivities) { %></a><% } %><br/>


                        <p>An activity log provide an invaluable insight to what operations took place on the repository. You can browse activities while filtering them by date range, user name, or activity type.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canManageNotifications) { %><a class="tip-title" href="../notifications/notifications.jsp?region=region1&item=governance_notification_menu"><% } %>Notifications<% if (canManageNotifications) { %></a><% } %><br/>


                        <p>The registry generates events when changes are made to a particular resource or collection. You can subscribe to these events via e-mail, or forward them to a web service via SOAP or REST.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canAddExtensions) { %><a class="tip-title" href="../extensions/add_extensions.jsp?region=region3&item=add_extensions_menu"><% } %>Extensions<% if (canAddExtensions) { %></a><% } %><br/>

                        <p>While we address most governance and registry related scenarios out-of-the-box, you also can upload your own extensions that is capable of extending the basic functionality of the product</p>

                    </div>
                </td>
            </tr>
            <tr>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
            </tr>
        </table>
<p>
    <br/>
</p>
