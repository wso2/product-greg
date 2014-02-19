<%@ page session="false" %>
<%@ page import="java.util.List,
                 org.apache.juddi.config.Install,
                 javax.xml.bind.JAXBException,
                 org.uddi.v3_service.DispositionReportFaultMessage,
                 org.apache.juddi.config.AppConfig,
                 org.apache.juddi.config.Property,
                 org.uddi.api_v3.BusinessEntity,
                 org.uddi.api_v3.Name,
                 org.uddi.api_v3.Description,
                 java.io.IOException,
                 org.apache.juddi.config.Release"
%>

<!-- index.jsp -->
<html>
<head>
<title>Apache jUDDI Registry</title>
<link rel="stylesheet" href="juddi.css" />
</head>
<body>
<div class="header" align="right"><a href="http://ws.apache.org/juddi/" target="_top">jUDDI@Apache</a></div>
<h1>Apache jUDDI version <%= Release.getRegistryVersion() %></h1>


<h3><em>Welcome</em> to Apache jUDDI!</h3>
<div class="install">
<h4>jUDDI Documentation</h4>
    <div class="content">
	<table><tr>
	<td>
	<ul>
	    <li><a href="/pluto/portal/jUDDI">jUDDI Portal (if installed)</a></li>
	    <li><a href="/jbossws/services">View</a> service listing</li>
	    <li><a href="http://juddi.apache.org/">Visit</a> the Apache-jUDDI Home Page</li>
	    <li><a href="http://issues.apache.org/jira/browse/JUDDI">Report</a> a bug</li>
	    <li><a href="http://juddi.apache.org/participate.html">Participate! We love you..</a></li>
	    <li><a href="http://juddi.apache.org/library.html">UDDI library</a></li>
	</ul>
	</td>
	<td>
	<ul>
	    <li><a href="http://juddi.apache.org/docs/3.x/userguide/html/index.html">jUDDI User Guide</a></li>
	    <li><a href="http://juddi.apache.org/docs/3.x/devguide/html/index.html">jUDDI Developer Guide</a></li>
	    <li><a href="http://svn.apache.org/repos/asf/juddi/trunk/docs/uddi3-specification/uddi-v3.0.2-20041019.pdf">UDDI v3.0.2 Specification</a></li>
	    <li><a href="http://wiki.apache.org/jUDDI">jUDDI wiki</a> - add your own docs</li>
	    <li><a href="http://apachejuddi.blogspot.com/">jUDDI Blog</a></li>
	</ul>
	</td>
	</tr>
	</table>
</div>

<div class="install">
  <h4>jUDDI Installation</h4>
    <div class="content">
<%
    // This will trigger the install process...
    String rootPartition = AppConfig.getConfiguration().getString(Property.JUDDI_ROOT_PARTITION);
	String nodeId = AppConfig.getConfiguration().getString(Property.JUDDI_NODE_ID);
    String nodeName = "";
    String nodeDescription = "";
    
    BusinessEntity be = Install.getNodeBusinessEntity(nodeId);
    
    if (be != null) {
        Name n = (Name) be.getName().get(0);
        if (n != null)
            nodeName = n.getValue();

        List descList = be.getDescription();
        if (descList != null && descList.size() > 0) {
            Description d = (Description) descList.get(0);
            if (d != null)
                nodeDescription = d.getValue();
        }
    }
%>
    <div>jUDDI has been successfully installed!</div>
    <p />
    <h3>Node Information</h3>
    <table>
        <tr>
            <td><b>Root Partition:</b></td>
            <td><%= rootPartition %></td>
        </tr>
        <tr>
            <td><b>Node Id:</b></td>
            <td><%= nodeId %></td>
        </tr>
        <tr>
            <td><b>Name:</b></td>
            <td><%= nodeName %></td>
        </tr>
        <tr>
            <td><b>Description:</b></td>
            <td><%= nodeDescription %></td>
        </tr>
    </table>
  </div>
</div>

<hr />
<table width="100%" border="0">
    <tr>
        <td height="50" align="center" valign="bottom" nowrap>
        <div class="footer">&nbsp;</div>
        </td>
    </tr>
</table>

</body>
</html>
