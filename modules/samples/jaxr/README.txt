JAXR  Client sample
===========

* JAXR enables Java  programmers to use an  abstraction API to access a variety of XML registries.

Steps for running this sample
---------------------------------------
1) Start the WSO2 Governance Registry with UDDI enable.

Linux :    sh wso2server.sh -Duddi=enable
Windows : wso2server.bat -Duddi=enable

2) Go in to the GREG_HOME/samples/jaxr/src/main/resources/ and update the scoutv3.properties file with valid username and password(This user must have UDDIPublisher permision).

3) Go to the sample directory(jaxr) and run the ant command.

Note: 

I) This sample creates an organization  and a service in UDDI registry.
II) After, it  browses through the UDDI registry to find that organization and service.
